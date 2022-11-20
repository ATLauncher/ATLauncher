package com.atlauncher.viewmodel.base;

import com.atlauncher.data.Server;

import java.util.List;
import java.util.function.Consumer;

/**
 * 19 / 11 / 2022
 */
public interface IServersTabViewModel {
    /**
     * Whenever the view changes, this listener will be invoked with new data
     *
     * @param consumer listener to invoke
     */
    void addOnChangeViewListener(Consumer<List<Server>> consumer);

    void setSearch(String search);

    void addOnSearchChangeListener(Consumer<String> consumer);

}
