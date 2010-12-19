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

import net.gslsrc.dmex.exercise.ExerciseSession;
import net.gslsrc.dmex.servlet.DMEXServlet;
import net.gslsrc.dmex.servlet.InvalidRequestException;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Presents the results of a finished (or aborted) {@link ExerciseSession}.
 * <p>
 * The presentation JSP is <code>finish.jsp</code>.  The web session is
 * cleared before presenting.
 *
 * @author Geoff Lewis
 */
public class FinishServlet extends DMEXServlet {
    private static final long serialVersionUID = 4439909601398844713L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        HttpSession session = req.getSession();

        ExerciseSession exsession = (ExerciseSession)getAttr(session,
                                        ProblemServlet.SESSION_ATTR_EXSESSION);
        if (exsession == null) {
            throw new InvalidRequestException("Missing exercise session");
        }

        // Close the session and calculate the elapsed time.
        exsession.close();

        clearSession(session);

        req.setAttribute(ProblemServlet.SESSION_ATTR_EXSESSION, exsession);
        req.getRequestDispatcher("/finish.jsp").include(req, resp);
    }
}
