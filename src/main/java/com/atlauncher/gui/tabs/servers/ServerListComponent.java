package com.atlauncher.gui.tabs.servers;

import com.atlauncher.AppEventBus;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Server;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.ServerCard;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ServerListComponent extends JPanel {
    private static final Logger LOG = LogManager.getLogger(ServerListComponent.class);
    private final Map<Server, ServerCard> cards = new HashMap<>();

    public ServerListComponent(@Nullable final Set<Server> servers){
        AppEventBus.register(this);
        this.setLayout(new GridBagLayout());
        if(servers != null && !servers.isEmpty()) {
            servers.forEach((server) -> this.cards.putIfAbsent(server, new ServerCard(server)));
            this.updateComponent();
        }
    }

    public ServerListComponent(){
        this(new HashSet<>());
    }

    public void addServer(@Nonnull final Server server){
        Preconditions.checkNotNull(server);
        this.cards.putIfAbsent(server, new ServerCard(server));

        this.validate();
        this.repaint();
    }

    public void removeServer(@Nonnull final Server server){
        Preconditions.checkNotNull(server);
        final ServerCard card = this.cards.remove(server);
        this.remove(card);
        this.validate();
        this.repaint();
    }

    private boolean hasServers(){
        return this.cards.size() > 0;
    }

    private GridBagConstraints createConstraints(){
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS_SMALL;
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    private void updateComponent(@Nullable final Pattern searchPattern){
        this.removeAll();

        final GridBagConstraints gbc = this.createConstraints();
        if(!this.hasServers()) {
            this.add(createNilCard(), gbc);
        } else{
            createServerStream(this.cards, searchPattern)
                .forEach((e) -> {
                    this.add(e.getValue(), gbc);
                    gbc.gridy++;
                });
        }

        this.validate();
        this.repaint();
    }

    private void updateComponent(){
        this.updateComponent(null);
    }

    @Subscribe
    public void onServerSearch(final ServerSearchEvent event){
        LOG.info("searching {}", event.getSearchPattern().pattern());
        this.updateComponent(event.getSearchPattern());
    }

    private static Predicate<Map.Entry<Server, ServerCard>> createSearchFilter(final Pattern searchPattern){
        return (val) -> searchPattern.matcher(val.getKey().name).find();
    }

    private static Stream<Map.Entry<Server, ServerCard>> createServerStream(final Map<Server, ServerCard> cards, @Nullable final Pattern searchPattern){
        Stream<Map.Entry<Server, ServerCard>> stream = cards.entrySet().stream();
        if(searchPattern != null)
            stream = stream.filter(createSearchFilter(searchPattern));
        return stream;
    }

    private static NilCard createNilCard(){
        return new NilCard(GetText.tr("There are no servers to display.\n\nInstall one from the Packs tab."));
    }
}