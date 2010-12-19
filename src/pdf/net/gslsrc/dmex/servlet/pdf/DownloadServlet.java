/*
Copyright (c) 2010 Geoff Lewis <gsl@gslsrc.net>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package net.gslsrc.dmex.servlet.pdf;

import net.gslsrc.dmex.exercise.ExerciseSession;
import net.gslsrc.dmex.exercise.Problem;
import net.gslsrc.dmex.render.ProblemRenderer;
import net.gslsrc.dmex.render.ProblemRendererFactory;
import net.gslsrc.dmex.render.DOMRenderContext;
import net.gslsrc.dmex.render.PDFRenderer;
import net.gslsrc.dmex.render.PDFOutputType;
import net.gslsrc.dmex.render.StreamPDFRenderContext;
import net.gslsrc.dmex.render.RenderContext;
import net.gslsrc.dmex.render.RenderException;
import net.gslsrc.dmex.render.xsl.ProblemTemplates;
import net.gslsrc.dmex.render.xsl.ProblemTemplates.OutputType;
import net.gslsrc.dmex.servlet.DMEXServlet;
import net.gslsrc.dmex.servlet.InvalidRequestException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

/**
 * Handles the presentation of the download links and the actual download of
 * the PDF work sheets.
 *
 * @author Geoff Lewis
 */
public class DownloadServlet extends DMEXServlet {

    /** Servlet context attribute that stores the global sheet counter. */
    public static final String CONTEXT_ATTR_SHEET_COUNTER =
            "net.gslsrc.dmex.servlet.pdf.DownloadServlet.sheetCounter";

    /**
     * Session attribute that stores the XML DOM of the rendered problems.  We
     * store the DOM so that we can use it for each download link by
     * {@link PDFOutputType} on the download page and get the same set of
     * problems rendered in each PDF.
     */
    public static final String SESSION_ATTR_XML_DOM =
            "net.gslsrc.dmex.render.DOMRenderContext";

    /** Request attribute that contains the array of supported
     * {@link PDFOUtputType}s according to the problem type.  We only create
     * download links for the supported output types.
     */
    private static final String REQUEST_ATTR_SUPPORTED_TYPES =
            "net.gslsrc.dmex.render.xsl.ProblemTemplates.OutputType";

    /** Maximum number of sheets allowed before the counter resets to 1. */
    private static final int MAX_SHEET_ID = 9999;

    private static final long serialVersionUID = 8897528207597847443L;

    private PDFRenderer pdfRenderer;

    @Override
    public void init() throws ServletException {
        super.init();

        registerSessionAttributes(SESSION_ATTR_XML_DOM);

        getServletContext().setAttribute(CONTEXT_ATTR_SHEET_COUNTER,
                                         new AtomicInteger());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        HttpSession session = req.getSession();

        String eid = extractId(req.getPathInfo());
        if (eid == null) {
            throw new InvalidRequestException("Missing exercise id");
        }

        ExerciseSession exsession = (ExerciseSession)getAttr(session,
                                        SheetServlet.SESSION_ATTR_EXSESSION);
        if (exsession == null) {
            throw new InvalidRequestException("Missing exercise session");
        }

        Class<? extends Problem> problemType = exsession.getProblemType();

        ProblemTemplates templates = ProblemTemplates.getTemplates(problemType);
        if (templates == null) {
            throw new InvalidRequestException("No templates for " + eid);
        }

        Collection<PDFOutputType> outputTypes =
                                    getSupportedPDFTypes(eid, templates);

        String typeParam = req.getParameter("type");

        // No selection has been made.  Show the download page with links to
        // each sheet type that can be downloaded.
        if (typeParam == null) {
            req.setAttribute(REQUEST_ATTR_SUPPORTED_TYPES, outputTypes);

            req.getRequestDispatcher("/download.jsp").include(req, resp);
            return;
        }

        PDFOutputType outputType = getPDFOutputType(typeParam, outputTypes);

        // If there is a DOM in the session, retrieve it, else generate the
        // problems and store in the session so that the same DOM can be used
        // for each link, in case the user downloads multiple PDFs from the
        // same page.

        Document doc = null;
        if (isRefresh(req)) {
            setAttr(session, SESSION_ATTR_XML_DOM, null);
        } else {
            doc = (Document)getAttr(session, SESSION_ATTR_XML_DOM);
        }

        if (doc == null) {
            doc = renderProblems(session, eid, exsession, problemType);

            // Store the DOM in the session in case we click other links on
            // the same page.
            setAttr(session, SESSION_ATTR_XML_DOM, doc);
        }

        byte[] content = null;
        try {
            content = renderSheet(problemType, doc, outputType);
        } catch (RenderException re) {
            throw new InvalidRequestException("Failed to render problems", re);
        }

        String filename = makeFileName(outputType, doc);

        resp.setContentType("application/pdf");
        resp.addHeader("Content-Length", String.valueOf(content.length));
        resp.addHeader("Content-Disposition",
                       "attachment;filename=" + filename);
        resp.getOutputStream().write(content, 0, content.length);
        resp.getOutputStream().flush();
    }

