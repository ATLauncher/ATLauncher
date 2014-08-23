/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;

@Json
public class MainClass{
    private String mainClass;
    private String depends;
    private String dependsGroup;

    public String getMainClass(){
        return this.mainClass;
    }

    public String getDepends(){
        return this.depends;
    }

    public String getDependsGroup(){
        return this.dependsGroup;
    }

    public boolean hasDepends(){
        return this.depends != null;
    }

    public boolean hasDependsGroup(){
        return this.dependsGroup != null;
    }
}