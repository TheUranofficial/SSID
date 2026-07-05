package com.theuran.test.settings;

import mchorse.bbs.settings.SettingsBuilder;
import mchorse.bbs.settings.values.ValueBoolean;
import mchorse.bbs.settings.values.ValueFloat;
import mchorse.bbs.settings.values.ValueInt;

public class TestSettings {
    public static ValueBoolean firstRun;

    public static ValueBoolean renderTerrainDebug;
    public static ValueFloat renderQuality;
    public static ValueBoolean renderVsync;
    public static ValueInt renderFrameRate;

    public static void register(SettingsBuilder builder) {
        firstRun = builder.category("rendering").getBoolean("first_run", false);
        firstRun.invisible();

        renderTerrainDebug = builder.getBoolean("terrain_debug", false);
        renderQuality = builder.getFloat("render_quality", 1F, 0.01F, 2F);
        renderVsync = builder.getBoolean("vsync", true);
        renderFrameRate = builder.getInt("frame_rate", 60, 5, 10000);
    }
}