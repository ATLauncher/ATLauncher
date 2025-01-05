/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.themes;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Resources;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

@SuppressWarnings("serial")
public class ATLauncherLaf extends FlatLaf {
    public static ATLauncherLaf instance;

    private final String defaultFontName = "OpenSans-Regular";
    private final String defaultBoldFontName = "OpenSans-Bold";
    private final String consoleFontName = "OpenSans-Regular";
    private final String tabFontName = "Oswald-Regular";

    public static boolean install() {
        instance = new ATLauncherLaf();

        return setup(instance);
    }

    public static ATLauncherLaf getInstance() {
        return instance;
    }

    /**
     * If user has disabled custom fonts or is using a language without a font, then
     * we should use the base "sansserif" font to let the OS font take over.
     */
    private static boolean useBaseFont() {
        return App.settings.disableCustomFonts || Language.localesWithoutFont.contains(Language.selectedLocale);
    }

    /**
     * If user has disabled custom fonts or is using a language without a tab font,
     * then we should use the base "sansserif" font to let the OS font take over.
     */
    private static boolean useTabFont() {
        return App.settings.disableCustomFonts || Language.localesWithoutTabFont.contains(Language.selectedLocale);
    }

    public Font getNormalFont() {
        if (useBaseFont()) {
            return Resources.makeFont("sansserif").deriveFont(Font.PLAIN, 12f);
        } else {
            return Resources.makeFont(defaultFontName).deriveFont(Font.PLAIN, 12f);
        }
    }

    public Font getBoldFont() {
        if (useBaseFont()) {
            return Resources.makeFont("sansserif").deriveFont(Font.BOLD, 12f);
        } else {
            return Resources.makeFont(defaultFontName).deriveFont(Font.BOLD, 12f);
        }
    }

    public Font getConsoleFont() {
        if (useBaseFont()) {
            return Resources.makeFont("sansserif").deriveFont(Font.PLAIN, 12f);
        } else {
            return Resources.makeFont(consoleFontName).deriveFont(Font.PLAIN, 12f);
        }
    }

    public Font getTabFont() {
        if (useTabFont()) {
            return Resources.makeFont("sansserif").deriveFont(Font.PLAIN, 32f);
        } else {
            return Resources.makeFont(tabFontName).deriveFont(Font.PLAIN, 32f);
        }
    }

    public void registerFonts() {
        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Resources.makeFont(defaultFontName));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Resources.makeFont(defaultBoldFontName));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Resources.makeFont(consoleFontName));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Resources.makeFont(tabFontName));
        } catch (Throwable t) {
            LogManager.logStackTrace("Error registering fonts", t);
        }
    }

    @Override
    public String getName() {
        return "ATLauncher";
    }

    @Override
    public String getDescription() {
        return "Default theme of ATLauncher";
    }

    @Override
    public boolean isDark() {
        return true;
    }

    public boolean isIntelliJTheme() {
        return false;
    }

    @Override
    public List<Class<?>> getLafClassesForDefaultsLoading() {
        List<Class<?>> classes = new ArrayList<>();

        classes.add(FlatLaf.class); // FlatLaf class

        // Add the themes base dark/light class
        if (isDark()) {
            classes.add(FlatDarkLaf.class);

            if (isIntelliJTheme()) {
                classes.add(FlatDarculaLaf.class);
            }
        } else {
            classes.add(FlatLightLaf.class);

            if (isIntelliJTheme()) {
                classes.add(FlatIntelliJLaf.class);
            }
        }

        classes.add(ATLauncherLaf.class); // ATLauncher base class

        if (getClass().getSuperclass() != ATLauncherLaf.class) {
            classes.add(getClass().getSuperclass()); // Dark/Light ATLauncher base class
        }

        classes.add(getClass()); // Theme's class

        return classes;
    }

    public String getIconPath(String icon) {
        // check for a theme specific icon first
        String themeSpecificPath = "/assets/icon/" + (isDark() ? "dark" : "light") + "/" + icon + ".png";
        if (App.class.getResource(themeSpecificPath) != null) {
            return themeSpecificPath;
        }

        // if no theme specific icon, then return path to where a general one should be
        return "/assets/icon/" + icon + ".png";
    }

    public String getResourcePath(String path, String icon) {
        // check for a theme specific icon first
        String themeSpecificPath = "/assets/" + path + "/" + (isDark() ? "dark" : "light") + "/" + icon + ".png";
        if (App.class.getResource(themeSpecificPath) != null) {
            return themeSpecificPath;
        }

        // if no theme specific icon, then return path to where a general one should be
        return "/assets/" + path + "/" + icon + ".png";
    }

    public void updateUIFonts() {
        EventQueue.invokeLater(() -> {
            for (Window w : Window.getWindows()) {
                updateFontInComponentTree(w);
            }
        });
    }

    private void updateFontInComponentTree(Component c) {
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            JPopupMenu jpm = jc.getComponentPopupMenu();
            if (jpm != null) {
                updateFontInComponentTree(jpm);
            }
        }
        Component[] children = null;
        if (c instanceof JMenu) {
            children = ((JMenu) c).getMenuComponents();
        } else if (c instanceof Container) {
            children = ((Container) c).getComponents();
        }
        if (children != null) {
            for (Component child : children) {
                updateFontInComponentTree(child);
            }
        }

        Font f = c.getFont();
        if (f != null) {
            Font newFont = App.THEME.getNormalFont();

            if (f.isBold()) {
                newFont = App.THEME.getBoldFont();
            }

            if (f.getSize() == 32f) {
                newFont = App.THEME.getTabFont();
            } else if (f.getSize() == 17f) {
                newFont = App.THEME.getNormalFont().deriveFont(17.0F);
            } else {
                newFont = newFont.deriveFont(f.getSize());
            }

            c.setFont(newFont);
        }
    }
}
