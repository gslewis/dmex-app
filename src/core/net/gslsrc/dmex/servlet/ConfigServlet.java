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
import net.gslsrc.dmex.settings.Setting;
import net.gslsrc.dmex.settings.Settings;
import net.gslsrc.dmex.servlet.tag.settings.SettingRenderer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Configures an exercise session for a selected {@link Exercise}.  The
 * configuration page contains form elements for each {@link Setting} for the
 * {@code Exercise}.
 * <p>
 * The URL for this servlet is <code>/config/<i>exid</i></code>.  The id of
 * the selected exercise is extracted from the URL (see {@link #extractId}).
 * <p>
 * The servlet responds to GET requests by presenting the configuration form
 * and responds to POST requests by validating the configuration form.
 *
 * @author Geoff Lewis
 */
public class ConfigServlet extends DMEXServlet {

    /**
     * Session attribute that contains the current {@link Settings} object.
     */
    public static final String SESSION_ATTR_SETTINGS =
            "net.gslsrc.dmex.settings.Settings";

    private static final long serialVersionUID = -3369158027307856417L;

    @Override
    public void init() throws ServletException {
        super.init();

        registerSessionAttributes(SESSION_ATTR_SETTINGS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        HttpSession session = req.getSession();

        // A GET in the ConfigServlet always means we start a new exercise
        // session.  We would like to do this from the index.jsp page as well,
        // but it is a JSP servlet.
        clearSession(session);

        String eid = extractId(req.getPathInfo());
        if (eid == null) {
            throw new InvalidRequestException("Missing exercise id");
        }

        Exercise ex = getExercise(eid);
        if (ex == null) {
            throw new InvalidRequestException("Unknown exercise '" + eid + "'");
        }

        Settings settings = (Settings)getAttr(session, SESSION_ATTR_SETTINGS);
        if (settings == null || !settings.getExerciseId().equals(eid)) {
            settings = ex.newSettings();
            setAttr(session, SESSION_ATTR_SETTINGS, settings);
        }

        doConfig(eid, ex, settings, session, req, resp);
    }

    private void doConfig(String eid, Exercise ex, Settings settings,
            HttpSession session, HttpServletRequest req,
            HttpServletResponse resp) throws IOException, ServletException {

        assert eid != null;
        assert ex != null;
        assert settings != null;

        Set<String> scripts = getJavascript(settings);
        if (scripts.size() > 0) {
            req.setAttribute(REQUEST_ATTR_JAVASCRIPT, scripts);
        }

        req.getRequestDispatcher("/configex.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        HttpSession session = req.getSession();

        String eid = extractId(req.getPathInfo());
        if (eid == null) {
            throw new InvalidRequestException("Missing exercise id");
        }

        Exercise ex = getExercise(eid);
        if (ex == null) {
            throw new InvalidRequestException("Unknown exercise '" + eid + "'");
        }

        Settings settings = (Settings)getAttr(session, SESSION_ATTR_SETTINGS);
        if (settings == null || !settings.getExerciseId().equals(eid)) {
            throw new InvalidRequestException("Invalid settings: " + settings);
        }

        // Apply the user-provided parameter value to the corresponding
        // setting and collect and failures as error messages.
        Collection<String> errMsgs = null;
        for (Setting setting : settings) {
            try {
                setting.apply(req.getParameterValues(setting.getId()));
            } catch (IllegalArgumentException iae) {
                errMsgs = addError(errMsgs,
                        ex.getMessage(req.getLocale(),
                            iae.getMessage() + "." + setting.getId()));
                continue;
            }

            if (!setting.isSet()) {
                errMsgs = addError(errMsgs,
                        ex.getMessage(req.getLocale(),
                            "error.setting.unset." + setting.getId()));
            }
        }

        setAttr(session, SESSION_ATTR_SETTINGS, settings);

        if (errMsgs != null) {
            // There were errors in the config, return to the config page.
            setErrors(req, errMsgs);
            doConfig(eid, ex, settings, session, req, resp);

        } else {
            // The config is OK, redirect to the problem handler which should
            // start a new session using the settings.

            String path = getServletContext().getContextPath()
                    + "/problem/" + eid;

            resp.sendRedirect(resp.encodeRedirectURL(path));
        }
    }

    private Set<String> getJavascript(Settings settings) {
        Set<String> scripts = null;

        String root = getServletContext().getContextPath();

        for (Setting setting : settings) {
            SettingRenderer renderer = SettingRenderer.getRenderer(setting);
            if (renderer == null) {
                continue;
            }

            String script = renderer.getJavascript(setting);
            if (script != null) {
                if (scripts == null) {
                    scripts = new LinkedHashSet<String>();
                }

                scripts.add(root + script);
            }
        }

        if (scripts == null) {
            scripts = Collections.emptySet();
        }

        return scripts;
    }
}
