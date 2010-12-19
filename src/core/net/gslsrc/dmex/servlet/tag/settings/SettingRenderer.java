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
import net.gslsrc.dmex.settings.Setting;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Enumeration;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.servlet.jsp.JspException;

/**
 * Interface for renderers that produce form elements from {@link Setting}s.
 * Renderers can be identified by {@link Setting#getId setting id} or by
 * the setting's class.
 * <p>
 * To associate a renderer with a setting id, the id must be mapped to the
 * renderer class in a "SettingRenderer.properties" file (which must be
 * located in the "net/gslsrc/dmex/servlet/tag/settings" directory).  All
 * instances of this properties file on the classpath are loaded.  Once a
 * setting id "key" is mapped, it is not overridden and the order in which the
 * properties files are found is not guaranteed (depends on the ClassLoader)
 * so in general the mapping should occur only once.  Hence {@code Setting}
 * instances should use unique ids to avoid collisions, unless the intention
 * is to share a renderer.
 * <p>
 * To associate a renderer with a {@code Setting} class, the renderer must be
 * listed in the "SettingRenderer.providers" file (one class name per line).
 * (Note we can't use the {@code ServiceLoader} approach because the deployed
 * {@code /META-INF/services} directory in a webapp isn't on the classpath --
 * only stuff in {@code /WEB-INF/classes} and {@code /WEB-INF/lib/*.jar}.)
 * Each renderer provider is then tested against the {@code Setting} class
 * using {@link #renders(java.lang.Class)}.  Note that the per-class renderer
 * search is only used if the setting's id is not mapped to a renderer
 * instance.
 *
 * @author Geoff Lewis
 */
public abstract class SettingRenderer {

    private static final String PROPS_FILE =
            "net/gslsrc/dmex/servlet/tag/settings/SettingRenderer.properties";

    private static final String PROVIDERS_FILE = "SettingRenderer.providers";

    private static Collection<SettingRenderer> providers;

    private static Map<String, SettingRenderer> cache;

    protected SettingRenderer() {}

    public abstract boolean renders(Class<? extends Setting> settingCls);

    public abstract String render(Exercise exercise, Setting setting,
            Locale locale) throws JspException;

    /**
     * Gets the relative URI path to a Javascript file to be included in the
     * rendered output.  Implementations should override this method if they
     * have particular Javascript requirements.  Note that in the configex.jsp
     * page, the JQuery library will be included if any Javascript files are
     * returned by this method from any renderer.
     *
     * @return the javascript file to include or null for no javascript
     */
    public String getJavascript(Setting setting) {
        return null;
    }

    public static SettingRenderer getRenderer(Setting setting) {
        if (setting == null) {
            throw new NullPointerException("Setting is null");
        }

        SettingRenderer renderer = findRendererById(setting);
        if (renderer != null) {
            return renderer;
        }

        if (providers == null) {
            providers = loadProviders();
        }

        for (SettingRenderer r : providers) {
            if (r.renders(setting.getClass())) {
                return r;
            }
        }

        throw new IllegalArgumentException("Unsupported setting \""
                + setting.getClass().getName() + "\"");
    }

    private static Collection<SettingRenderer> loadProviders() {
        Collection<SettingRenderer> list = new LinkedList<SettingRenderer>();

        InputStream in =
                SettingRenderer.class.getResourceAsStream(PROVIDERS_FILE);
        if (in != null) {
            BufferedReader rin = null;
            try {
                rin = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = rin.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.charAt(0) == '#') {
                        continue;
                    }

                    SettingRenderer renderer = makeRenderer(line);
                    if (renderer != null) {
                        list.add(renderer);
                    }
                }
            } catch (IOException ioe) {
                System.err.println(
                        "SettingRenderer failed to read providers from "
                        + PROVIDERS_FILE + " - " + ioe);
            } finally {
                closeQuietly(rin);
                closeQuietly(in);
            }
        }

        return list;
    }

    private static SettingRenderer findRendererById(Setting setting) {
        assert setting != null;

        if (cache == null) {
            try {
                loadRenderers();
            } catch (IOException ioe) {
                System.err.println(
                        "SettingRenderer failed to load renderers - " + ioe);
            }
        }

        if (cache.containsKey(setting.getId())) {
            return cache.get(setting.getId());
        }

        return null;
    }

    private static void loadRenderers() throws IOException {
        cache = new HashMap<String, SettingRenderer>();

        // This is a map of class name to instance.  The key map (cache) looks
        // up the class name in this map so that we need only create one
        // instance of each renderer.
        Map<String, SettingRenderer> clsMap =
                new HashMap<String, SettingRenderer>();

        Enumeration<URL> urls = SettingRenderer.class.getClassLoader()
                                                .getResources(PROPS_FILE);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();

            try {
                loadRenderers(url, clsMap);
            } catch (IOException ioe) {
                System.err.println(
                        "SettingRenderer failed to load renders from "
                        + url + " - " + ioe);
            }
        }

        clsMap.clear();
    }

    private static void loadRenderers(URL url,
            Map<String, SettingRenderer> clsMap) throws IOException {
        Properties p = new Properties();

        InputStream in = null;
        try {
            in = url.openStream();
            p.load(in);
        } finally {
            closeQuietly(in);
        }

        for (String key : p.stringPropertyNames()) {
            if (cache.containsKey(key)) {
                continue;
            }

            String clsname = p.getProperty(key);
            if (clsname == null) {
                continue;
            }
            clsname = clsname.trim();

            SettingRenderer renderer = null;
            if (clsMap.containsKey(clsname)) {
                renderer = clsMap.get(clsname);
            } else {
                renderer = makeRenderer(clsname);
                // Cache null if an error occurs.
                clsMap.put(clsname, renderer);
            }

            cache.put(key, renderer);
        }
    }

    private static SettingRenderer makeRenderer(String clsname) {
        assert clsname != null;

        SettingRenderer renderer = null;
        try {
            renderer = (SettingRenderer)Class.forName(clsname).newInstance();
        // CHECKSTYLE:OFF
        } catch (Exception e) {
            System.err.println(
                    "SettingRenderer failed to create renderer \""
                    + clsname + "\" - " + e);
        }
        // CHECKSTYLE:ON

        return renderer;
    }

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            // CHECKSTYLE:OFF
            } catch (IOException ioe) {}
            // CHECKSTYLE:ON
        }
    }

}
