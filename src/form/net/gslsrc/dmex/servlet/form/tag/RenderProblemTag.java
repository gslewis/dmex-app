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

package net.gslsrc.dmex.servlet.form.tag;

import net.gslsrc.dmex.exercise.Problem;
import net.gslsrc.dmex.exercise.ProblemAnswer;
import net.gslsrc.dmex.render.ProblemRenderer;
import net.gslsrc.dmex.render.ProblemRendererFactory;
import net.gslsrc.dmex.render.RenderException;
import net.gslsrc.dmex.render.XMLRenderContext;
import net.gslsrc.dmex.render.xsl.ProblemTemplates;
import net.gslsrc.dmex.render.xsl.ProblemTemplates.OutputType;
import net.gslsrc.dmex.render.xsl.HTMLOutputType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

/**
 * Renders a problem as an HTML form.
 *
 * @author Geoff Lewis
 */
public class RenderProblemTag extends SimpleTagSupport {

    private Problem problem;
    private ProblemAnswer answer;
    private Locale locale;
    private boolean form = true;

    @SuppressWarnings("unchecked")
    @Override
    public void doTag() throws IOException, JspException {
        if (problem == null) {
            throw new JspException(this + " missing \"problem\"");
        }

        XMLRenderContext context = new XMLRenderContext();
        context.setRootTag(problem.getExerciseId());

        if (answer != null) {
            context.setAttribute(XMLRenderContext.ATTR_ANSWER, answer);
        }

        ProblemRenderer<?> renderer = ProblemRendererFactory
                .getFactory(problem.getClass())
                .getRenderer(XMLRenderContext.class);

        if (renderer == null) {
            throw new JspException(this + " found no renderer for "
                    + problem.getClass().getName());
        }

        // Render to SAX.
        try {
            renderer.render((Collection)Collections.singleton(problem),
                            context);
        } catch (RenderException re) {
            throw new JspException(this + " failed to render " + problem, re);
        }

        OutputType type = form ? HTMLOutputType.FORM : HTMLOutputType.PLAIN;

        try {
            Templates templates = ProblemTemplates
                                        .getTemplates(problem.getClass())
                                        .getTemplates(type);

            String text = transform(templates, context.getSource());

            getJspContext().getOut().write(text);
        } catch (RenderException re) {
            throw new JspException(this + " failed to render " + problem, re);
        }
    }

    private String transform(Templates templates, Source source)
        throws RenderException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            templates.newTransformer().transform(source, new StreamResult(out));
        } catch (TransformerException te) {
            throw new RenderException("Failed to transform", te);
        }

        return out.toString();
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public void setAnswer(ProblemAnswer answer) {
        this.answer = answer;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setForm(boolean form) {
        this.form = form;
    }
}