    private boolean isRefresh(HttpServletRequest req) {
        String value = req.getParameter("new");

        return value != null && "true".equalsIgnoreCase(value.trim());
    }

    @SuppressWarnings("unchecked")
    private Document renderProblems(HttpSession session, String eid,
            ExerciseSession exsession, Class<? extends Problem> problemType)
        throws InvalidRequestException {

        Integer count = (Integer)getAttr(session,
                                    SheetServlet.SESSION_ATTR_PROBLEM_COUNT);
        if (count == null || count.intValue() <= 0) {
            throw new InvalidRequestException("Missing problem count");
        }

        Collection<Problem> problems = new LinkedList<Problem>();
        for (int i = 0; i < count.intValue(); ++i) {
            problems.add(exsession.nextProblem());
        }

        // Store the sheet id in the DOM so that it can be used by all output
        // types to make the file name.
        final int cid = nextSheetId();
        DOMRenderContext context = new DOMRenderContext() {
            @Override
            public Document init() {
                return init("cid",
                        String.format("%04d", Integer.valueOf(cid)));
            }
        };

        context.setTitle(getExercise(eid).getMessage("exercise.title." + eid));

        ProblemRenderer<?> renderer = findRenderer(problemType, context);
        if (renderer == null) {
            throw new InvalidRequestException(
                    "Found no DOM renderer for " + problemType.getName());
        }

        try {
            renderer.render((Collection)problems, context);
        } catch (RenderException re) {
            throw new InvalidRequestException("Failed to render problems", re);
        }

        return context.getDocument();
    }

    private ProblemRenderer findRenderer(Class<? extends Problem> problemType,
            RenderContext context) {
        return ProblemRendererFactory.getFactory(problemType)
                .getRenderer(context.getClass());
    }

    private byte[] renderSheet(Class<? extends Problem> problemType,
            Document dom, PDFOutputType outputType) throws RenderException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Throws RenderEx if can't get templates.
        Templates templates = ProblemTemplates.getTemplates(problemType)
                                    .getTemplates(outputType);

        getPDFRenderer().render(new DOMSource(dom), templates,
                                new StreamPDFRenderContext(baos));

        return baos.toByteArray();
    }

    private PDFRenderer getPDFRenderer() {
        if (pdfRenderer == null) {
            pdfRenderer = new PDFRenderer();
        }

        return pdfRenderer;
    }

    private String makePrefix(PDFOutputType outputType) {
        assert outputType != null;

        String result = null;

        switch (outputType) {
            case PROBLEMS:
                result = "problems";
                break;
            case ANSWERS:
                result = "answers";
                break;
            case PROBLEMS_WITH_ANSWERS:
                result = "problems_wa";
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unsupported PDFOutputType " + outputType);
        }

        return result;
    }

    private String makeFileName(PDFOutputType outputType, Document doc) {
        assert outputType != null;
        assert doc != null;

        StringBuilder sb = new StringBuilder();
        sb.append(makePrefix(outputType));

        String cid = doc.getDocumentElement().getAttribute("cid");
        if (cid != null) {
            cid = cid.trim();

            if (!cid.isEmpty()) {
                sb.append("-").append(cid);
            }
        }

        sb.append(".pdf");

        return sb.toString();
    }

    private Collection<PDFOutputType> getSupportedPDFTypes(String eid,
            ProblemTemplates templates) throws InvalidRequestException {
        Collection<PDFOutputType> list = new LinkedList<PDFOutputType>();

        for (OutputType type : templates.getSupportedTypes()) {
            if (type instanceof PDFOutputType) {
                list.add((PDFOutputType)type);
            }
        }

        if (list.isEmpty()) {
            throw new InvalidRequestException("No PDF output for " + eid);
        }

        return list;
    }

    private PDFOutputType getPDFOutputType(String typeParam,
            Collection<PDFOutputType> outputTypes)
        throws ServletException {

        for (PDFOutputType type : outputTypes) {
            if (type.name().equalsIgnoreCase(typeParam)) {
                return type;
            }
        }

        throw new InvalidRequestException("Invalid PDF output " + typeParam);
    }

    private int nextSheetId() {
        int id = 0;

        AtomicInteger counter = (AtomicInteger)getServletContext()
                                    .getAttribute(CONTEXT_ATTR_SHEET_COUNTER);

        synchronized (counter) {
            id = counter.incrementAndGet();

            if (id > MAX_SHEET_ID) {
                counter.set(1);
                id = 1;
            }
        }

        return id;
    }
}
