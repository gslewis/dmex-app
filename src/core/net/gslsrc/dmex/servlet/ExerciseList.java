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

import java.io.Closeable;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A {@code ServletContext} resource that provides the list of
 * {@link Exercise}s used in the webapp.  The {@code ExerciseList} is created
 * and stored in the context by a {@link DMEXContextListener} implementation.
 * The plain {@code ExerciseList} maintains a map of exercises by id.
 * <p>
 * This class is used as a JSP bean ({@code <jsp:useBean>}) so needs to be
 * public and have a public zero-arg constructor.
 * <p>
 * The {@code ExerciseList} is configured from an XML resource.  The format
 * is:
 *
 * <blockquote><pre>
 * {@code
 * <ExerciseList>
 *    <exercise class="net.gslsrc.dmex.exercise.tables.TablesRevision"/>
 *    <exercise class="net.gslsrc.dmex.exercise.longmult.LongMultiplication"/>
 *    ...
 * </ExerciseList>}
 * </pre></blockquote>
 *
 * Implementations of the {@code ExerciseList} can associate additional
 * context resources with an exercise by adding child elements of an
 * {@code <exercise>} element and overriding the {@link #processElement}
 * method.
 *
 * @author Geoff Lewis
 *
 * @see net.gslsrc.dmex.servlet.DMEXContextListener
 */
public class ExerciseList {

    private Map<String, Exercise> exercises;

    public ExerciseList() {}

    public ExerciseList(InputStream in) throws IOException {
        try {
            loadExercises(in);
        } finally {
            closeQuietly(in);
        }
    }

    public Collection<Exercise> getExercises() {
        if (exercises != null) {
            return exercises.values();
        }

        return Collections.emptyList();
    }

    public Exercise getExercise(String id) {
        if (id != null && exercises != null) {
            return exercises.get(id);
        }

        return null;
    }

    public final void loadExercises(InputStream in) throws IOException {
        if (in == null) {
            return;
        }

        Document doc = getDocument(in);

        Node node = doc.getDocumentElement().getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("exercise".equalsIgnoreCase(node.getNodeName())) {
                    try {
                        addExercise((Element)node);
                    } catch (IllegalArgumentException iae) {
                        System.err.println(iae.getMessage());
                    }

                } else {
                    System.err.println(
                        "Unknown element <" + node.getNodeName() + ">");
                }
            }

            node = node.getNextSibling();
        }
    }

    /**
     * Process any additional elements in the belonging to the
     * {@code <exercise>}.  Implementations should override this method if
     * they associate additional information with an exercise.
     *
     * @param exercise the exercise to which the element belongs
     * @param element the element to be processed
     */
    protected void processElement(Exercise exercise, Element element) {}

    private void addExercise(Element element) {
        String clsname = getAttribute(element, "class");
        if (clsname == null) {
            throw new IllegalArgumentException(
                    "<exercise> missing class attribute");
        }

        Exercise ex = (Exercise)makeObject(clsname);

        Node node = element.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                processElement(ex, (Element)node);
            }

            node = node.getNextSibling();
        }

        if (exercises == null) {
            exercises = new LinkedHashMap<String, Exercise>();
        }

        exercises.put(ex.getId(), ex);
    }

    protected final Object makeObject(Element element) {
        assert element != null;

        String clsname = element.getTextContent();
        if (clsname != null) {
            clsname = clsname.trim();

            if (clsname.isEmpty()) {
                clsname = null;
            }
        }

        if (clsname == null) {
            throw new IllegalArgumentException("<" + element.getTagName()
                    + "> does not contain a class name");
        }

        return makeObject(clsname);
    }

    private Object makeObject(String clsname) {
        assert clsname != null;
        assert !clsname.isEmpty();

        try {
            return Class.forName(clsname).newInstance();

        // CHECKSTYLE:OFF
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to create object \"" + clsname + "\"", e);
        }
        // CHECKSTYLE:ON
    }

    private String getAttribute(Element element, String attrName) {
        if (element == null || attrName == null) {
            return null;
        }

        String value = element.getAttribute(attrName);
        if (value != null) {
            value = value.trim();
        }

        return value == null || value.isEmpty() ? null : value;
    }

    private static Document getDocument(InputStream in) throws IOException {
        assert in != null;

        try {
            return DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse(in);
        // CHECKSTYLE:OFF
        } catch (Exception e) {
            throw new IOException("Failed to parse configuration", e);
        }
        // CHECKSTYLE:ON
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
