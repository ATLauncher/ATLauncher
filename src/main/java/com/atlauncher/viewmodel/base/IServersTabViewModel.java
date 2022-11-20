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

    /**
     * Used to save the position the user was looking at
     *
     * @param position position the user was at
     */
    void setViewPosition(int position);

    /**
     * Watch when the position is changed, this is used to restore the view.
     * <p>
     * Note, that the consumer is only called
     *  after a change to search or servers.
     *
     * @param consumer consumer to take new scroll position
     */
    void addOnViewPositionChangedListener(Consumer<Integer> consumer);
}
