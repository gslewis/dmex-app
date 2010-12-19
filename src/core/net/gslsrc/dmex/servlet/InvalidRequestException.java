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

import javax.servlet.ServletException;

/**
 * Indicates request or session parameters are incorrect or incomplete.
 * <p>
 * This exception provides a quick means of exiting processing and returning
 * to the home page if the request can't be handled.  The web.xml application
 * descriptor should be configured with an {@literal <error-page>} entry to
 * pass handling to the {@link ErrorHandler}.
 *
 * <blockquote><pre>{@code
 * <servlet>
 *    <servlet-name>error</servlet-name>
 *    <servlet-class>net.gslsrc.dmex.servlet.ErrorHandler</servlet-class>
 * </servlet>
 *
 * <servlet-mapping>
 *    <servlet-name>error</servlet-name>
 *    <url-pattern>/error/*</url-pattern>
 * </servlet-mapping>
 *
 * <error-page>
 *    <exception-type>
 *       net.gslsrc.dmex.servlet.InvalidRequestException
 *    </exception-type>
 *    <location>/error</location>
 * </error-page>
 * }</pre></blockquote>
 *
 * @author Geoff Lewis
 *
 * @see net.gslsrc.dmex.servlet.ErrorHandler
 */
public class InvalidRequestException extends ServletException {
    private static final long serialVersionUID = 7246865739476099927L;

    public InvalidRequestException() {}

    public InvalidRequestException(String msg) {
        super(msg);
    }

    public InvalidRequestException(Throwable cause) {
        super(cause);
    }

    public InvalidRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public String toString() {
        String s = "InvalidRequestException";

        if (getMessage() != null) {
            s += ": " + getMessage();
        }

        if (getCause() != null) {
            s += " - " + getCause();
        }

        return s;
    }
}
