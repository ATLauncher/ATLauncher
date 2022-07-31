package com.atlauncher.strings;

@SuppressWarnings("NullableProblems")
public enum Noun implements Word {
    MOD,
    UPDATE,
    LAUNCHER,
    DL_PAGE(true),
    ERROR,
    USERNAME,
    CHANGE,

    HAS,
    HAS_NOT,

    ONEDRIVE(true),
    PROGRAM_FILES(true),

    CURSEFORGE(true),
    ;

    final boolean unique;

    Noun() {
        this(false);
    }

    Noun(boolean unique) {
        this.unique = unique;
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
