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
import net.gslsrc.dmex.exercise.longdiv.LongDivisionProblem;
import net.gslsrc.dmex.servlet.form.demo.Script.ScriptList;
import net.gslsrc.dmex.servlet.form.demo.Script.ScriptMap;

import java.util.Locale;

/**
 * Produces a demonstration script for the {@link LongDivisionProblem}.
 *
 * @author Geoff Lewis
 */
public class LongDivisionDemonstrator extends Demonstrator {

    public LongDivisionDemonstrator() {}

    public String getScript(Problem problem, Locale locale) {
        if (problem == null) {
            throw new NullPointerException("Problem is null");
        }

        if (!(problem instanceof LongDivisionProblem)) {
            throw new IllegalArgumentException("Unsupported problem type "
                    + problem.getClass().getName());
        }

        Script script = new Script(getResources(locale));
        script.setIndent(true);

        script.addMap()
            .setL10n("text", "longdiv.dividend.text")
            .set("target", "td.dividend")
            .set("class", "demo-highlight-cell");

        script.addMap()
            .setL10n("text", "longdiv.divisor.text")
            .set("target", "td.divisor")
            .set("class", "demo-highlight-cell");

        script.addMap()
            .setL10n("text", "longdiv.quotient.text")
            .set("target", "tr.quotient td.blank input")
            .set("class", "demo-highlight-input");

        script.addMap()
            .setL10n("text", "longdiv.remainder.text")
            .set("target", "table.problem tr.subend:last td.blank input")
            .set("class", "demo-highlight-input");

        LongDivisionProblem prob = (LongDivisionProblem)problem;

        int quotient = prob.getQuotient();
        int qlength = String.valueOf(quotient).length();

        int remainder = solveStep(script, prob.getDivisor(),
                                  prob.getDividend(), prob.getDividend(),
                                  1, 0, 1, qlength);

        scriptQuotient(script, prob.getQuotient());
        scriptRemainder(script, remainder);

        return script.toString();
    }

    /**
     * Solve a single step of the problem.  A "step" is essentially the
     * calculation of a quotient digit.  This method recursively calls itself
     * to perform the next step until the running dividend is less than the
     * divisor, at which point it returns this final running dividend which is
     * the <i>remainder</i>.
     * <p>
     * We need to keep track of the working row (<code>wrow</code>) and the
     * quotient index.  Note that {@link LongDivisionProblem} does not include
     * a working row for quotient digits of zero so there is not necessarily a
     * correspondence between the working row and the quotient index (the
     * latter can get ahead of the former).
     * <p>
     * At the end of the step we determine the target dividend for the next
     * step.  We calculate "shift" for this step's part product and the target
     * dividend. "Shift" represents the number of missing digits from the
     * right, or corresponds to the exponent of the quotient digit's 10s
     * multiplier: the "ones" digit has a shift of zero, the "tens" digit has
     * a shift of 1, and so on.
     * <p>
     * Using the shifts, we calculate the "qdiff" which is the number of
     * quotient digits we advance on the next step.  If the qdiff is greater
     * than 1 then there are zeros in the quotient to be filled.  These are
     * either done at the start of the next step (see {@link #scriptZeros}) or
     * during {@link #scriptDividendDigits} while assembling the partial
     * dividend.
     *
     * @param script the script
     * @param divisor the problem divisor
     * @param dividend the full dividend for this step
     * @param tdiv the target dividend to be used (may discard trailing digits
     *             of the full dividend)
     * @param wrow the index of the working row (1-based, top-to-bottom)
     * @param lastq the index of the quotient digit (1-based, ltr) that
     *              represents the <i>previous</i> step
     * @param qdiff the advance to get to <i>this</i> step (number of quotient
     *              digits)
     * @param qlength the length of the quotient (hence the number of steps)
     *
     * @return the next dividend, which is the remainder on the last step
     */
    private int solveStep(Script script, int divisor, int dividend, int tdiv,
            int wrow, int lastq, int qdiff, int qlength) {
        // Current quotient step is the last solved step plus the "quotient
        // difference" (bigend-shift - subend shift) from last step.
        int step = lastq + qdiff;

        // Split the dividend into digits.
        char[] ddigits = String.valueOf(dividend).toCharArray();

        // Index of the i-th digit from the left that we test for
        // divisibility: we know it must be at least the width of the divisor.
        int dindex = String.valueOf(divisor).length() - 1;

        // Index of the tr.subend row that contains the dividend for this step
        // (for step > 1 as first step uses the original dividend).
        int sindex = wrow > 1 ? getSubendIndex(wrow - 1) : 0;

        // Quotient digit has been skipped AND initial partial dividend length
        // is the same as the target dividend length, THEN we need to fill in
        // the missing quotient zero here.
        if (qdiff > 1 && dindex == String.valueOf(tdiv).length() - 1) {
            scriptZeros(script, lastq, qdiff - 1);
        }

        scriptStartStep(script, divisor, tdiv, wrow);

        // The partial dividend and quotient digit (answer) for pdiv (if any).
        int pdiv = 0;
        int pqd = 0;
        while (dindex < ddigits.length) {
            pdiv = makeInt(ddigits, dindex);
            pqd = pdiv / divisor;

            // need to determine whether we have to enter a result for this
            // dindex.
            int qshift = qlength - step;
            int dshift = ddigits.length - dindex - 1;
            int qoffset = qdiff > 1 ? dshift - qshift : 0;

            scriptDividendDigits(script, divisor, tdiv, dindex, pdiv,
                                 pqd, sindex, step, qoffset);

            if (pqd == 0) {
                ++dindex;
            } else {
                break;
            }
        }

        scriptQuotientDigit(script, pqd, step);

        // Part product
        int pp = divisor * pqd;
        // "bigend" shift (zeros to the right of pp)
        int beshift = ddigits.length - dindex - 1;
        // Real part product (with trailing zeros)
        int realpp = (int)(pp * Math.pow(10, beshift));

        scriptPartProductDigits(script, divisor, pqd, pp, realpp, wrow);

        int[] results = scriptDividendRemain(script, divisor, dividend, realpp,
                                             wrow, step, qlength);

        // The next dividend: subend = dividend - realpp
        int subend = results[0];
        // The partial subend: the smalled fragment of the subend that is
        // larger than the divisor, hence the target dividend for the next
        // step.
        int psdiv = results[1];
        // The "shift" of the partial subend to the left: or the number of
        // trailing digits from the subend that are ignored.
        int seshift = results[2];

        if (subend < divisor) {
            return subend;
        }

        return solveStep(script, divisor, subend, psdiv, wrow + 1,
                step, beshift - seshift, qlength);
    }

