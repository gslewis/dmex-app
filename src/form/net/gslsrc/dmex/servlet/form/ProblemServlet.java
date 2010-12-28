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

package net.gslsrc.dmex.servlet.form;

import net.gslsrc.dmex.exercise.Exercise;
import net.gslsrc.dmex.exercise.ExerciseSession;
import net.gslsrc.dmex.exercise.Problem;
import net.gslsrc.dmex.exercise.ProblemAnswer;
import net.gslsrc.dmex.exercise.ProblemAnswer.ErrorCode;
import net.gslsrc.dmex.settings.Settings;
import net.gslsrc.dmex.servlet.DMEXServlet;
import net.gslsrc.dmex.servlet.InvalidRequestException;
import net.gslsrc.dmex.servlet.form.validate.ProblemValidator;
import static net.gslsrc.dmex.servlet.ConfigServlet.SESSION_ATTR_SETTINGS;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handles rendering and validation of {@link Problem}s.
 * <p>
 * Presentation is handled by <code>exsession.jsp</code>.
 * <p>
 * A GET request is used when starting an {@link ExerciseSession}, when
 * skipping a problem or when moving to the next problem (after showing a
 * solution).  A POST request is used for validating a problem submission.
 * <p>
 * Problem-specific JavaScript files can be supplied via the
 * "ProblemServlet.properties" file.
 *
 * @author Geoff Lewis
 */
public class ProblemServlet extends DMEXServlet {

    /** Session attribute that contains the current {@link ExerciseSession}. */
    public static final String SESSION_ATTR_EXSESSION =
            "net.gslsrc.dmex.exercise.ExerciseSession";

    private static final String SESSION_ATTR_PROBLEM =
            "net.gslsrc.dmex.exercise.Problem";
    private static final String SESSION_ATTR_PROBLEM_ATTEMPTS =
            "net.gslsrc.dmex.exercise.Problem.attempts";

    private static final String REQUEST_ATTR_PROBLEM_ANSWER =
            "net.gslsrc.dmex.exercise.ProblemAnswer";
    private static final String REQUEST_ATTR_PROBLEM_NUMBER =
            "net.gslsrc.dmex.exercise.Problem.number";

    private static final long serialVersionUID = 2195592764257517753L;

    private Properties properties;

    @Override
    public void init() throws ServletException {
        super.init();

        registerSessionAttributes(SESSION_ATTR_EXSESSION,
                                  SESSION_ATTR_PROBLEM,
                                  SESSION_ATTR_PROBLEM_ATTEMPTS);

        properties = loadProperties("ProblemServlet.properties");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        HttpSession session = req.getSession();

        String eid = extractId(req.getPathInfo());
        if (eid == null) {
            throw new InvalidRequestException("Missing exercise id");
        }

        ExerciseSession exsession = getExerciseSession(session);

        // If restarting, discard the existing session.
        if (exsession != null
                && "restart".equalsIgnoreCase(req.getParameter("action"))) {
            exsession = null;
            setAttr(session, SESSION_ATTR_EXSESSION, null);
        }

        Settings settings = (Settings)getAttr(session, SESSION_ATTR_SETTINGS);

        if (exsession != null) {
            // There is an existing session and this is a GET request.  We
            // have skipped or otherwise gone to the next problem in the
            // exsession.

            exsession.notifySkip();
            nextProblem(eid, exsession, session, req, resp);

        } else if (settings != null) {
            // There are settings so start a new exsession.  We have come from
            // the ConfigServlet or clicked a "restart" link.

            doExSession(eid, settings, session, req, resp);

        } else {
            throw new InvalidRequestException(
                    "Missing settings and exercise session");
        }
    }

    private void doExSession(String eid, Settings settings,
            HttpSession session, HttpServletRequest req,
            HttpServletResponse resp) throws IOException, ServletException {

        assert eid != null;
        assert settings != null;

        Exercise ex = getExercise(eid);
        if (ex == null) {
            throw new InvalidRequestException("Unknown exercise '" + eid + "'");
        }

        ExerciseSession exsession = ex.newSession(settings);
        setAttr(session, SESSION_ATTR_EXSESSION, exsession);
        setAttr(session, SESSION_ATTR_PROBLEM, null);

        // We don't remove the settings from the session so that we can
        // restart the exsession using the same settings by passing the
        // "action=restart" parameters to the /problem/eid URL.

        nextProblem(eid, exsession, session, req, resp);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        HttpSession session = req.getSession();

        String eid = extractId(req.getPathInfo());
        if (eid == null) {
            throw new InvalidRequestException("Missing exercise id");
        }

        ExerciseSession exsession = getExerciseSession(session);
        if (exsession == null) {
            throw new InvalidRequestException("Missing exercise session");
        }

        Problem problem = (Problem)getAttr(session, SESSION_ATTR_PROBLEM);
        if (problem != null) {
            int attempts = incrementAttempts(session);

            ProblemValidator validator =
                    ((FormExerciseList)getExerciseList())
                    .getProblemValidator(eid);

            if (validator != null) {
                ProblemAnswer answer = validator.validate(problem, req);

                if (answer.isCorrect()) {
                    // Include a report of the number of attempts made to
                    // complete a problem.
                    req.setAttribute(REQUEST_ATTR_PROBLEM_NUMBER,
                            Integer.valueOf(exsession.getCreated()));
                    req.setAttribute(SESSION_ATTR_PROBLEM_ATTEMPTS,
                            Integer.valueOf(attempts));

                    setAttr(session, SESSION_ATTR_PROBLEM_ATTEMPTS, null);

                    exsession.notifySubmit(true);
                } else {
                    exsession.notifySubmit(false);
                    renderProblem(eid, session, req, resp, problem, answer);
                    return;
                }
            }
        }

        nextProblem(eid, exsession, session, req, resp);
    }

