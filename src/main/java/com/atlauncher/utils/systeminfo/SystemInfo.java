package com.atlauncher.utils.systeminfo;

import com.google.gson.annotations.SerializedName;

public class SystemInfo {
    @SerializedName("Memory")
    public Memory memory;

    @SerializedName("CPU")
    public CPU cpu;
}
