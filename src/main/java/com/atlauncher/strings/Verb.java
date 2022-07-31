package com.atlauncher.strings;

import org.intellij.lang.annotations.MagicConstant;

@SuppressWarnings("NullableProblems")
public enum Verb implements Word {
    UPDATE,
    CHANGE,
    ;

    public static final int PAST = -1;
    public static final int PRESENT = 0;
    public static final int FUTURE = 1;

    @Override
    public String toString(@MagicConstant(intValues = {FUTURE,PRESENT,PAST}) int n) {
        switch (n) {
            case -1: return SentenceBuilder.verbs.getProperty(name()+"-past");
            default: return SentenceBuilder.verbs.getProperty(name()+"-psnt");
            case 1: return SentenceBuilder.verbs.getProperty(name()+"-futr");
        }
    }

    @Override
    public String capitalize(@MagicConstant(intValues = {FUTURE,PRESENT,PAST}) int alt) {
        return Capitalizable.capitalize(toString(alt));
    }
}
