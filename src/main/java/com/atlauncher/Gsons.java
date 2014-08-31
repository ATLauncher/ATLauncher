package com.atlauncher;

import com.atlauncher.adapter.ColorTypeAdapter;
import com.atlauncher.data.mojang.DateTypeAdapter;
import com.atlauncher.data.mojang.EnumTypeAdapterFactory;
import com.atlauncher.data.mojang.FileTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.Color;
import java.io.File;
import java.util.Date;

public final class Gsons {
    public static final Gson DEFAULT = new GsonBuilder().setPrettyPrinting().create();

    public static final Gson THEMES = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Color.class,
            new ColorTypeAdapter()).create();

    public static final Gson DEFAULT_ALT = new GsonBuilder().registerTypeAdapterFactory(new EnumTypeAdapterFactory())
            .registerTypeAdapter(Date.class, new DateTypeAdapter()).registerTypeAdapter(File.class,
                    new FileTypeAdapter()).create();

    private Gsons() {
    }
}
