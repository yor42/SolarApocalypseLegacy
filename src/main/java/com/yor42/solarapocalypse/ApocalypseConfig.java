package com.yor42.solarapocalypse;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_NAME)
public class ApocalypseConfig {

    @Config.RequiresMcRestart
    @Config.RangeInt(min = 1)
    public static int DaysBetweenStage = 7;

    @Config.RequiresMcRestart
    @Config.RangeInt(min = 1)
    public static int ChunkUpdatesPerTick = 3;

}
