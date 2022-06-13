package com.atlauncher.gui.tabs.about;

import com.atlauncher.constants.Constants;
import com.atlauncher.utils.Java;
import org.jetbrains.annotations.NotNull;

/**
 * 13 / 06 / 2022
 */
public class AboutTabViewModel implements IAboutTabViewModel {
    private String info = null;

    @NotNull
    @Override
    public String[] getAuthors() {
        return AUTHORS_ARRAY;
    }

    /**
     * Optimization method to ensure about tab opens with information as fast
     * as possible.
     *
     * @return information on this launcher
     */
    @NotNull
    @Override
    public String getInfo() {
        if (info == null)
            info = Constants.LAUNCHER_NAME + "\n" +
                "Version:\t" + Constants.VERSION + "\n" +
                "OS:\t" + System.getProperty("os.name") + "\n" +
                "Java:\t" +
                String.format("Java %d (%s)",
                    Java.getLauncherJavaVersionNumber(),
                    Java.getLauncherJavaVersion()
                );

        return info;
    }

    /**
     * Produced via "git shortlog -s -n --all --no-merges" then some edits
     * <p>
     * Use the following pattern to retrieve icons
     * "https://avatars.githubusercontent.com/USERNAME"
     */
    @SuppressWarnings("JavadocLinkAsPlainText")
    private static final String[] AUTHORS_ARRAY = {
        "Ryan Dowling",
        "RyanTheAllmighty",
        "atlauncher-bot",
        "Asyncronous",
        "PORTB",
        "ATLauncher Bot",
        "doomsdayrs",
        "JakeJMattson",
        "Jamie (Lexteam)",
        "Ryan",
        "Jamie Mansfield",
        "flaw600",
        "s0cks",
        "Leah",
        "Alan Jenkins",
        "dgelessus",
        "Kihira",
        "Harald Krämer",
        "James Ross",
        "iarspider",
        "xz-dev",
        "Mysticpasta1",
        "Torsten Walluhn",
        "modmuss50",
        "Andrew Thurman",
        "Cassandra Caina",
        "Jamie (Lexware)",
        "Jowsey",
        "Shegorath123",
        "notfood",
        "Dallas Epperson",
        "Emma Waffle",
        "Hossam Mohsen",
        "Jamie",
        "Laceh",
        "Sasha Sorokin",
        "TecCheck",
        "Trejkaz",
        "mac",
    };
}