    /**
     * Place zeros in the quotient at the <i>start</i> of a step.  This is
     * necessary when the following conditions occur:
     *
     * <ul>
     * <li>the previous step ended with a "qdiff" greater than 1 and,</li>
     * <li>the target dividend is the same length as the divisor.</li>
     * </ul>
     *
     * (If the target dividend is longer than the divisor, then the quotient
     * zero is handled by {@link #scriptDividendDigits} on its failed pass.)
     *
     * @param script the script
     * @param lastq the last quotient step completed
     * @param count the number of zeros to fill in
     */
    private void scriptZeros(Script script, int lastq, int count) {
        ScriptMap element = script.addMap();

        element.setL10n("text", "longdiv.solve.zeros", Integer.valueOf(count));

        ScriptList targets = element.addList("targets");
        ScriptList actions = element.addList("actions");

        for (int i = 1; i <= count; ++i) {
            String qname = "longdiv_answer[" + (lastq + i) + "]";
            String qtarget = "input[name='" + qname + "']";

            targets.addMap()
                .set("target", qtarget)
                .set("class", "demo-highlight-input-value");

            actions.addMap()
                .set("target", qtarget)
                .set("value", "0");
        }
    }

    private void scriptQuotient(Script script, int quotient) {
        script.addMap()
            .setL10n("text", "longdiv.solve.quotient",
                     Integer.valueOf(quotient))
            .set("target", "tr.quotient td.blank input")
            .set("class", "demo-highlight-input-value");
    }

    private void scriptRemainder(Script script, int remainder) {
        script.addMap()
            .setL10n("text", "longdiv.solve.remainder",
                     Integer.valueOf(remainder))
            .set("target", "tr.subend:last td.blank input")
            .set("class", "demo-highlight-input-value");
    }

