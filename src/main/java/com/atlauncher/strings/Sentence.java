package com.atlauncher.strings;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public enum Sentence implements SentenceBuilderStub {
    BASE_XY,
    BASE_XYZ,
    BASE_XYZW,

    INF_DONT_REMIND_AGAIN,
    INF_DOWNLOADING_X,
    INF_VISIT_X,
    INF_CHECKING_XY,
    INF_NO_X,
    INF_YOUR_XYZ,

    PRT_FOR_X,
    PRT_PLEASE_WAIT,

    ERR_WRONG_INSTALL_LOCATION,
    ERR_CANNOT_WRITE_FILES,
    ERR_WRONG_JAVA_ARCHITECTURE,
    ;

    //A_NEED_B_FROM_C("%s need %s from %s");//"{0} needs {1} from {2}");

    @NotNull
    @Override
    public String toString() {
        return SentenceBuilder.sentences.getProperty(name());
    }

    int varCount() {
        int c = 0;
        Matcher matcher = SentenceBuilder.formatPattern.matcher(toString());
        while (matcher.find())
            c++;
        return c;
    }

    @Override
    public SentenceBuilder insert(Word word, int alt, SentenceBuilder.AltUsage altUsage) {
        return new SentenceBuilder(this).insert(word, alt, altUsage);
    }

    @Override
    public SentenceBuilder insert(String str) {
        return new SentenceBuilder(this).insert(str);
    }

    @Override
    public SentenceBuilder append(CharSequence delimiter, CharSequence seq) {
        return new SentenceBuilder(this).append(delimiter, seq);
    }

    @Override
    public SentenceBuilder capitalize(int alt) {
        return new SentenceBuilder(this).capitalize(alt);
    }
}
