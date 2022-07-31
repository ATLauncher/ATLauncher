package com.atlauncher.strings;

import org.jetbrains.annotations.NotNull;

public interface SubStr extends CharSequence {
    @Override
    default int length() {
        return toString().length();
    }

    @Override
    default char charAt(int index) {
        return toString().charAt(index);
    }

    @NotNull
    @Override
    default CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }
}
