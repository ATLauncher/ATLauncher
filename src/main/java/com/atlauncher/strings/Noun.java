package com.atlauncher.strings;

@SuppressWarnings("NullableProblems")
public enum Noun implements Word {
    MOD,
    PACK,
    UPDATE,
    VERSION,
    CONFIG,
    MANIFEST,
    FILE,
    IMAGE,
    DEFINITION,
    RESOURCE,
    LIBRARY,
    CLIENT,
    RUNTIME,
    OVERRIDE,
    LAUNCHER,
    DESCRIPTION,
    DL_PAGE(true),
    ERROR,
    USERNAME,
    CHANGE,
    INSTANCE,
    SERVER,
    NEWS(true),
    NOTHING(true),
    CATEGORY,

    PACK_IDENTIFIER(true),

    ME,
    HAS,
    HAS_NOT,

    DIRECTORY,
    JAVA(true),
    ONEDRIVE(true),
    PROGRAM_FILES(true),

    VANILLA(true),
    MINECRAFT(true),
    CONSOLE(true),
    BROWSER(true),

    LOADER,
    FORGE(true),
    FABRIC(true),
    QUILT(true),

    ATLAUNCHER(true),
    CURSEFORGE(true),
    MODRINTH(true),
    MODPACKS_CH(true),
    TECHNIC_SOLDER(true),
    MULTIMC(true),

    // non-nouns
    AVAILABLE(true),
    LOGGING(true),
    OK(true),
    CANCEL(true),
    YES(true),
    NO(true),
    ;

    final boolean unique;

    Noun() {
        this(false);
    }

    Noun(boolean unique) {
        this.unique = unique;
    }

    public String singular() {
        return toString(1);
    }

    public String plural() {
        return toString(0);
    }

    public String toString(int n) {
        if (unique)
            return SentenceBuilder.nouns.getProperty(name());
        return n == 1
            ? SentenceBuilder.nouns.getProperty(name()+"-1")  // singular
            : SentenceBuilder.nouns.getProperty(name()+"-0"); // plural
    }

    @Override
    public String capitalize(int alt) {
        return Capitalizable.capitalize(toString(alt));
    }
}
