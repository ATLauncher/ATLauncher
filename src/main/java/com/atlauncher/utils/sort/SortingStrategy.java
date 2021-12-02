package com.atlauncher.utils.sort;

import java.util.Comparator;

public interface SortingStrategy<T> extends Comparator<T>{
    String getName();
}