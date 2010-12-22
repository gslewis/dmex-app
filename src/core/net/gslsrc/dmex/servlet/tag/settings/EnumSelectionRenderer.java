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

package net.gslsrc.dmex.servlet.tag.settings;

import net.gslsrc.dmex.exercise.Exercise;
import net.gslsrc.dmex.settings.EnumSelection;
import net.gslsrc.dmex.settings.Setting;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import javax.servlet.jsp.JspException;

/**
 * Renders an enum's values as a series of radio buttons.
 *
 * @author Geoff Lewis
 */
public class EnumSelectionRenderer extends SettingRenderer {

    public EnumSelectionRenderer() {}

    @Override
    public boolean renders(Class<? extends Setting> settingCls) {
        return EnumSelection.class.equals(settingCls);
    }

    @Override
    public String render(Exercise exercise, Setting setting, Locale locale)
        throws JspException {

        if (!(setting instanceof EnumSelection)) {
            throw new JspException(this + " setting not EnumSelection");
        }

        EnumSelection es = (EnumSelection)setting;

        StringBuilder sb = new StringBuilder();
        sb.append("<fieldset class='RadioEnumSelection'>");

        String title = exercise.getMessage(locale,
                                           "setting.title." + es.getId());
        sb.append("<legend>").append(title).append("</legend>");

        for (Enum e : es.getValues()) {
            sb.append("<span class='option'>");
            sb.append("<input type='radio' name='").append(es.getId())
                    .append("' value='").append(e.name()).append("'");

            if (e.equals(es.getSelection())) {
                sb.append(" checked='checked'");
            }

            String label = es.getMessage(locale, "label." + e.name(), e.name());
            sb.append("/>&nbsp;").append(label);
            sb.append("</span>");
        }

        sb.append("</fieldset>");

        return sb.toString();
    }

    @Override
    public Collection<String> getResources(Setting setting, String rootPath) {
        if (setting instanceof EnumSelection) {
            StringBuilder css = new StringBuilder();
            css.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"")
                .append(rootPath)
                .append("/style/RadioEnumSelection.css")
                .append("\"></link>");

            return Collections.singleton(css.toString());
        }

        return super.getResources(setting, rootPath);
    }
}
