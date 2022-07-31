package com.atlauncher.strings;

public interface Capitalizable<T> {
    default T capitalize() {
        return capitalize(Word.DEFAULT_ALT);
    }

    T capitalize(int alt);

    static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
