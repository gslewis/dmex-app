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

import net.gslsrc.dmex.exercise.Exercise;
import net.gslsrc.dmex.exercise.ExerciseSession;
import net.gslsrc.dmex.settings.Settings;
import net.gslsrc.dmex.servlet.DMEXServlet;
import net.gslsrc.dmex.servlet.InvalidRequestException;
import static net.gslsrc.dmex.servlet.ConfigServlet.SESSION_ATTR_SETTINGS;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Shows the form to configure the worksheets.
 * <p>
 * Currently just allows the number of problems to be included to be selected.
 *
 * @author Geoff Lewis
 */
public class SheetServlet extends DMEXServlet {

    /** Session attribute that contains the current {@link ExerciseSession}. */
    public static final String SESSION_ATTR_EXSESSION =
            "net.gslsrc.dmex.exercise.ExerciseSession";

    /**
     * Session attribute that contains the number of problems to be rendered
     * in the worksheet.  This is stored in the session so that it can be
     * passed to the {@link DownloadServlet} during a redirect.
     */
    public static final String SESSION_ATTR_PROBLEM_COUNT =
            "net.gslsrc.dmex.servlet.pdf.SheetServlet.problemCount";

    private static final long serialVersionUID = 7960365710204810428L;

    private static final int MAX_PROBLEMS = 100;

    @Override
    public void init() throws ServletException {
        super.init();

        registerSessionAttributes(SESSION_ATTR_EXSESSION,
                                  SESSION_ATTR_PROBLEM_COUNT);
    }

    // Redirected here from ConfigServlet.doPost() when exercise settings have
    // been validated.  There should be a Settings object in the session so we
    // start a new ExerciseSession.
    //
    // We display the configpage.jsp form which posts back to this servlet.
    // The form is validated in doPost() and, if OK, we redirect to the
    // DownloadServlet.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        HttpSession session = req.getSession();

        String eid = extractId(req.getPathInfo());
        if (eid == null) {
            throw new InvalidRequestException("Missing exercise id");
        }

        ExerciseSession exsession = null;

        Settings settings = (Settings)getAttr(session, SESSION_ATTR_SETTINGS);
        if (settings == null) {
            // There are no settings: we have arrived by coming back from the
            // download page so expect an existing exercise session.
            exsession = (ExerciseSession)getAttr(session,
                                                 SESSION_ATTR_EXSESSION);

        } else {
            // There are settings: we have arrived by coming forward from the
            // exercise settings page (configex).  Create a new exercise
            // session.

            Exercise ex = getExercise(eid);
            if (ex == null) {
                throw new InvalidRequestException(
                        "Unknown exercise '" + eid + "'");
            }

            exsession = ex.newSession(settings);
            setAttr(session, SESSION_ATTR_EXSESSION, exsession);
            setAttr(session, SESSION_ATTR_SETTINGS, null);
        }

        if (exsession == null) {
            throw new InvalidRequestException("Missing exercise session");
        }

        req.getRequestDispatcher("/configpage.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        HttpSession session = req.getSession();

        // Ensure we clear any previous download session.
        setAttr(session, DownloadServlet.SESSION_ATTR_XML_DOM, null);
        setAttr(session, SESSION_ATTR_PROBLEM_COUNT, null);

        String eid = extractId(req.getPathInfo());
        if (eid == null) {
            throw new InvalidRequestException("Missing exercise id");
        }

        ExerciseSession exsession =
                (ExerciseSession)getAttr(session, SESSION_ATTR_EXSESSION);
        if (exsession == null) {
            throw new InvalidRequestException("Missing exercise session");
        }

        // Just truncate the max problems at the limit, rather than show an
        // error.
        int count = Math.min(MAX_PROBLEMS,
                             getCount(req.getParameter("problemCount")));

        if (count <= 0) {
            setErrors(req, "Number of problems must be specified");
            req.getRequestDispatcher("/configpage.jsp").include(req, resp);
            return;
        }

        // Store the problem count in the session so it is available to the
        // download servlet.  It needs to remain available so that we can
        // refresh the download page and generate a new batch of problems from
        // the ExerciseSession using the same problem count.
        setAttr(session, SESSION_ATTR_PROBLEM_COUNT, Integer.valueOf(count));

        String path = getServletContext().getContextPath() + "/download/" + eid;
        resp.sendRedirect(resp.encodeRedirectURL(path));
    }

    private int getCount(String paramValue) {
        int count = -1;

        if (paramValue != null) {
            try {
                count = Integer.parseInt(paramValue);
            } catch (NumberFormatException nfe) {
                count = -1;
            }
        }

        return count;
    }
}
