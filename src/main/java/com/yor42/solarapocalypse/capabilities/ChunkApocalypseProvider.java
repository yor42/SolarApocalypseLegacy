package com.yor42.solarapocalypse.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkApocalypseProvider implements ICapabilitySerializable<NBTTagCompound> {

    @CapabilityInject(IChunkApocalypse.class)
    public static final Capability<IChunkApocalypse> CHUNK_APOCALYPSE_CAP = null;

    private final IChunkApocalypse instance = CHUNK_APOCALYPSE_CAP.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CHUNK_APOCALYPSE_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CHUNK_APOCALYPSE_CAP ? CHUNK_APOCALYPSE_CAP.cast(this.instance) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) CHUNK_APOCALYPSE_CAP.getStorage().writeNBT(CHUNK_APOCALYPSE_CAP, this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CHUNK_APOCALYPSE_CAP.getStorage().readNBT(CHUNK_APOCALYPSE_CAP, this.instance, null, nbt);
    }
}
