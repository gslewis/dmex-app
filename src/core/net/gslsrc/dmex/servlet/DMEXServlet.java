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

package net.gslsrc.dmex.servlet;

import net.gslsrc.dmex.exercise.Exercise;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Basis for DMEX servlets.
 * <p>
 * If an implementation stores an attribute in the {@code HttpSession} it
 * should override {@code init()} and register the attributes via
 * {@link #registerSessionAttributes} so that {@link #clearSession} will
 * remove all exercise-related attributes.
 *
 * @author Geoff Lewis
 */
public abstract class DMEXServlet extends HttpServlet {

    // Implementation note: We need to store session attributes directly in
    // the session object, not in a single "store" map in the session, because
    // GAE doesn't work with the latter case.  This means we need to register
    // the DMEX attributes stored in the session so that we know which to
    // remove in clearSession().

    /**
     * Servlet context attribute that contains the {@link ExerciseList}.  This
     * attribute is read-only and it set by the webapp's
     * {@link DMEXContextListener} implementation.  As this attribute is used
     * by a {@code <jsp:useBean> tag, it can't contain periods.
     */
    public static final String CONTEXT_ATTR_EXLIST = "exlist";

    /**
     * Request attribute that contains a list of JavaScript or CSS resource
     * elements.  Each entry in the list should be a full HTML {@code <link>}
     * or {@code <script>} to be added in the {@code scripts.jsp} template.
     */
    public static final String REQUEST_ATTR_RESOURCES =
            "net.gslsrc.dmex.servlet.resources";

    /**
     * Request attribute that contains a boolean indicating whether to
     * include the jQuery JavaScript library.
     */
    public static final String REQUEST_ATTR_JAVASCRIPT_JQUERY =
            "net.gslsrc.dmex.servlet.javascript.jquery";

    /**
     * Servlet context attribute that points to the set of session attribute
     * names.  Attribute names are registered by servlets using
     * {@link #registerSessionAttributes}.  The set of attribute names is used
     * in {@link #clearSession} to remove the attribute contents.
     */
    private static final String CONTEXT_ATTR_SESSION_ATTRS =
            "net.gslsrc.dmex.servlet.DMEXServlet.sessionAttrs";

    /** Request attribute that contains an array of error messages. */
    private static final String REQUEST_ATTR_ERRORS =
            "net.gslsrc.dmex.servlet.errors";

    /**
     * Gets the {@link Exercise} for the given exercise id.  The exercise
     * instance is retrieved from the {@link ExerciseList} stored in the
     * servlet context under the {@link #CONTEXT_ATTR_EXLIST} attribute.
     *
     * @param eid the exercise id
     *
     * @return the exercise instance or null if no matching exercise
     *
     * @see ExerciseList#getExercise(String)
     */
    protected Exercise getExercise(String eid) {
        if (eid != null) {
            return ((ExerciseList)getServletContext()
                        .getAttribute(CONTEXT_ATTR_EXLIST)).getExercise(eid);
        }

        return null;
    }

    /**
     * Gets the {@link ExerciseList} stored in the servlet context.  Stored
     * under the attribute {@link #CONTEXT_ATTR_EXLIST}.
     *
     * @return the exercise list
     */
    protected ExerciseList getExerciseList() {
        return (ExerciseList)getServletContext()
                    .getAttribute(CONTEXT_ATTR_EXLIST);
    }

    protected void clearSession(HttpSession session) {
        if (session == null) {
            return;
        }

        for (String attrName : getSessionAttrs()) {
            session.removeAttribute(attrName);
        }
    }

    protected Object getAttr(HttpSession session, String attrName) {
        if (session == null || attrName == null) {
            return null;
        }

        return session.getAttribute(attrName);
    }

    protected void setAttr(HttpSession session, String attrName,
            Object attrValue) {
        if (session == null || attrName == null) {
            return;
        }

        if (attrValue != null) {
            session.setAttribute(attrName, attrValue);
        } else {
            session.removeAttribute(attrName);
        }
    }

    @SuppressWarnings("unchecked")
    protected final void registerSessionAttributes(String... attrNames) {
        if (attrNames == null || attrNames.length == 0) {
            return;
        }

        Set<String> set = (Set<String>)getServletContext()
                            .getAttribute(CONTEXT_ATTR_SESSION_ATTRS);

        if (set == null) {
            set = new HashSet<String>();
            getServletContext().setAttribute(CONTEXT_ATTR_SESSION_ATTRS, set);
        }

        for (String attrName : attrNames) {
            set.add(attrName);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> getSessionAttrs() {
        Set<String> set = (Set<String>)getServletContext()
                                .getAttribute(CONTEXT_ATTR_SESSION_ATTRS);

        if (set == null) {
            set = Collections.emptySet();
        }

        return set;
    }

    protected void setErrors(HttpServletRequest request, String... msgs) {
        if (request == null) {
            return;
        }

        if (msgs != null && msgs.length > 0) {
            request.setAttribute(REQUEST_ATTR_ERRORS, msgs);
        } else {
            request.removeAttribute(REQUEST_ATTR_ERRORS);
        }
    }

    protected void setErrors(HttpServletRequest request,
            Collection<String> msgs) {
        if (request == null) {
            return;
        }

        if (msgs == null || msgs.isEmpty()) {
            request.removeAttribute(REQUEST_ATTR_ERRORS);
        } else {
            request.setAttribute(REQUEST_ATTR_ERRORS,
                                 msgs.toArray(new String[msgs.size()]));
        }
    }

    protected Collection<String> addError(Collection<String> errors,
            String msg) {
        if (msg == null) {
            return errors;
        }

        Collection<String> list = errors;
        if (list == null) {
            list = new LinkedList<String>();
        }

        list.add(msg);

        return list;
    }

    /**
     * Extract the exercise id from the path.  We assume that the servlet
     * component of the path has been removed (ie., path obtained by the
     * servlet using {@code HttpServletRequest.getPathInfo()}).
     * <p>
     * Remove any leading '/' slashes and trim whitespace.  Extract string up
     * to any following '/' slash.
     *
     * @param path the path from which the id is extracted
     *
     * @return the id or null
     */
    public static String extractId(String path) {
        if (path == null) {
            return null;
        }

        String id = path.trim();
        while (!id.isEmpty() && id.startsWith("/")) {
            id = id.substring(1).trim();
        }

        if (id.isEmpty()) {
            return null;
        }

        int index = id.indexOf('/');
        if (index != -1) {
            id = id.substring(0, index);
        }

        return id.isEmpty() ? null : id.toLowerCase();
    }
}
