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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Basis for tags that obtain an {@link Exercise} from a string id.  The
 * exercise instance is obtained from the {@link ExerciseList} which is stored
 * in the {@code ServletContext}.
 * <p>
 * Implementations must define a tag {@literal <attribute>} called "exercise"
 * of type {@code java.lang.String}.
 *
 * @author Geoff Lewis
 */
abstract class AbstractExerciseTag extends SimpleTagSupport {

    protected String exid;

    protected Exercise getExercise() throws JspException {
        if (exid == null) {
            throw new JspException("Missing exercise id");
        }

        ExerciseList list = (ExerciseList)((PageContext)getJspContext())
                .getServletContext().getAttribute("exlist");

        if (list == null) {
            throw new JspException("Cannot obtain exercise list");
        }

        Exercise ex = list.getExercise(exid);
        if (ex == null) {
            throw new JspException("No exercise for '" + exid + "'");
        }

        return ex;
    }

    public void setExercise(String exid) {
        this.exid = exid;
    }
}