    /**
     * Script the calculation of the dividend for the next step.  The process
     * is:
     *
     * <ul>
     * <li>Calculate the full dividend for the next step:
     * <code>div[n+1]&nbsp;=&nbsp;div[n]&nbsp;-&nbsp;prod[n]</code></li>
     * <li>Calculate the partial dividend for the next step: working
     * left-to-right, find the smallest combination of dividend digits that is
     * larger than the divisor.</li>
     * <li>Calculate the partial dividend left-shift: represents the number of
     * trailing digits of the full dividend that are ignored in the next
     * step.</li>
     * <li>If the partial dividend does not equal the full dividend, include a
     * message in the output.</li>
     * <li>If the full dividend is less than the divisor, we have
     * finished.</li>
     * <li>If we have finished but the current step (quotient digit) is not
     * the last digit in the quotient, then fill out the trailing zeros in the
     * quotient.</li>
     * </ul>
     *
     * @param script the script
     * @param divisor the problem divisor
     * @param prevDividend the previous full dividend
     * @param lastProd the product from the previous step
     * @param wrow the working row
     * @param step the step whose quotient digit we have just calculated
     * @param steps the number of steps (total number of quotient digits)
     *
     * @return an integer array containing: [0]= the full dividend for the
     *         next step, [1]= the partial dividend for the next step, and
     *         [2]= the left-shift of the partial dividend
     */
    private int[] scriptDividendRemain(Script script, int divisor,
            int prevDividend, int lastProd, int wrow, int step, int steps) {
        // The running dividend is obtained by subtracting the last product
        // (last quotient digit * divisor) from the previous dividend.
        int subend = prevDividend - lastProd;

        int psdiv = 0;
        int seshift = 0;

        ScriptMap element = script.addMap();

        ScriptList text = element.addList("text");
        ScriptList targets = element.addList("targets");
        ScriptList actions = element.addList("actions");

        text.addL10n("longdiv.solve.remdiv",
                Integer.valueOf(lastProd),
                Integer.valueOf(prevDividend),
                Integer.valueOf(subend));

        String stub = "longdiv_working[" + wrow + "][2]";

        char[] sdigits = String.valueOf(subend).toCharArray();
        for (int i = 0; i < sdigits.length; ++i) {
            seshift = sdigits.length - i - 1;

            String name = stub + "[" + (i + 1) + "]";
            String target = "input[name='" + name + "']";

            targets.addMap()
                .set("target", target)
                .set("class", "demo-highlight-input-value");

            actions.addMap()
                .set("target", target)
                .set("value", String.valueOf(sdigits[i]));

            psdiv = makeInt(sdigits, i);

            // If the partial dividend exceeds or equals the divisor and does
            // not represent the complete dividend (seshift > 0) then we can
            // use the partial dividend in the next step.
            if (psdiv >= divisor && i < (sdigits.length - 1)) {
                text.addL10n("longdiv.solve.remdiv.part",
                        Integer.valueOf(i + 1),
                        Integer.valueOf(subend),
                        Integer.valueOf(psdiv),
                        Integer.valueOf(divisor));

                break;
            }
        }

        if (subend < divisor) {
            text.addL10n("longdiv.solve.remdiv.finish",
                    Integer.valueOf(subend),
                    Integer.valueOf(divisor));

            // We have finished by there are still quotient digits remaining:
            // enter zeros for the remainder.
            if (step < steps) {
                text.addL10n("longdiv.solve.remdiv.finish.zeros",
                        Integer.valueOf(steps - step));

                for (int i = step + 1; i <= steps; ++i) {
                    String qname = "longdiv_answer[" + i + "]";
                    String qtarget = "input[name='" + qname + "']";

                    targets.addMap()
                        .set("target", qtarget)
                        .set("class", "demo-highlight-input-value");

                    actions.addMap()
                        .set("target", qtarget)
                        .set("value", "0");
                }
            }
        }

        return new int[] { subend, psdiv, seshift };
    }

    private void scriptPartProductDigits(Script script, int divisor, int pqd,
            int pp, int realpp, int wrow) {
        ScriptMap element = script.addMap();

        ScriptList text = element.addList("text");
        text.addL10n("longdiv.solve.partprod",
                Integer.valueOf(divisor),
                Integer.valueOf(pqd),
                Integer.valueOf(pp));

        if (realpp != pp) {
            text.addL10n("longdiv.solve.partprod.real",
                         Integer.valueOf(realpp));
        }

        ScriptList targets = element.addList("targets");
        ScriptList actions = element.addList("actions");

        String stub = "longdiv_working[" + wrow + "][1]";

        char[] digits = String.valueOf(pp).toCharArray();
        for (int i = 0; i < digits.length; ++i) {
            String name = stub + "[" + (i + 1) + "]";
            String target = "input[name='" + name + "']";

            targets.addMap()
                .set("target", target)
                .set("class", "demo-highlight-input-value");

            actions.addMap()
                .set("target", target)
                .set("value", String.valueOf(digits[i]));
        }
    }

    private void scriptQuotientDigit(Script script, int pqd, int qindex) {
        String target = "tr.quotient td.blank input[name='longdiv_answer["
                            + qindex + "]']";

        script.addMap()
            .setL10n("text", "longdiv.solve.quotient.digit",
                             Integer.valueOf(pqd))
            .set("target", target)
            .set("class", "demo-highlight-input-value")
            .set("value", String.valueOf(pqd));
    }

