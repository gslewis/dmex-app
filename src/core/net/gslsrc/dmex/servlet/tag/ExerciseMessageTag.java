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

import java.io.IOException;
import java.util.Locale;
import javax.servlet.jsp.JspException;

/**
 * Custom tag that supplies a message from an {@link Exercise}.
 *
 * @author Geoff Lewis
 *
 * @see net.gslsrc.dmex.exercise.Exercise#getMessage(Locale,String,String)
 */
public class ExerciseMessageTag extends AbstractExerciseTag {

    private String key;
    private String defaultMsg;
    private Locale locale;

    @Override
    public void doTag() throws IOException, JspException {
        Exercise ex = getExercise();

        if (key == null) {
            throw new JspException("Key is null for " + ex.getId());
        }

        getJspContext().getOut().write(ex.getMessage(locale, key, defaultMsg));
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setDefault(String defaultMsg) {
        this.defaultMsg = defaultMsg;
    }
}
