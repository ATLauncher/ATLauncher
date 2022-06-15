package com.atlauncher.gui.components.search;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.gui.card.Card;
import com.atlauncher.utils.SortingStrategy;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class SearchListPanel<T> extends JPanel {
    private final Map<T, Card> items = new HashMap<>();
    private SortingStrategy<T> sortingStrategy;

    protected SearchListPanel(final Set<T> initItems){
        this.setLayout(new GridBagLayout());
        if(initItems != null && !initItems.isEmpty()){
            initItems.forEach((item) -> this.items.putIfAbsent(item, createCardFor(item)));
            this.updateComponent();
        }
    }

    public final void addItem(@Nonnull final T item){
        Preconditions.checkNotNull(item);
        this.items.putIfAbsent(item, this.createCardFor(item));
        this.updateComponent();
    }

    public final void removeItem(@Nonnull final T item){
        Preconditions.checkNotNull(item);
        this.remove((JComponent) this.items.remove(item));
        this.updateComponent();
    }

    public boolean hasItems(){
        return this.items.size() > 0;
    }

    public final Optional<SortingStrategy<T>> getSortingStrategy(){
        return Optional.ofNullable(this.sortingStrategy);
    }

    protected abstract Card createNilCard();
    protected abstract Card createCardFor(final T value);
    protected abstract Predicate<Map.Entry<T, Card>> createSearchFilter(final Pattern searchPattern);

    private GridBagConstraints createConstraints(){
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS_SMALL;
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    protected final void updateComponent(@Nullable final Pattern searchPattern){
        this.removeAll();

        final GridBagConstraints gbc = this.createConstraints();
        if(!this.hasItems()) {
            this.add((JPanel) createNilCard(), gbc);
        } else{
            createServerStream(searchPattern)
                .forEach((e) -> {
                    this.add( (JComponent)e.getValue(), gbc);
                    gbc.gridy++;
                });
        }

        this.validate();
        this.repaint();
    }

    protected final void updateComponent(){
        this.updateComponent(null);
    }

    private Stream<Map.Entry<T, Card>> createServerStream(@Nullable final Pattern searchPattern){
        Stream<Map.Entry<T, Card>> stream = this.items.entrySet().stream();

        final Optional<SortingStrategy<T>> strategy = this.getSortingStrategy();
        if(strategy.isPresent())
            stream = stream.sorted(this.createSorter(strategy.get()));

        final Optional<Pattern> pattern = Optional.ofNullable(searchPattern);
        if(pattern.isPresent())
            stream = stream.filter(this.createSearchFilter(searchPattern));
        return stream;
    }

    private Comparator<Map.Entry<T, Card>> createSorter(final SortingStrategy<T> strategy){
        return (e1, e2) -> strategy.compare(e1.getKey(), e2.getKey());
    }
}