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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Represents a {@link Demonstrator} script in JSON format.
 *
 * @author Geoff Lewis
 */
public class Script {

    private static final String NL = System.getProperty("line.separator");

    private ResourceBundle resources;
    private Collection<ScriptElement> elements;

    private boolean indent;

    public Script(ResourceBundle resources) {
        this.resources = resources;

        elements = new LinkedList<ScriptElement>();
    }

    public boolean isIndent() {
        return indent;
    }

    public Script setIndent(boolean indent) {
        this.indent = indent;

        return this;
    }

    public ScriptMap addMap() {
        ScriptMap map = new ScriptMap();
        elements.add(map);

        return map;
    }

    public ScriptList addList() {
        ScriptList list = new ScriptList();
        elements.add(list);

        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (ScriptElement element : elements) {
            if (sb.length() == 0) {
                sb.append("[").append(sep());
            } else {
                sb.append(", ").append(sep());
            }

            sb.append(element.toString(1));
        }

        if (sb.length() > 0) {
            sb.append(sep()).append("]");
        }

        return sb.toString();
    }

    private String getL10n(String key, Object... params) {
        String value = getMessage(key);

        if (value == null) {
            value = key;
        } else if (params != null && params.length > 0) {
            try {
                value = MessageFormat.format(value, params);
            } catch (IllegalArgumentException iae) {
                System.err.println("Invalid message format '" + value
                        + "' - " + iae);
            }
        }

        return value;
    }

    private String getMessage(String key) {
        String value = null;

        if (resources != null) {
            try {
                value = resources.getString(key);
            } catch (MissingResourceException mre) {
                System.err.println("Missing resource for '" + key + "'");
            }
        }

        return value;
    }

    private Locale getLocale() {
        return resources != null ? resources.getLocale() : Locale.getDefault();
    }

    private String sep() {
        return indent ? NL : "";
    }

    private String indentStr(int depth) {
        if (!indent || depth == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int spaces = depth * 2;
        for (int i = 0; i < spaces; ++i) {
            sb.append(' ');
        }

        return sb.toString();
    }

    private static String q(Object obj) {
        return "\"" + (obj != null ? obj.toString() : "") + "\"";
    }

    /**
     * Basis for elements of the {@code Script}.
     */
    public interface ScriptElement {
        /**
         * Provide a string representation of the element at the given indent
         * level.  If {@link #isIndent indenting} is off, the indent level is
         * ignored and the output is a continuous string.  If indenting is on,
         * the output may contain line breaks.
         *
         * @param indent the indent level
         *
         * @return the indented string representation of the element
         */
        String toString(int indent);
    }

    /**
     * Script list element.  See {@link #toString(int)} for a description of
     * the output.
     */
    public final class ScriptList implements ScriptElement {
        private List<Object> list;

        private ScriptList() {
            list = new LinkedList<Object>();
        }

        public ScriptList add(Object value) {
            if (value != null) {
                list.add(value);
            }

            return this;
        }

        public ScriptList addL10n(String value, Object... params) {
            return add(getL10n(value, params));
        }

        public ScriptList addList() {
            ScriptList sublist = new ScriptList();
            list.add(sublist);

            return sublist;
        }

        public ScriptMap addMap() {
            ScriptMap submap = new ScriptMap();
            list.add(submap);

            return submap;
        }

        @Override
        public String toString() {
            return toString(0);
        }

        /**
         * Provides a JSON-format string representation of this list element.
         * If {@link #isIndent} is true, the output will be indented with line
         * breaks.  Otherwise output is a continuous string.
         * <p>
         * The format is:
         *
         * <blockquote><pre>
         * [
         * <i>element1</i>,
         * <i>element2</i>,
         * ...
         * ]
         * </pre></blockquote>
         *
         * where element is typically a {@link ScriptMap}.
         *
         * @param indent the indent level
         *
         * @return the indented list string representation
         */
        @Override
        public String toString(int indent) {
            StringBuilder sb = new StringBuilder();

            String in = indentStr(indent);

            for (Object obj : list) {
                if (sb.length() == 0) {
                    sb.append(in).append("[").append(sep());
                } else {
                    sb.append(", ").append(sep());
                }

                if (obj instanceof ScriptElement) {
                    sb.append(((ScriptElement)obj).toString(indent + 1));
                } else {
                    sb.append(in).append(q(obj));
                }
            }

            if (sb.length() > 0) {
                sb.append(sep()).append(in).append("]");
            }

            return sb.toString();
        }
    }

    /**
     * Script map element.  See {@link #toString(int)} for description of
     * output.
     */
    public final class ScriptMap implements ScriptElement {
        private Map<String, Object> attributes;

        private ScriptMap() {
            attributes = new LinkedHashMap<String, Object>();
        }

        public ScriptMap addMap(String name) {
            ScriptMap submap = new ScriptMap();
            attributes.put(name, submap);

            return submap;
        }

        public ScriptList addList(String name) {
            ScriptList sublist = new ScriptList();
            attributes.put(name, sublist);

            return sublist;
        }

        public ScriptMap setL10n(String name, String value, Object... params) {
            return set(name, getL10n(value, params));
        }

        public ScriptMap set(String name, Object value) {
            attributes.put(name, value);

            return this;
        }

        @Override
        public String toString() {
            return toString(0);
        }

        /**
         * Provides a JSON-format string representation of this map.  If
         * {@link #isIndent} is true, the string will be indented with
         * line-breaks.  Otherwise it is a single line.
         * <p>
         * The format is:
         *
         * <blockquote><pre>
         * {
         * "key1": <i>value1</i>,
         * "key2": <i>value2</i>,
         * ...
         * }
         * </pre></blockquote>
         *
         * If <code><i>value</i></code> is a {@link ScriptElement}, its
         * {@link ScriptElement#toString(int) toString(int)} method is called
         * with the indent level incremented if indenting is on.  If
         * <code><i>value</i></code> is not a {@code ScriptElement}, it is
         * output as a quoted string.
         *
         * @param indent the indent level (ignored if indenting is off)
         *
         * @return an indented string representation of this script map
         *
         * @see Script#isIndent
         */
        @Override
        public String toString(int indent) {
            StringBuilder sb = new StringBuilder();

            String in = indentStr(indent);

            for (String key : attributes.keySet()) {
                if (sb.length() == 0) {
                    sb.append(in).append("{").append(sep());
                } else {
                    sb.append(",").append(sep());
                }

                sb.append(in).append(q(key)).append(": ");

                Object value = attributes.get(key);
                if (value instanceof ScriptElement) {
                    sb.append(sep());
                    sb.append(((ScriptElement)value).toString(indent + 1));
                } else {
                    sb.append(q(value));
                }
            }

            if (sb.length() > 0) {
                sb.append(sep()).append(in).append("}");
            }

            return sb.toString();
        }
    }
}
