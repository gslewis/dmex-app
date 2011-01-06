/*
Copyright (c) 2011 Geoff Lewis <gsl@gslsrc.net>

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

package net.gslsrc.dmex.servlet.form.demo;

import net.gslsrc.dmex.exercise.Problem;

import java.io.Closeable;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * Produces a JSON-format demonstration script for a {@link Problem} instance.
 * The script can be constructed using a {@link Script} object.  When the
 * script elements have been assembled, the complete script can be rendered as
 * a JSON string using {@link Script#toString()}.
 * <p>
 * A script is an <b>array</b> of "steps".
 * <p>
 * Each "step" element is a <b>map</b> of instructions: "text" to be
 * displayed, "target" identifying the form components associated with the
 * step, "class" listing the class(es) to be applied to the target components,
 * and "value" to be set as the {@code <input>} attribute value of the target
 * components.
 *
 * <h3>Text</h3>
 *
 * The text property may be a single string or an array of strings.  If an
 * array, each element string is placed in its own {@code <p>} paragraph.
 *
 * <blockquote><pre>
 * { "text": "description of the step", ... },
 * </pre></blockquote>
 *
 * or
 *
 * <blockquote><pre>
 * { "text":
 *   [ "first para",
 *     "second para",
 *     "third para" ],
 *   ...
 * }
 * </pre></blockquote>
 *
 * The text can be localised using the demonstrator-specific
 * {@link #getResources ResourceBundle}.  See the {@link Script} class.
 *
 * <h3>Target &amp; class</h3>
 *
 * A target expects (but does not require) an asociated "class" property.  (A
 * "class" property does nothing without a "target".)
 * <p>
 * If a step has a single target, use the "target" string property:
 *
 * <blockquote><pre>
 * { "target": "tr.divisor td.dividend",
 *   "class": "demo-highlight-cell",
 *   ...
 * },
 * </pre></blockquote>
 *
 * If a step has multiple targets, use the "targets" array property, in which
 * each element is a single target/class mapping.
 *
 * <blockquote><pre>
 * { "targets":
 *   [
 *     { "target": "target selector 1",
 *       "class": "highlight-class-1" },
 *     { "target": "target selector 2",
 *       "class": "highlight-class-2" },
 *   ],
 * },
 * </pre></blockquote>
 *
 * <h3>Action &amp; value</h3>
 *
 * If a step needs to set a form {@code <input>} value, use an action.  If you
 * only need to set one value, you can use the "value" property in conjunction
 * with the "target" property:
 *
 * <blockquote><pre>
 * { "text": "Put 7 in the quotient.",
 *   "target": "tr.quotient td.blank:nth-child(2) input",
 *   "class": "demo-highlight-input-digit",
 *   "value": "7" },
 * </pre></blockquote>
 *
 * If a step needs to set multiple values, use the "actions" array property in
 * which each element is a single target/value mapping.
 *
 * <blockquote><pre>
 * { "actions":
 *   [
 *     { "target": "target selector 1",
 *       "value": "1" },
 *     { "target": "target selector 2",
 *       "value": "2" },
 *   ],
 * },
 * </pre></blockquote>
 *
 * @author Geoff Lewis
 */
public abstract class Demonstrator implements Serializable {

    private static final String PROPS_FILE = "Demonstrator.properties";

    private static Map<String, Demonstrator> demonstrators;

    public Demonstrator() {}

    /**
     * Get the problem demonstration script as a JSON-format string.
     *
     * @return the demo script in JSON format
     */
    public abstract String getScript(Problem problem, Locale locale);

    protected ResourceBundle getResources(Locale locale) {
        ResourceBundle rb = null;
        try {
            rb = ResourceBundle.getBundle(getClass().getName(),
                    locale != null ? locale : Locale.getDefault());

        } catch (MissingResourceException mre) {
            // XXX trace-level log message
            System.err.println("Missing resources for " + getClass().getName());
        }

        return rb;
    }

    public static Demonstrator getDemonstrator(Problem problem) {
        if (problem == null) {
            return null;
        }

        if (demonstrators == null) {
            loadDemonstrators();
        }

        return demonstrators.get(problem.getExerciseId());
    }

    private static void loadDemonstrators() {
        demonstrators = new HashMap<String, Demonstrator>();

        Properties p = getProperties();

        for (String name : p.stringPropertyNames()) {
            String value = p.getProperty(name);

            String clsname = value;
            if (clsname.indexOf('.') == -1) {
                clsname = Demonstrator.class.getPackage().getName()
                            + "." + clsname;
            }

            Demonstrator demo = null;
            try {
                demo = (Demonstrator)Class.forName(clsname).newInstance();

            // CHECKSTYLE:OFF
            } catch (Exception e) {
                System.err.println("Failed to create demonstrator '" + value
                    + "' for '" + name + "' - " + e);
            }
            // CHECKSTYLE:ON

            if (demo != null) {
                demonstrators.put(name, demo);
            }
        }
    }

    private static Properties getProperties() {
        Properties p = new Properties();

        InputStream in = Demonstrator.class.getResourceAsStream(PROPS_FILE);
        if (in != null) {
            try {
                p.load(in);
            } catch (IOException ioe) {
                System.err.println("Failed to load properties from "
                        + PROPS_FILE + " - " + ioe);
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
