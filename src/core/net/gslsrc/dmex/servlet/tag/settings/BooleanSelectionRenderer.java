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
import net.gslsrc.dmex.settings.BooleanSelection;
import net.gslsrc.dmex.settings.Setting;

import java.util.Locale;
import javax.servlet.jsp.JspException;

/**
 * Renders boolean values as a pair of radio buttons.  The labels of the radio
 * buttons are obtained from the exercise's resources using the keys:
 *
 * <blockquote><code>
 * setting.<i>setting_id</i>.true<br/>
 * setting.<i>setting_id</i>.false
 * </code></blockquote>
 *
 * where <code><i>setting_id</i></code> is the rendered setting's id from
 * {@link Setting#getId}.  Implementations can override {@link #getLabel} to
 * provide an alternative label.
 *
 * @author Geoff Lewis
 */
public class BooleanSelectionRenderer extends SettingRenderer {

    public BooleanSelectionRenderer() {}

    @Override
    public boolean renders(Class<? extends Setting> settingCls) {
        return BooleanSelection.class.equals(settingCls);
    }

    @Override
    public String render(Exercise exercise, Setting setting, Locale locale)
        throws JspException {

        if (!(setting instanceof BooleanSelection)) {
            throw new JspException(this + " setting not BooleanSelection");
        }

        BooleanSelection bs = (BooleanSelection)setting;

        StringBuilder sb = new StringBuilder();
        sb.append("<fieldset>");

        String title = exercise.getMessage(locale,
                                           "setting.title." + bs.getId());
        sb.append("<legend>").append(title).append("</legend>");

        sb.append(makeRadio(locale, exercise, bs, Boolean.TRUE));
        sb.append(makeRadio(locale, exercise, bs, Boolean.FALSE));

        sb.append("</fieldset>");

        return sb.toString();
    }

    private String makeRadio(Locale locale, Exercise exercise,
            BooleanSelection setting, Boolean value) {
        assert setting instanceof BooleanSelection;
        assert value != null;

        StringBuilder sb = new StringBuilder();

        sb.append("<input type='radio' name='").append(setting.getId());
        sb.append("' value='").append(value).append("'");

        if (value.equals(setting.getSelection())) {
            sb.append(" checked='checked'");
        }

        sb.append("/>&nbsp;");

        sb.append(getLabel(locale, exercise, setting, value));

        return sb.toString();
    }

    protected String getLabel(Locale locale, Exercise exercise,
            Setting setting, Boolean value) {
        String key = "setting." + setting.getId() + "." + value.toString();

        return exercise.getMessage(locale, key, value.toString());
    }
}
