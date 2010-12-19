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
import net.gslsrc.dmex.exercise.tables.TablesRevisionProblem;
import net.gslsrc.dmex.exercise.tables.TablesRevisionProblemAnswer;
import net.gslsrc.dmex.servlet.form.validate.ProblemValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * Validator for the {@link TablesRevisionProblem}.
 * <p>
 * Extracts the "tablesrev_answer" parameter from the request and, if it can
 * be converted to an integer, passes it to
 * {@link TablesRevisionProblem#isCorrect} which determines which problem
 * field it should be compared against.
 *
 * @author Geoff Lewis
 *
 * @see net.gslsrc.dmex.exercise.tables.TablesRevisionProblem#isCorrect
 */
public class TablesRevisionProblemValidator
    extends ProblemValidator<TablesRevisionProblem> {

    public TablesRevisionProblemValidator() {}

    @Override
    public ProblemAnswer<TablesRevisionProblem> validate(
            TablesRevisionProblem problem, HttpServletRequest req) {

        TablesRevisionProblemAnswer answer =
                new TablesRevisionProblemAnswer(problem);

        String value = req.getParameter("tablesrev_answer");
        if (value == null || value.isEmpty()) {
            answer.setErrorType(TablesRevisionProblemAnswer.ErrorType.EMPTY);

        } else {
            try {
                answer.setAnswer(Integer.valueOf(value));
            } catch (NumberFormatException fe) {
                answer.setErrorType(
                        TablesRevisionProblemAnswer.ErrorType.NON_NUMBER);
            }
        }

        return answer;
    }
}