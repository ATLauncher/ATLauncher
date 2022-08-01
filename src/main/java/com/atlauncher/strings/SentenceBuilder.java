package com.atlauncher.strings;

import com.atlauncher.data.Language;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlauncher.strings.SentenceBuilder.AltUsage.*;

public final class SentenceBuilder implements SentenceBuilderStub {
    static final Pattern formatPattern = Pattern.compile("(\\{(\\d+)})");
    static final Properties nouns = new Properties();
    static final Properties verbs = new Properties();
    static final Properties sentences = new Properties();
    private boolean capitalize = false;

    static {
        setLocale(Language.selectedLocale);
    }

    public static void setLocale(Locale locale) {
        //ResourceBundle.getBundle("assets/lang/Nouns.properties", locale);
        // cant use ResourceBundle because apparently it DOES NOT WORK, so we do it ourselves
        try {
            nouns.clear();
            verbs.clear();
            sentences.clear();

            nouns.load(ClassLoader.getSystemResourceAsStream("assets/lang/"+locale+"/Nouns.properties"));
            verbs.load(ClassLoader.getSystemResourceAsStream("assets/lang/"+locale+"/Verbs.properties"));
            sentences.load(ClassLoader.getSystemResourceAsStream("assets/lang/"+locale+"/Sentences.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load localization", e);
        }
    }

    public static void main(String[] args) {
        int n = 3;
        System.out.println(
            Sentence.BASE_ABC.capitalize()
                .insert(Verb.CHECK)
                .insert(Noun.MOD, n)
                .insert(Sentence.PRT_FOR_X.insert(Noun.UPDATE, n, PluralOnly))
                .append(Sentence.PRT_FROM_X.insert(Noun.CURSEFORGE))
                .append(". ", Sentence.PRT_PLEASE_WAIT.capitalize())
                .append("", "!")
        );
    }

    private final Sentence sentence;
    private final List<WordUsage> words = new ArrayList<>();
    private final List<CharSequence> append = new ArrayList<>();

    public SentenceBuilder(Sentence sentence) {
        this.sentence = sentence;
    }

    @Override
    public SentenceBuilder insert(Word word, int alt, AltUsage altUsage) {
        words.add(new WordUsage(word, alt, altUsage));
        return this;
    }

    @Override
    public SentenceBuilder insert(CharSequence seq) {
        words.add(new WordUsage(seq));
        return this;
    }

    @Override
    public SentenceBuilder append(CharSequence delimiter, CharSequence seq) {
        append.add("" + delimiter + seq);
        return this;
    }

    @Override
    public SentenceBuilder capitalize(int _alt) {
        capitalize = true;
        return this;
    }

    @NotNull
    @Override
    public String toString() {
        if (sentence.varCount() != words.size())
            throw new IllegalArgumentException("Invalid variable count for sentence " + sentence);
        String format = sentence.toString();
        Matcher matcher = formatPattern.matcher(format);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            int offset = format.indexOf(placeholder);
            int index = Integer.parseInt(matcher.group(2));
            format = format.substring(0, offset) + words.get(index) + format.substring(offset + placeholder.length());
        }
        if (capitalize)
            format = Capitalizable.capitalize(format);
        return format + String.join("", append);
    }

    public enum AltUsage {
        Default, NumberOnly, PluralOnly
    }

    private static class WordUsage {
        private final CharSequence seq;
        private final Word word;
        private final int alt;
        private final AltUsage altUsage;

        public WordUsage(CharSequence seq) {
            this.seq = seq;
            this.word = null;
            this.alt = 0;
            this.altUsage = Default;
        }

        public WordUsage(Word word, int alt, AltUsage altUsage) {
            this.seq = null;
            this.word = word;
            this.alt = alt;
            this.altUsage = altUsage;
        }

        @Override
        public String toString() {
            if (seq != null)
                return seq.toString();
            else if (word instanceof Noun && !((Noun) word).unique)
                switch (altUsage) {
                    default:
                        return String.format("%d %s", alt, word.toString(alt));
                    case NumberOnly:
                        return String.format("%d %s", alt, word.toString(1));
                    case PluralOnly:
                        return word.toString(alt);
                }
            else {
                assert word != null;
                return word.toString(alt);
            }
        }
    }
}
