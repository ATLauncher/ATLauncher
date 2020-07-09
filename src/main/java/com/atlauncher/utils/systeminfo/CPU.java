package com.atlauncher.utils.systeminfo;

import com.google.gson.annotations.SerializedName;

public class CPU {
    @SerializedName("total_cores")
    public int totalCores;

    @SerializedName("total_threads")
    public int totalThreads;
}
