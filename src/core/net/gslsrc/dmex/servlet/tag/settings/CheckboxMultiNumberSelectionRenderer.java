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
import net.gslsrc.dmex.settings.MultiNumberSelection;
import net.gslsrc.dmex.settings.Setting;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.jsp.JspException;

/**
 * Renders a multi-number selection as a series of checkboxes.
 *
 * @author Geoff Lewis
 */
public class CheckboxMultiNumberSelectionRenderer extends SettingRenderer {

    public CheckboxMultiNumberSelectionRenderer() {}

    @Override
    public boolean renders(Class<? extends Setting> settingCls) {
        return MultiNumberSelection.class.equals(settingCls);
    }

    @Override
    public String render(Exercise exercise, Setting setting, Locale locale)
        throws JspException {

        if (!(setting instanceof MultiNumberSelection)) {
            throw new JspException(this + " setting not MultiNumberSelection");
        }

        MultiNumberSelection mns = (MultiNumberSelection)setting;

        StringBuilder sb = new StringBuilder();
        sb.append("<fieldset class='CheckboxMultiNumberSelection'>");

        String title = exercise.getMessage(locale,
                                           "setting.title." + mns.getId());
        sb.append("<legend>").append(title).append("</legend>");

        sb.append(makeCheckBox(mns.getId() + ".all",
                    exercise.getMessage(locale, "setting.checkbox.all", "All"),
                    false, "all"));

        int size = mns.getHighValue() - mns.getLowValue() + 1;
        for (int i = 0; i < size; ++i) {
            int value = mns.getLowValue() + i;
            sb.append(makeCheckBox(mns.getId(), String.valueOf(value),
                                   isSelected(mns, value)));
        }

        sb.append("</fieldset>");

        return sb.toString();
    }

    @Override
    public Collection<String> getResources(Setting setting, String rootPath) {
        if (setting instanceof MultiNumberSelection) {
            StringBuilder js = new StringBuilder();
            js.append("<script type=\"text/javascript\" src=\"")
                .append(rootPath)
                .append("/javascript/CheckboxMultiNumberSelection.js")
                .append("\"></script>");

            StringBuilder css = new StringBuilder();
            css.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"")
                .append(rootPath)
                .append("/style/CheckboxMultiNumberSelection.css")
                .append("\"></link>");

            return Arrays.asList(js.toString(), css.toString());
        }

        return super.getResources(setting, rootPath);
    }

    private String makeCheckBox(String name, String value, boolean checked,
            String... spanCls) {
        StringBuilder sb = new StringBuilder();

        sb.append("<span class='").append(makeSpanClass(spanCls)).append("'>");
        sb.append("<input type='checkbox' name='").append(name)
                .append("' value='").append(value).append("'");

        if (checked) {
            sb.append(" checked='checked'");
        }

        sb.append("/>&nbsp;").append(value);
        sb.append("</span>");

        return sb.toString();
    }

    private String makeSpanClass(String[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append("option");

        if (values != null && values.length > 0) {
            for (String value : values) {
                if (value != null) {
                    String s = value.trim();

                    if (!s.isEmpty()) {
                        sb.append(" ").append(s);
                    }
                }
            }
        }

        return sb.toString();
    }

    private boolean isSelected(MultiNumberSelection setting, int target) {
        for (int value : setting.getSelection()) {
            if (value == target) {
                return true;
            }
        }

        return false;
    }
}
