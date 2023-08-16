package com.atlauncher.gui.models;

import com.atlauncher.data.Instance;

/**
 * 2023 / 08 / 15
 */
public class InstanceUIModel {
    public final Instance instance;
    public final boolean hasUpdate;

    public InstanceUIModel(Instance instance, boolean hasUpdate) {
        this.instance = instance;
        this.hasUpdate = hasUpdate;
    }
}
