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

import static net.gslsrc.dmex.servlet.DMEXServlet.CONTEXT_ATTR_EXLIST;

import java.io.InputStream;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Basis for custom listeners to handle servlet context life cycle.
 * <p>
 * On initialization, we create the {@link ExerciseList} to be used by the
 * application and store it in the servlet context.  The configuration
 * resource for the {@code ExerciseList} must be accessible as a servlet
 * resource using {@code ServletContext.getResourceAsStream()} so the path
 * must be relative to the context root.  The path must start with a '/'.
 * <p>
 * Implementations provide a default configuration resource using
 * {@link #getDefaultResource}.  This can be overridden by providing a
 * {@code <context-param>} in the web.xml application descriptor:
 *
 * <blockquote><pre>
 * {@code
 * <context-param>
 *    <param-name>net.gslsrc.dmex.servlet.ExerciseList</param-name>
 *    <param-value>/path/to/config/resource.xml</param-value>
 * </context-param>
 * }
 * </pre></blockquote>
 *
 * @author Geoff Lewis
 */
public abstract class DMEXContextListener implements ServletContextListener {

    /**
     * Parameter name for the {@link ExerciseList} configuration resource.
     * Used in a {@literal <context-param>} in the web.xml application
     * descriptor.
     */
    public static final String PARAM_EXLIST_FILE =
            "net.gslsrc.dmex.servlet.ExerciseList";

    /**
     * Gets the resource path for the default {@code ExerciseList}
     * configuration file.
     *
     * @return the default exercise list config resource path
     */
    protected abstract String getDefaultResource();

    /**
     * Creates the webapp's {@code ExerciseList} from an input stream.
     *
     * @param in the input stream for the configuration resource (XML)
     *
     * @return the exercise list
     *
     * @throws IOException if unable to create the exercise list from the input
     */
    protected abstract ExerciseList createExerciseList(InputStream in)
        throws IOException;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String resource =
                sce.getServletContext().getInitParameter(PARAM_EXLIST_FILE);

        if (resource == null) {
            resource = getDefaultResource();
        }

        if (resource == null) {
            throw new NullPointerException(
                    "No exercise list resource defined");
        }

        InputStream in = sce.getServletContext().getResourceAsStream(resource);
        if (in == null) {
            throw new NullPointerException(
                    "Failed to find exercise list resource '" + resource + "'");
        }

        try {
            sce.getServletContext().setAttribute(CONTEXT_ATTR_EXLIST,
                                                 createExerciseList(in));

        } catch (IOException ioe) {
            throw new RuntimeException("Failed to set exercise list", ioe);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(CONTEXT_ATTR_EXLIST);
    }
}
