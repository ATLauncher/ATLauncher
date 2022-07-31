package com.atlauncher.strings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Word extends SubStr, Capitalizable<String> {
    int DEFAULT_ALT = -1;

    String toString(int n);
}
