/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import java.util.ArrayList;

public class Instances {

    private ArrayList<Instance> instances;

    public Instances() {
        instances = new ArrayList<Instance>();
    }

    public void addInstance(Instance instance) {
        instances.add(instance);
    }

    public int totalInstances() {
        return instances.size();
    }

    public String getName(int index) {
        Instance instance = instances.get(index);
        return instance.getName();
    }

    public String getPack(int index) {
        Instance instance = instances.get(index);
        return instance.getPackName();
    }

    public Version getVersion(int index) {
        Instance instance = instances.get(index);
        return instance.getVersion();
    }

    public Version getLatestVersion(int index) {
        Instance instance = instances.get(index);
        return instance.getLatestVersion();
    }

    public Instance getInstance(int index) {
        return instances.get(index);
    }

    public void removeInstance(Instance instance) {
        instances.remove(instance);
    }

    public boolean isInstance(String instanceName) {
        for (int i = 0; i < instances.size(); i++) {
            if (instances.get(i).getName().equals(instanceName)) {
                return true;
            }
        }
        return false;
    }

}
