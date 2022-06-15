package com.atlauncher.gui.components.search;

import com.atlauncher.utils.SortingStrategy;

import javax.swing.*;
import java.awt.*;

public abstract class SearchPanel<T, E> extends JPanel {
    private final SearchField<E> searchField;
    private final JComboBox<SortingStrategy<T>> sortingBox;

    protected SearchPanel(final SearchField<E> searchField,
                          final SortingStrategy<T>[] sortingStrategies) {
        this.searchField = searchField;
        this.sortingBox = new JComboBox<>(sortingStrategies);
        this.sortingBox.setMaximumSize(new Dimension(190, 23));

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(Box.createHorizontalGlue());
        this.add(this.searchField);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.sortingBox);
    }

//    private void addListeners() {
//        // action listeners
//        this.importButton.addActionListener((e) -> new ImportInstanceDialog());
//
//        // item listeners
//        this.sortingBox.addItemListener((e) -> {
//            if (e.getStateChange() == ItemEvent.SELECTED) {
//                this.parent.fireSortEvent(new InstancesSortEvent(e.getSource(), (InstanceSortingStrategy) e.getItem()));
//            }
//        });
//    }

//    @Override
//    public void onRelocalization() {
//        this.importButton.setText(GetText.tr("Import"));
//    }
}