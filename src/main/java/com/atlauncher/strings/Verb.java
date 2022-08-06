package com.atlauncher.strings;

import org.intellij.lang.annotations.MagicConstant;

@SuppressWarnings("NullableProblems")
public enum Verb implements Word {
    UPDATE,
    CHANGE,
    CHECK,
    OPEN,
    CLOSE,
    COPY,
    FAIL,
    SUCCESS,
    SEARCH,
    SHOW,
    SORT,
    ADD,
    REMOVE,
    ENTER,
    GENERATE,
    DOWNLOAD,
    UPLOAD,
    CLEAR,
    KILL,
    EXTRACT,
    ORGANIZE,
    INSTALL,
    REINSTALL,
    CALCULATE,
    CREATE,
    SKIP,
    IMPORT,
    LOAD,
    PLAY
    ;

    public static final int FUTURE = -1;
    public static final int PRESENT = 0;
    public static final int PAST = 1;

    public String past() {
        return toString(PAST);
    }

    public String present() {
        return toString(PRESENT);
    }

    public String future() {
        return toString(FUTURE);
    }

    @Override
    public String toString(@MagicConstant(intValues = {FUTURE,PRESENT,PAST}) int n) {
        switch (n) {
            case PAST: return SentenceBuilder.verbs.getProperty(name()+"-past");
            case PRESENT: return SentenceBuilder.verbs.getProperty(name()+"-psnt");
            default: return SentenceBuilder.verbs.getProperty(name()+"-futr");
        }
    }

    @Override
    public String capitalize(@MagicConstant(intValues = {FUTURE,PRESENT,PAST}) int alt) {
        return Capitalizable.capitalize(toString(alt));
    }
}