    /**
     * Report the divisibility status of a partial dividend.  We have
     * constructed a partial dividend from the target dividend and calculated
     * its quotient digit (<code>pqd</code>).  If the <code>pqd</code> is
     * zero, the partial dividend cannot be divided.
     * <p>
     * If the "qoffset" is greater than zero then a number of quotient digits
     * were skipped in the last step so should be filled with zeros when the
     * partial dividend cannot be divided.
     *
     * @param script the script
     * @param divisor the problem divisor
     * @param tdiv the target dividend
     * @param dindex the index of the right-most target dividend digit in the
     *               partial dividend
     * @param pdiv the partial dividend from the target dividend
     * @param pqd the quotient digit for this partial dividend
     * @param sindex the index of the {@literal <tr class="subend">}
     *               containing the dividend (if not step 1)
     *               (see {@link #getSubendIndex})
     * @param step the current step (quotient digit)
     * @param qoffset the number of quotient digits skipped in the last step
     */
    private void scriptDividendDigits(Script script, int divisor,
            int tdiv, int dindex, int pdiv, int pqd, int sindex, int step,
            int qoffset) {
        ScriptMap element = script.addMap();
        ScriptList text = element.addList("text")
            .addL10n("longdiv.solve.lookdiv.1",
                     Integer.valueOf(dindex + 1), Integer.valueOf(tdiv))
            .addL10n("longdiv.solve.lookdiv.2",
                     Integer.valueOf(pdiv), Integer.valueOf(divisor));

        boolean quotientZero = false;

        if (pqd == 0) {
            text.addL10n("longdiv.solve.lookdiv.no");

            // If we require an answer in the quotient on this pass, then
            // enter a zero.
            if (step > 1 && qoffset > 0) {
                text.addL10n("longdiv.solve.lookdiv.no.zero");
                quotientZero = true;
            }
        } else {
            text.addL10n("longdiv.solve.lookdiv.yes",
                    Integer.valueOf(pqd),
                    Integer.valueOf(divisor), Integer.valueOf(pdiv));
        }

        ScriptList targets = element.addList("targets");
        if (step == 1) {
            for (int i = 0; i <= dindex; ++i) {
                // The :nth-child selector is 1-based so add 1 to 'i'.
                // There are two preceding <td>s in the divisor <tr> so add
                // another 2.
                targets.addMap()
                    .set("target",
                         "tr.divisor td.dividend:nth-child(" + (i + 3) + ")")
                    .set("class", "demo-highlight-cell");
            }
        } else {
            String stub = "tr.subend:nth-child(" + sindex + ")";
            for (int i = 0; i <= dindex; ++i) {
                // The subend <tr> has one leading <td> with padding (or
                // remainder label), so add an extra 1.
                targets.addMap()
                    .set("target",
                         stub + " td.blank:nth-child(" + (i + 2) + ") input")
                    .set("class", "demo-highlight-input");
            }
        }

        if (quotientZero) {
            String qname = "longdiv_answer[" + (step - qoffset) + "]";
            String qtarget = "input[name='" + qname + "']";

            targets.addMap()
                .set("target", qtarget)
                .set("class", "demo-highlight-input-value");

            element.addList("actions").addMap()
                .set("target", qtarget)
                .set("value", "0");
        }
    }

    private void scriptStartStep(Script script, int divisor, int dividend,
            int wrow) {
        ScriptMap element = script.addMap()
            .setL10n("text", "longdiv.solve.dividing",
                     Integer.valueOf(dividend), Integer.valueOf(divisor));

        ScriptList list = element.addList("targets");
        list.addMap()
            .set("target", "tr.divisor td.divisor")
            .set("class", "demo-highlight-cell");

        if (wrow == 1) {
            list.addMap()
                .set("target", "tr.divisor td.dividend")
                .set("class", "demo-highlight-cell");
        } else {
            int sindex = getSubendIndex(wrow - 1);
            list.addMap()
                .set("target",
                     "tr.subend:nth-child(" + sindex + ") td.blank input")
                .set("class", "demo-highlight-input");
        }
    }

    // step=2 => wrow=1; step=3 => wrow=2; etc.
    // tbody is parent:
    //   1: tr.quotient
    //   2: tr.rule
    //   3: tr.divisor
    //   3*step + 1: tr.bigend
    //   3*step + 2: tr.rule
    //   3*step + 3: tr.subend
    // For step S, the subend of interest belongs to S-1
    // Child index of the step-th tr.subend.
    private int getSubendIndex(int step) {
        return 3 * step + 3;
    }

    /**
     * Combine digits to make a number.  We combine starting from the left
     * (zero-th) digit up to and including the i-th digit (given by the index
     * parameter).
     *
     * @param digits the complete array of digits
     * @param index the index of the i-th digit to be combined (right-most)
     *
     * @return the combined digits as an integer
     */
    private int makeInt(char[] digits, int index) {
        String s = "";
        for (int i = 0; i <= index; ++i) {
            s += digits[i];
        }

        return Integer.parseInt(s);
    }
}
