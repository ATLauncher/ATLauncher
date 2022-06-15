package com.atlauncher.gui.tabs.servers;

import com.atlauncher.AppEventBus;
import com.atlauncher.data.Server;
import com.atlauncher.events.servers.ServerAddedEvent;
import com.atlauncher.events.servers.ServerRemovedEvent;
import com.atlauncher.gui.card.Card;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.ServerCard;
import com.atlauncher.gui.components.search.SearchListPanel;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class ServerListPanel extends SearchListPanel<Server>{
    private static final Logger LOG = LogManager.getLogger(ServerListPanel.class);

    public ServerListPanel(@Nullable final Set<Server> servers){
        super(servers);
        AppEventBus.registerToUIOnly(this);
    }

    @Override
    protected Card createNilCard(){
        return createDefaultNilCard();
    }

    @Override
    protected Card createCardFor(@Nonnull final Server value){
        return new ServerCard(value);
    }

    @Override
    protected Predicate<Map.Entry<Server, Card>> createSearchFilter(Pattern searchPattern) {
        return (entry) -> searchPattern.matcher(entry.getKey().name).find();
    }

    @Subscribe
    public void onServerSearch(final ServerSearchEvent event){
        this.updateComponent(event.getSearchPattern());
    }

    @Subscribe
    public void onServerAdded(final ServerAddedEvent event){
        this.addItem(event.getServer());
    }

    @Subscribe
    public void onServerRemoved(final ServerRemovedEvent event){
        this.removeItem(event.getServer());
    }

    private static NilCard createDefaultNilCard(){
        return new NilCard(GetText.tr("There are no servers to display.\n\nInstall one from the Packs tab."));
    }
}