package com.yor42.solarapocalypse.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ChunkApocalypse implements IChunkApocalypse {

    private long lastUnlodedTime = 0;
    private int stage = -1;

    @Override
    public int getStage() {
        return this.stage;
    }

    @Override
    public long getLastUnlodedTime() {
        return lastUnlodedTime;
    }

    @Override
    public void setStage(int stage) {
        this.stage = stage;
    }

    @Override
    public void setLastUnlodedTime(long time) {
        this.lastUnlodedTime = time;
    }
}
