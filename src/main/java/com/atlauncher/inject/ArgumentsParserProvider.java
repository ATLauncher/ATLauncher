package com.atlauncher.inject;

import joptsimple.OptionParser;

import javax.inject.Provider;
import java.util.Arrays;

public final class ArgumentsParserProvider implements Provider<OptionParser> {
    @Override
    public OptionParser get(){
        OptionParser parser = new OptionParser();
        parser.accepts("updated", "If the launcher was just updated.").withOptionalArg().ofType(Boolean.class);
        parser.accepts("skip-setup-dialog",
                "If the first time setup dialog should be skipped, using the defaults. Note that this will enable analytics by default.")
            .withOptionalArg().ofType(Boolean.class);
        parser.accepts("skip-tray-integration", "If the tray icon should not be enabled.").withOptionalArg()
            .ofType(Boolean.class);
        parser.accepts("disable-analytics", "If analytics should be disabled.").withOptionalArg().ofType(Boolean.class);
        parser.accepts("disable-error-reporting", "If error reporting should be disabled.").withOptionalArg()
            .ofType(Boolean.class);
        parser.accepts("working-dir", "This forces the working directory for the launcher.").withRequiredArg()
            .ofType(String.class);
        parser.accepts("base-launcher-domain", "The base launcher domain.").withRequiredArg().ofType(String.class);
        parser.accepts("base-cdn-domain", "The base CDN domain.").withRequiredArg().ofType(String.class);
        parser.accepts("base-cdn-path", "The path on the CDN used for downloading files.").withRequiredArg()
            .ofType(String.class);
        parser.accepts("allow-all-ssl-certs",
                "This will tell the launcher to allow all SSL certs regardless of validity. This is insecure and only intended for development purposes.")
            .withOptionalArg().ofType(Boolean.class);
        parser.accepts("no-launcher-update",
                "This forces the launcher to not check for a launcher update. It can be enabled with the below command line argument.")
            .withOptionalArg().ofType(Boolean.class);
        parser.accepts("no-console", "If the console shouldn't be shown.").withOptionalArg().ofType(Boolean.class);
        parser.accepts("close-launcher", "If the launcher should be closed after launching an instance.")
            .withOptionalArg().ofType(Boolean.class);
        parser.accepts("debug", "If debug logging should be enabled.").withOptionalArg().ofType(Boolean.class);
        parser.accepts("launch",
                "The name of an instance to automatically launch. Can be the instances directory name in the file system or the full name of the instance.")
            .withRequiredArg().ofType(String.class);
        parser.accepts("proxy-type", "The type of proxy to use. Can be \"SOCKS\", \"DIRECT\" or \"HTTP\".")
            .withRequiredArg().ofType(String.class);
        parser.accepts("proxy-host", "The host of the proxy to use.").withRequiredArg().ofType(String.class);
        parser.accepts("proxy-port", "The port of the proxy to use.").withRequiredArg().ofType(Integer.class);
        parser.accepts("config-override", "A JSON string to override the launchers config.").withRequiredArg()
            .ofType(String.class);
        parser.acceptsAll(Arrays.asList("help", "?"), "Shows help for the arguments for the application.").forHelp();
        return parser;
    }
}