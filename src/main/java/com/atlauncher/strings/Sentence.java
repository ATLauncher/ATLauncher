package com.atlauncher.strings;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public enum Sentence implements SentenceBuilderStub {
    BASE_A,
    BASE_AB,
    BASE_ABC,
    BASE_ABCD,

    ACT_KILL_X,
    ACT_TOGGLE_X,
    ACT_X_MANUALLY,

    INF_DONT_REMIND_AGAIN,
    @Deprecated
    INF_DOWNLOADING_X,
    @Deprecated
    INF_VISIT_X,
    @Deprecated
    INF_CHECKING_XY,
    @Deprecated
    INF_NO_X,
    @Deprecated
    INF_YOUR_XYZ,
    @Deprecated
    INF_X_AVAILABLE,

    PRT_X_TO_Y,
    PRT_THIS_X,
    PRT_FOR_X,
    PRT_ON_X,
    PRT_ALL_X,
    PRT_X_OF_Y,
    PRT_FROM_X,
    PRT_X_BY_Y,
    PRT_X_NOT_Y,
    PRT_PLEASE_WAIT,
    PRT_PLEASE_WAIT_LONG,

    ERR_BAD_INSTALL,
    ERR_CANNOT_WRITE_FILES,
    ERR_WRONG_JAVA_ARCHITECTURE,

    MSG_KILL_MINECRAFT,
    MSG_NO_SERVERS,
    MSG_NO_INSTANCES,
    MSG_MODS_NOT_AVAILABLE,
    MSG_MODS_NOT_DOWNLOADABLE,
    MSG_SAVE_FILE_TO,
    MSG_PACK_MAY_BREAK,
    MSG_BAD_INSTALL,
    MSG_CANNOT_WRITE_FILES,
    MSG_WRONG_JAVA_ARCHITECTURE,
    MSG_LAUNCHER_UPDATE_AVAILABLE,
    MSG_LAUNCHER_UPDATE_FAILED,
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
    public SentenceBuilder insert(CharSequence seq) {
        return new SentenceBuilder(this).insert(seq);
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
