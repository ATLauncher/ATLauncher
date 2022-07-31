package com.atlauncher.strings;

public interface SentenceBuilderStub extends SubStr, Capitalizable<SentenceBuilder> {
    default SentenceBuilder insert(Word word) {
        return insert(word, Word.DEFAULT_ALT);
    }

    default SentenceBuilder insert(Word word, int alt) {
        return insert(word, alt, SentenceBuilder.AltUsage.Default);
    }

    SentenceBuilder insert(Word word, int alt, SentenceBuilder.AltUsage altUsage);

    SentenceBuilder insert(String str);

    default SentenceBuilder append(CharSequence seq) {
        return append(" ", seq);
    }

    SentenceBuilder append(CharSequence delimiter, CharSequence seq);
}
