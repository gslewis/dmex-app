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

package net.gslsrc.dmex.servlet.tag;

import net.gslsrc.dmex.exercise.Exercise;
import net.gslsrc.dmex.servlet.ExerciseList;
import static net.gslsrc.dmex.servlet.DMEXServlet.extractId;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Custom tag that supplies the URL to the localized info for a path.  The
 * path can point to two sources of information:
 *
 * <ul>
 * <li>{@link Exercise} help which is obtained from the exercise's
 * package (searches <code><i>ex_pkg</i>/help/<i>exid</i>.html</code>)</li>
 * <li>Other info as HTML fragments in the <code>/WEB-INF/info/</code>
 * directory.</li>
 * </ul>
 *
 * If the given path matches an exercise id, the first case is used,
 * otherwise the second case is attempted.
 * <p>
 * The info content is sought from the following files, in order:
 *
 * <ul>
 * <li><code><i>path</i>_<i>la</i>_<i>CO</i>.html</code></li>
 * <li><code><i>path</i>_<i>la</i>.html</code></li>
 * <li><code><i>path</i>.html</code></li>
 * </ul>
 *
 * where <code><i>la</i></code> is the language code and
 * <code><i>CO</i></code> is the country code of the given locale.
 * <p>
 * The content is then loaded in the <code>info.jsp</code> using
 * {@literal <c:import>}.
 *
 * @author Geoff Lewis
 */
public class InfoTag extends SimpleTagSupport {

    private String path;
    private Locale locale;

    @Override
    public void doTag() throws IOException, JspException {
        if (path == null) {
            throw new JspException("Path is null");
        }

        String id = extractId(path);
        if (id == null) {
            return;
        }

        ExerciseList list = (ExerciseList)((PageContext)getJspContext())
                                .getServletContext().getAttribute("exlist");
        if (list == null) {
            throw new JspException("Cannot obtain exercise list");
        }

        URL url = null;

        Exercise ex = list.getExercise(id);
        if (ex != null) {
            url = getExerciseInfo(ex);
        } else {
            url = getInfo(id);
        }

        if (url != null) {
            getJspContext().getOut().write(url.toString());
        }
    }

    private URL getInfo(String id) {
        assert id != null;

        String stub = "/WEB-INF/info/" + id;

        URL url = null;

        if (locale != null) {
            url = getResource(
                    makeName(stub, locale.getLanguage(), locale.getCountry()));

            if (url == null) {
                url = getResource(makeName(stub, locale.getLanguage()));
            }
        }

        if (url == null) {
            url = getResource(makeName(stub));
        }

        return url;
    }

    private URL getExerciseInfo(Exercise ex) {
        assert ex != null;

        String stub = "help/" + ex.getClass().getSimpleName();

        URL url = null;

        if (locale != null) {
            url = getResource(ex,
                    makeName(stub, locale.getLanguage(), locale.getCountry()));

            if (url == null) {
                url = getResource(ex, makeName(stub, locale.getLanguage()));
            }
        }

        if (url == null) {
            url = getResource(ex, makeName(stub));
        }

        return url;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private URL getResource(String path) {
        assert path != null;

        try {
            return ((PageContext)getJspContext()).getServletContext()
                        .getResource(path);
        } catch (MalformedURLException mue) {
            System.err.println("Invalid path " + path);
        }

        return null;
    }

    private URL getResource(Exercise ex, String path) {
        assert ex != null;
        assert path != null;

        return ex.getClass().getResource(path);
    }

    private String makeName(String... elements) {
        StringBuilder sb = new StringBuilder();

        for (String element : elements) {
            if (sb.length() > 0) {
                sb.append("_");
            }

            sb.append(element);
        }

        sb.append(".html");

        return sb.toString();
    }
}
