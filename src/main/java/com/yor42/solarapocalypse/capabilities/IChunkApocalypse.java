package com.yor42.solarapocalypse.capabilities;

import net.minecraftforge.common.capabilities.Capability;

public interface IChunkApocalypse {
    int getStage();
    long getLastUnlodedTime();
    void setStage(int stage);
    void setLastUnlodedTime(long time);
}
