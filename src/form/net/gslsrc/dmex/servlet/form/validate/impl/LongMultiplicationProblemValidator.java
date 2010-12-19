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
import net.gslsrc.dmex.exercise.longmult.LongMultiplicationProblem;
import net.gslsrc.dmex.exercise.longmult.LongMultiplicationProblemAnswer;
import net.gslsrc.dmex.servlet.form.validate.ProblemValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * Validates form inputs for the {@link LongMultiplicationProblem}.  The form
 * has been rendered using the "longmult_html_form.xsl" stylesheet.
 *
 * @author Geoff Lewis
 */
public class LongMultiplicationProblemValidator
    extends ProblemValidator<LongMultiplicationProblem> {

    private static final Integer ZERO = Integer.valueOf(0);

    public LongMultiplicationProblemValidator() {}

    @Override
    public ProblemAnswer<LongMultiplicationProblem> validate(
            LongMultiplicationProblem problem, HttpServletRequest req) {

        LongMultiplicationProblemAnswer answer =
                new LongMultiplicationProblemAnswer(problem);

        int[] rows = problem.getWorkingRows();
        for (int i = 0; i < rows.length; ++i) {
            String prefix = "longmult_working[" + (i + 1) + "]";

            char[] rowc = String.valueOf(rows[i]).toCharArray();
            // Exclude the "tens" least-sig-dig
            int nonZero = rowc.length - i;
            for (int j = 0; j < rowc.length; ++j) {
                if (j >= nonZero) {
                    // Can't enter trailing zeros, so just set answer.
                    answer.setWorking(i, j, ZERO);

                } else {
                    String key = prefix + "[" + (j + 1) + "]";

                    answer.setWorking(i, j, toInteger(req.getParameter(key)));
                }
            }
        }

        char[] answerc = String.valueOf(problem.getAnswer()).toCharArray();
        for (int i = 0; i < answerc.length; ++i) {
            String key = "longmult_answer[" + (i + 1) + "]";

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
