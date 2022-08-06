package com.atlauncher.strings;

public interface SentenceBuilderStub extends SubStr, Capitalizable<SentenceBuilder> {
    default SentenceBuilder insert(Word word) {
        return insert(word, Word.DEFAULT_ALT);
    }

    default SentenceBuilder insert(Word word, int alt) {
        return insert(word, alt, SentenceBuilder.AltUsage.Default);
    }

    SentenceBuilder insert(Word word, int alt, SentenceBuilder.AltUsage altUsage);

    SentenceBuilder insert(CharSequence seq);

    default SentenceBuilder append(CharSequence seq) {
        return append(seq.length() <= 3 ? "" : " ", seq);
    }

    SentenceBuilder append(CharSequence delimiter, CharSequence seq);
}
