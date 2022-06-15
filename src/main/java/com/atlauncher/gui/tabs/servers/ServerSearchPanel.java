package com.atlauncher.gui.tabs.servers;

import com.atlauncher.AppEventBus;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;

@Singleton
public final class ServerSearchPanel extends JPanel {
    private final ServerSearchField searchField = new ServerSearchField();
    private final JComboBox<ServerSortingStrategy> sortingStrategy = new JComboBox<>(ServerSortingStrategies.values());
    @Inject
    public ServerSearchPanel(){
        AppEventBus.registerToUIOnly(this);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.add(Box.createHorizontalGlue());
        this.add(this.searchField);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.sortingStrategy);

        this.sortingStrategy.setMaximumSize(new Dimension(190, 23));
    }
}