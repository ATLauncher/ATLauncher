package com.atlauncher.gui.tabs.instances;

import com.atlauncher.data.Instance;
import com.atlauncher.gui.components.search.SearchPanel;

public final class InstanceSearchPanel extends SearchPanel<Instance, InstanceSearchEvent> {
    public InstanceSearchPanel(){
        super(new InstanceSearchField(), InstanceSortingStrategies.values());
    }
}