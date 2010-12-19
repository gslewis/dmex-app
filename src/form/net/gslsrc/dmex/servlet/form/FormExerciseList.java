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
import net.gslsrc.dmex.servlet.ExerciseList;
import net.gslsrc.dmex.servlet.form.validate.ProblemValidator;

import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * Loads a list of {@link Exercise}s and their {@link ProblemValidator}s from
 * an XML configuration file.
 * <p>
 * This implementation extends the default {@link ExerciseList} XML format by
 * adding the optional {@code <validator>} child element to the
 * {@code <exercise>} element:
 *
 * <blockquote><pre>
 * {@code
 * <FormExerciseList>
 *    <exercise class="net.gslsrc.dmex.exercise.tables.TablesRevision">
 *       <validator>
 *    net.gslsrc.dmex.servlet.form.validate.impl.TablesRevisionProblemValidator
 *       </validator>
 *    </exercise>
 *    ...
 * </FormExerciseList>}
 * </pre></blockquote>
 *
 * @author Geoff Lewis
 */
public class FormExerciseList extends ExerciseList {

    private Map<String, ProblemValidator> validators;

    public FormExerciseList() {}

    public FormExerciseList(InputStream in) throws IOException {
        super(in);
    }

    /**
     * Associate {@link ProblemValidator}s with an {@link Exercise}.  Expects
     * the XML element name to be "validator" (case-insensitive).
     *
     * @param exercise the exercise being configured
     * @param element the element to be processed
     */
    protected void processElement(Exercise exercise, Element element) {
        if (exercise == null || element == null) {
            return;
        }

        if ("validator".equalsIgnoreCase(element.getTagName())) {
            try {
                ProblemValidator validator =
                        (ProblemValidator)makeObject(element);

                if (validators == null) {
                    validators = new LinkedHashMap<String, ProblemValidator>();
                }

                validators.put(exercise.getId(), validator);

            } catch (IllegalArgumentException iae) {
                System.err.println(
                        "Failed to create ProblemValidator - " + iae);
            }
        }
    }

    public ProblemValidator getProblemValidator(String id) {
        if (id != null && validators != null) {
            return validators.get(id);
        }

        return null;
    }
}
