/*
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 */
package com.atlauncher.utils.javafinder;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;

public class JavaFinder {
    private static SoftReference<List<String>> javaPaths = new SoftReference<>(null);

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
        } catch (Throwable ignored) {
        }
        return result;
    }

    // this could probably be written better
    public static List<JavaInfo> findJavas() {
        List<String> javaExecs = javaPaths.get();

        if (javaExecs == null) {
            javaExecs = new ArrayList<>();

            if (OS.isWindows()) {
                javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment",
                        WinRegistry.KEY_WOW64_32KEY, javaExecs);
                javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment",
                        WinRegistry.KEY_WOW64_64KEY, javaExecs);
                javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit",
                        WinRegistry.KEY_WOW64_32KEY, javaExecs);
                javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit",
                        WinRegistry.KEY_WOW64_64KEY, javaExecs);
                javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\JDK", WinRegistry.KEY_WOW64_32KEY,
                        javaExecs);
                javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\JDK", WinRegistry.KEY_WOW64_64KEY,
                        javaExecs);

                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/bin/java.exe");

                String[] pathsToSearch = { "Java", "Amazon Corretto", "AdoptOpenJDK" };

                for (String searchPath : pathsToSearch) {
                    List<String> foundPaths = new ArrayList<>();

                    try {
                        Files.walkFileTree(Paths.get(System.getenv("programfiles"), searchPath),
                                EnumSet.noneOf(FileVisitOption.class), 10, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                                            throws IOException {
                                        if (pathMatcher.matches(path)) {
                                            foundPaths.add(path.toString());
                                        }

                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                    } catch (Exception ignored) {
                    }

                    if (foundPaths.size() != 0) {
                        javaExecs.addAll(foundPaths);
                    }
                }
            }

            javaPaths = new SoftReference<>(javaExecs);
        }

        return javaExecs.stream().distinct().filter(java -> Files.exists(Paths.get(java))).map(java -> {
            return new JavaInfo(java);
        }).collect(Collectors.toList());
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
        for (JavaInfo java : javas) {
            if (java.is64bits == isOS64)
                return java.path;
        }
        return null;
    }
}