    private void nextProblem(String eid, ExerciseSession exsession,
            HttpSession session, HttpServletRequest req,
            HttpServletResponse resp) throws IOException, ServletException {

        // Remove any existing problem and its attempt counter.
        setAttr(session, SESSION_ATTR_PROBLEM, null);
        setAttr(session, SESSION_ATTR_PROBLEM_ATTEMPTS, null);

        Problem problem = null;
        if (exsession.getCreated() < exsession.getProblemCount()) {
            problem = exsession.nextProblem();
        }

        if (problem == null) {
            resp.sendRedirect(resp.encodeRedirectURL(
                    getServletContext().getContextPath() + "/finish/" + eid));

        } else {
            renderProblem(eid, session, req, resp, problem, null);
        }
    }

    @SuppressWarnings("unchecked")
    private void renderProblem(String eid, HttpSession session,
            HttpServletRequest req, HttpServletResponse resp,
            Problem problem, ProblemAnswer answer)
        throws IOException, ServletException {

        assert problem != null;

        setAttr(session, SESSION_ATTR_PROBLEM, problem);

        if (answer != null && !answer.isCorrect()) {
            setErrors(req,
                    makeErrors(eid, answer.getErrorCodes(), req.getLocale()));

            req.setAttribute(REQUEST_ATTR_PROBLEM_ANSWER, answer);
        }

        req.setAttribute(REQUEST_ATTR_JAVASCRIPT_JQUERY, Boolean.TRUE);

        Collection<String> resources = getResources(problem,
                                        getServletContext().getContextPath());
        if (resources != null && resources.size() > 0) {
            req.setAttribute(REQUEST_ATTR_RESOURCES, resources);
        }

        req.getRequestDispatcher("/exsession.jsp").include(req, resp);
    }

    private Collection<String> makeErrors(String eid,
            Set<? extends ErrorCode> errors, Locale locale) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }

        Exercise ex = getExercise(eid);
        if (ex == null) {
            return null;
        }

        Collection<String> list = null;
        for (ErrorCode error : errors) {
            list = addError(list, ex.getMessage(locale, error.key()));
        }

        return list;
    }

    private Set<String> getResources(Problem problem, String rootPath) {
        if (problem != null) {
            String key = problem.getExerciseId() + ".problem.javascript";

            String value = properties.getProperty(key);
            if (value != null) {
                StringBuilder js = new StringBuilder();
                js.append("<script type=\"text/javascript\" src=\"")
                    .append(rootPath)
                    .append("/javascript/problem/")
                    .append(value)
                    .append("\"></script>");

                return Collections.singleton(js.toString());
            }
        }

        return null;
    }

    private ExerciseSession getExerciseSession(HttpSession session) {
        if (session != null) {
            return (ExerciseSession)getAttr(session, SESSION_ATTR_EXSESSION);
        }

        return null;
    }

    private int incrementAttempts(HttpSession session) {
        int attempts = 1;

        Object value = getAttr(session, SESSION_ATTR_PROBLEM_ATTEMPTS);
        if (value != null) {
            attempts += ((Integer)value).intValue();
        }

        setAttr(session, SESSION_ATTR_PROBLEM_ATTEMPTS,
                Integer.valueOf(attempts));

        return attempts;
    }

    private Properties loadProperties(String filename) throws ServletException {
        Properties p = new Properties();

        InputStream in = getClass().getResourceAsStream(filename);
        if (in != null) {
            try {
                p.load(in);
            } catch (IOException ioe) {
                throw new ServletException("Failed to load properties from "
                        + filename, ioe);
            } finally {
                closeQuietly(in);
            }
        }

        return p;
    }

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            // CHECKSTYLE:OFF
            } catch (IOException ioe) {}
            // CHECKSTYLE:ON
        }
    }
}
