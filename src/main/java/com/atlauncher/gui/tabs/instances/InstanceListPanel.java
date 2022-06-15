/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.tabs.instances;

import com.atlauncher.AppEventBus;
import com.atlauncher.data.Instance;
import com.atlauncher.events.instance.InstanceAddedEvent;
import com.atlauncher.events.instance.InstanceRemovedEvent;
import com.atlauncher.gui.card.Card;
import com.atlauncher.gui.card.InstanceCard;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.components.search.SearchListPanel;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class InstanceListPanel extends SearchListPanel<Instance>{
    private static final Logger LOG = LogManager.getLogger(InstanceListPanel.class);

    public InstanceListPanel(@Nullable final Set<Instance> instances){
        super(instances);
        AppEventBus.registerToUIOnly(this);
    }

    @Override
    protected Card createNilCard() {
        return createDefaultNilCard();
    }

    @Override
    protected Card createCardFor(final Instance value) {
        return new InstanceCard(value);
    }

    @Override
    protected Predicate<Map.Entry<Instance, Card>> createSearchFilter(Pattern searchPattern) {
        return (val) -> searchPattern.matcher(val.getKey().launcher.name).find();
    }

    @Subscribe
    public void onInstanceSearch(final InstanceSearchEvent event){
        this.updateComponent(event.getSearchPattern());
    }

    @Subscribe
    public void onInstanceAdded(final InstanceAddedEvent event){
        this.addItem(event.getInstance());
    }

    @Subscribe
    public void onInstanceRemoved(final InstanceRemovedEvent event){
        this.removeItem(event.getInstance());
    }

    private static Card createDefaultNilCard(){
        return new NilCard(GetText.tr("There are no instances to display.\n\nInstall one from the Packs tab."));
    }
}
