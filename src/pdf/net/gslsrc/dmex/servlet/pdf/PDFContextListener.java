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

import net.gslsrc.dmex.servlet.DMEXContextListener;
import net.gslsrc.dmex.servlet.ExerciseList;

import java.io.InputStream;
import java.io.IOException;

/**
 * PDF webapp context listener to handle servlet context life cycle.
 * <p>
 * On initialization, we create the {@link ExerciseList} to be used by the
 * application and store it in the servlet context.
 * <p>
 * This listener is registered in the application descriptor (web.xml) using:
 *
 * <blockquote><pre>
 * {@code
 * <listener>
 *    <listener-class>
 *       net.gslsrc.dmex.servlet.pdf.PDFContextListener
 *    </listener-class>
 * </listener>
 * }
 * </pre></blockquote>
 *
 * By default it uses the configuration
 * {@code /WEB-INF/classes/net/gslsrc/dmex/servlet/pdf/PDFExerciseList.xml}.
 * An alternate configuration resource can be set using the web.xml
 * {@code <context-param>} {@code net.gslsrc.dmex.servlet.ExerciseList} whose
 * value is the path to the configuration resource relative to the context
 * root.
 *
 * @author Geoff Lewis
 */
public class PDFContextListener extends DMEXContextListener {

    /**
     * The default {@link ExerciseList} configuration resource.  The
     * resource is found using
     * {@code getServletContext().getResourceAsStream()} so must be relative
     * to the context root.
     */
    private static final String DEFAULT_EXLIST_FILE =
            "/WEB-INF/classes/net/gslsrc/dmex/servlet/pdf/PDFExerciseList.xml";

    public PDFContextListener() {}

    @Override
    protected String getDefaultResource() {
        return DEFAULT_EXLIST_FILE;
    }

    @Override
    protected ExerciseList createExerciseList(InputStream in)
        throws IOException {

        if (in == null) {
            throw new NullPointerException("Configuration input is null");
        }

        return new ExerciseList(in);
    }
}
