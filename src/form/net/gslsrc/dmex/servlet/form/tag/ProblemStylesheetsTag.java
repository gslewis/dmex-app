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

package net.gslsrc.dmex.servlet.form.tag;

import net.gslsrc.dmex.exercise.Problem;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Output stylesheet links for a given {@link Problem}.
 * <p>
 * Currently just produces one stylesheet based on the problem's class.
 *
 * @author Geoff Lewis
 */
public class ProblemStylesheetsTag extends SimpleTagSupport {

    private Problem problem;

    @Override
    public void doTag() throws IOException, JspException {
        if (problem == null) {
            throw new JspException("Problem is null");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<link type='text/css' rel='stylesheet' href='");

        sb.append(((PageContext)getJspContext()).getServletContext()
                            .getContextPath());
        sb.append("/style/");
        sb.append(problem.getClass().getSimpleName()).append(".css");

        sb.append("'></link>");

        getJspContext().getOut().println(sb.toString());
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }
}
