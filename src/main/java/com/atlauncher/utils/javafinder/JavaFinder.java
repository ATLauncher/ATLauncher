/*
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 */
package com.atlauncher.utils.javafinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavaFinder {
    /**
     * @return: A list of javaExec paths found under this registry key (rooted at
     *          HKEY_LOCAL_MACHINE)
     * @param wow64     0 for standard registry access (32-bits for 32-bit app,
     *                  64-bits for 64-bits app) or WinRegistry.KEY_WOW64_32KEY to
     *                  force access to 32-bit registry view, or
     *                  WinRegistry.KEY_WOW64_64KEY to force access to 64-bit
     *                  registry view
     * @param previous: Insert all entries from this list at the beggining of the
     *                  results
     *************************************************************************/
    private static List<String> searchRegistry(String key, int wow64, List<String> previous) {
        List<String> result = previous;
        try {
            List<String> entries = WinRegistry.readStringSubKeys(WinRegistry.HKEY_LOCAL_MACHINE, key, wow64);
            for (int i = 0; entries != null && i < entries.size(); i++) {
                String val = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, key + "\\" + entries.get(i),
                        "JavaHome", wow64);
                if (!result.contains(val + "\\bin\\java.exe")) {
                    result.add(val + "\\bin\\java.exe");
                }
            }
        } catch (Throwable t) {}
        return result;
    }

    /**
     * @return: A list of JavaInfo with informations about all javas installed on
     *          this machine Searches and returns results in this order:
     *          HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment
     *          (32-bits view) HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime
     *          Environment (64-bits view) HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java
     *          Development Kit (32-bits view)
     *          HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit (64-bits
     *          view) WINDIR\system32 WINDIR\SysWOW64
     ****************************************************************************/
    public static List<JavaInfo> findJavas() {
        List<String> javaExecs = new ArrayList<String>();

        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment",
                WinRegistry.KEY_WOW64_32KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment",
                WinRegistry.KEY_WOW64_64KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit", WinRegistry.KEY_WOW64_32KEY,
                javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit", WinRegistry.KEY_WOW64_64KEY,
                javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\JDK", WinRegistry.KEY_WOW64_32KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\JDK", WinRegistry.KEY_WOW64_64KEY, javaExecs);

        javaExecs.add(System.getenv("WINDIR") + "\\system32\\java.exe");
        javaExecs.add(System.getenv("WINDIR") + "\\SysWOW64\\java.exe");

        List<JavaInfo> result = new ArrayList<JavaInfo>();
        for (String javaPath : javaExecs) {
            if (!(new File(javaPath).exists()))
                continue;
            result.add(new JavaInfo(javaPath));
        }
        return result;
    }

    /**
     * @return: The path to a java.exe that has the same bitness as the OS (or null
     *          if no matching java is found)
     ****************************************************************************/
    public static String getOSBitnessJava() {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        boolean isOS64 = arch.endsWith("64") || (wow64Arch != null && wow64Arch.endsWith("64"));

        List<JavaInfo> javas = JavaFinder.findJavas();
        for (int i = 0; i < javas.size(); i++) {
            if (javas.get(i).is64bits == isOS64)
                return javas.get(i).path;
        }
        return null;
    }
}
