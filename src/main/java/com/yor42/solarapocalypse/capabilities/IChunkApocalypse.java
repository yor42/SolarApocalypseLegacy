package com.yor42.solarapocalypse.capabilities;

public interface IChunkApocalypse {
    int getStage();
    long getLastUnlodedTime();
    void setStage(int stage);
    void setLastUpdateTime(long time);
}
