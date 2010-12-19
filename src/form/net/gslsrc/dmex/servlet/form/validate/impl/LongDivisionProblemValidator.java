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

package net.gslsrc.dmex.servlet.form.validate.impl;

import net.gslsrc.dmex.exercise.ProblemAnswer;
import net.gslsrc.dmex.exercise.longdiv.LongDivisionProblem;
import net.gslsrc.dmex.exercise.longdiv.LongDivisionProblem.WorkingRow;
import net.gslsrc.dmex.exercise.longdiv.LongDivisionProblemAnswer;
import net.gslsrc.dmex.servlet.form.validate.ProblemValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * Validates form inputs for the {@link LongDivisionProblem}.  The form
 * has been rendered using the "longdiv_html_form.xsl" stylesheet.
 *
 * @author Geoff Lewis
 */
public class LongDivisionProblemValidator
    extends ProblemValidator<LongDivisionProblem> {

    private static final Integer ZERO = Integer.valueOf(0);

    public LongDivisionProblemValidator() {}

    @Override
    public ProblemAnswer<LongDivisionProblem> validate(
            LongDivisionProblem problem, HttpServletRequest req) {

        LongDivisionProblemAnswer answer =
                new LongDivisionProblemAnswer(problem);

        WorkingRow[] rows = problem.getWorkingRows();
        for (int i = 0; i < rows.length; ++i) {
            String prefix = "longdiv_working[" + (i + 1) + "]";

            // Do bigend
            char[] bec = String.valueOf(rows[i].getBigEnd()).toCharArray();
            for (int j = 0; j < bec.length; ++j) {
                String key = prefix + "[1][" + (j + 1) + "]";

                answer.setBigEnd(i, j, toInteger(req.getParameter(key)));
            }

            // Do subend
            char[] sec = String.valueOf(rows[i].getSubEnd()).toCharArray();
            for (int j = 0; j < sec.length; ++j) {
                String key = prefix + "[2][" + (j + 1) + "]";

                answer.setSubEnd(i, j, toInteger(req.getParameter(key)));
            }
        }

        char[] qchars = String.valueOf(problem.getQuotient()).toCharArray();
        for (int i = 0; i < qchars.length; ++i) {
            String key = "longdiv_answer[" + (i + 1) + "]";

            answer.setAnswer(i, toInteger(req.getParameter(key)));
        }

        return answer;
    }

    private Integer toInteger(String value) {
        if (value == null) {
            return null;
        }

        String s = value.trim();
        if (!s.isEmpty()) {
            try {
                return Integer.valueOf(s);

            // CHECKSTYLE:OFF
            } catch (NumberFormatException nfe) {}
            // CHECKSTYLE:ON
        }

        return null;
    }
}
