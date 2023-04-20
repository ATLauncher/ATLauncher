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
package com.atlauncher.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.atlauncher.Network;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.utils.javafinder.JavaFinder;
import com.atlauncher.utils.javafinder.JavaInfo;

import okhttp3.tls.Certificates;

public class Java {
    /**
     * Get the Java version that the launcher runs on.
     *
     * @return the Java version that the launcher runs on
     */
    public static String getLauncherJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Checks if the Java being used is 64 bit.
     */
    public static boolean is64Bit() {
        return System.getProperty("sun.arch.data.model").contains("64");
    }

    public static String getVersionForJavaPath(File folder) {
        String executablePath = Java.getPathToJavaExecutable(folder.toPath());
        ProcessBuilder processBuilder = new ProcessBuilder(executablePath, "-version");
        processBuilder.directory(folder.getAbsoluteFile());
        processBuilder.redirectErrorStream(true);

        String version = "Unknown";

        try {
            Process process = processBuilder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                Pattern p = Pattern.compile("(java|openjdk) version \"([^\"]*)\"");

                while ((line = br.readLine()) != null) {
                    // Extract version information
                    Matcher m = p.matcher(line);

                    if (m.find()) {
                        version = m.group(2);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        LogManager.debug(String.format("Got version \"%s\" for Java at path \"%s\"", version, executablePath));

        if (version.equals("Unknown")) {
            LogManager.warn("Cannot get Java version from the output of \"" + folder.getAbsolutePath() + " -version\"");
        }

        return version;
    }

    /**
     * Parse a Java version string and get the major version number. For example
     * "1.8.0_91" is parsed to 8.
     *
     * @param version the version string to parse
     * @return the parsed major version number
     */
    public static int parseJavaVersionNumber(String version) {
        Matcher m = Pattern.compile("(?:1\\.)?([0-9]+).*").matcher(version);

        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    /**
     * Parse a Java build version string and get the major version number. For
     * example "1.8.0_91" is parsed to 91, 11.0.3_7 is parsed to 7 and 11.0.3+7 is
     * parsed to 7
     *
     * @param version the version string to parse
     * @return the parsed build number
     */
    public static int parseJavaBuildVersion(String version) {
        Matcher m = Pattern.compile(".*[_\\.]([0-9]+)").matcher(version);

        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }

        return 0;
    }

    /**
     * Get the major Java version that the launcher runs on.
     *
     * @return the major Java version that the launcher runs on
     */
    public static int getLauncherJavaVersionNumber() {
        return parseJavaVersionNumber(getLauncherJavaVersion());
    }

    public static boolean isSystemJavaNewerThanJava8() {
        return getLauncherJavaVersionNumber() >= 9;
    }

    /**
     * Checks whether Metaspace should be used instead of PermGen. This is the case
     * for Java 8 and above.
     *
     * @return whether Metaspace should be used instead of PermGen
     */
    public static boolean useMetaspace(String path) {
        String version = getVersionForJavaPath(new File(path));

        // if we fail to get the version, assume it's Java 8 or newer since it's more
        // likely these days
        if (version.equals("Unknown")) {
            return true;
        }

        return parseJavaVersionNumber(version) >= 8;
    }

    public static String getPathToSystemJavaExecutable() {
        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

        if (OS.isWindows()) {
            path += "w";
        }

        return path;
    }

    public static String getPathToJavaExecutable(Path root) {
        return root.resolve("bin/java" + (OS.isWindows() ? "w" : "")).toAbsolutePath().toString();
    }

    public static List<JavaInfo> getInstalledJavas() {
        PerformanceManager.start();
        List<JavaInfo> javas = JavaFinder.findJavas().stream()
                .filter(javaInfo -> javaInfo.majorVersion != null && javaInfo.minorVersion != null)
                .collect(Collectors.toList());

        JavaInfo systemJava = new JavaInfo(Java.getPathToSystemJavaExecutable());
        if (javas.size() == 0
                || javas.stream().noneMatch(java -> java.rootPath.equalsIgnoreCase(systemJava.rootPath))) {
            javas.add(systemJava);
        }

        PerformanceManager.end();
        return javas;
    }

    /**
     * Injects any needed SSL certificates.
     *
     * Modified from Minecraft Forge Installer.
     */
    public static void injectNeededCerts() {
        // Java 8 >= 141 supports ISRG Root X1 so no need to inject
        if (getLauncherJavaVersionNumber() == 8 && parseJavaBuildVersion(getLauncherJavaVersion()) < 141) {
            injectLetsEncryptCerts();
        }

        // Java 8 >= 91 supports DigiCert G2 so no need to inject
        if (getLauncherJavaVersionNumber() == 8 && parseJavaBuildVersion(getLauncherJavaVersion()) < 91) {
            injectDigiCertG2Certs();
        }
    }

    private static void injectLetsEncryptCerts() {
        LogManager.info("Injecting Lets Encrypt Certificate");
        Network.addTrustedCertificate(Certificates.decodeCertificatePem("-----BEGIN CERTIFICATE-----\n"
                + "MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw\n"
                + "TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh\n"
                + "cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4\n"
                + "WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu\n"
                + "ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY\n"
                + "MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc\n"
                + "h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+\n"
                + "0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U\n"
                + "A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW\n"
                + "T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH\n"
                + "B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC\n"
                + "B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv\n"
                + "KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWn\n"
                + "OlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTn\n"
                + "jh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbw\n"
                + "qHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CI\n"
                + "rU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV\n"
                + "HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq\n"
                + "hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL\n"
                + "ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ\n"
                + "3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK\n"
                + "NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5\n"
                + "ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur\n"
                + "TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC\n"
                + "jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc\n"
                + "oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq\n"
                + "4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA\n"
                + "mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d\n"
                + "emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=\n"
                + "-----END CERTIFICATE-----"));

        // almost everything goes through OkHttp, but until all does, we need this
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            final Path ksPath = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath), "changeit".toCharArray());
            Map<String, Certificate> jdkTrustStore = Collections.list(keyStore.aliases()).stream()
                    .collect(Collectors.toMap(a -> a, (String alias) -> {
                        try {
                            return keyStore.getCertificate(alias);
                        } catch (Exception e) {
                            LogManager.logStackTrace("Failed to get certificate", e);
                            return null;
                        }
                    }));

            KeyStore leKS = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream leKSFile = Utils.getResourceInputStream("/assets/certs/letsencrypt.jks");
            leKS.load(leKSFile, "notasecret".toCharArray());

            Map<String, Certificate> leTrustStore = Collections.list(leKS.aliases()).stream()
                    .collect(Collectors.toMap(a -> a, (String alias) -> {
                        try {
                            return leKS.getCertificate(alias);
                        } catch (KeyStoreException e) {
                            LogManager.logStackTrace("Failed to get certificate", e);
                            return null;
                        }
                    }));

            KeyStore mergedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            mergedTrustStore.load(null, new char[0]);
            for (final Map.Entry<String, Certificate> entry : jdkTrustStore.entrySet()) {
                mergedTrustStore.setCertificateEntry(entry.getKey(), entry.getValue());
            }
            for (final Map.Entry<String, Certificate> entry : leTrustStore.entrySet()) {
                mergedTrustStore.setCertificateEntry(entry.getKey(), entry.getValue());
            }

            TrustManagerFactory instance = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            instance.init(mergedTrustStore);
            SSLContext tls = SSLContext.getInstance("TLS");
            tls.init(null, instance.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(tls.getSocketFactory());
            LogManager.info("Injected new root certificates");
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to inject new root certificates. Problems might happen", e);
        }
    }

    private static void injectDigiCertG2Certs() {
        LogManager.info("Injecting DigiCert G2 Certificate");
        Network.addTrustedCertificate(Certificates.decodeCertificatePem("-----BEGIN CERTIFICATE-----\n"
                + "MIIDjjCCAnagAwIBAgIQAzrx5qcRqaC7KGSxHQn65TANBgkqhkiG9w0BAQsFADBh\n"
                + "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\n"
                + "d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBH\n"
                + "MjAeFw0xMzA4MDExMjAwMDBaFw0zODAxMTUxMjAwMDBaMGExCzAJBgNVBAYTAlVT\n"
                + "MRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5j\n"
                + "b20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IEcyMIIBIjANBgkqhkiG\n"
                + "9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzfNNNx7a8myaJCtSnX/RrohCgiN9RlUyfuI\n"
                + "2/Ou8jqJkTx65qsGGmvPrC3oXgkkRLpimn7Wo6h+4FR1IAWsULecYxpsMNzaHxmx\n"
                + "1x7e/dfgy5SDN67sH0NO3Xss0r0upS/kqbitOtSZpLYl6ZtrAGCSYP9PIUkY92eQ\n"
                + "q2EGnI/yuum06ZIya7XzV+hdG82MHauVBJVJ8zUtluNJbd134/tJS7SsVQepj5Wz\n"
                + "tCO7TG1F8PapspUwtP1MVYwnSlcUfIKdzXOS0xZKBgyMUNGPHgm+F6HmIcr9g+UQ\n"
                + "vIOlCsRnKPZzFBQ9RnbDhxSJITRNrw9FDKZJobq7nMWxM4MphQIDAQABo0IwQDAP\n"
                + "BgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUTiJUIBiV\n"
                + "5uNu5g/6+rkS7QYXjzkwDQYJKoZIhvcNAQELBQADggEBAGBnKJRvDkhj6zHd6mcY\n"
                + "1Yl9PMWLSn/pvtsrF9+wX3N3KjITOYFnQoQj8kVnNeyIv/iPsGEMNKSuIEyExtv4\n"
                + "NeF22d+mQrvHRAiGfzZ0JFrabA0UWTW98kndth/Jsw1HKj2ZL7tcu7XUIOGZX1NG\n"
                + "Fdtom/DzMNU+MeKNhJ7jitralj41E6Vf8PlwUHBHQRFXGU7Aj64GxJUTFy8bJZ91\n"
                + "8rGOmaFvE7FBcf6IKshPECBV1/MUReXgRPTqh5Uykw7+U0b6LJ3/iyK5S9kJRaTe\n"
                + "pLiaWN0bfVKfjllDiIGknibVb63dDcY3fe0Dkhvld1927jyNxF1WW6LZZm6zNTfl\n"
                + "MrY=\n"
                + "-----END CERTIFICATE-----"));
    }
}
