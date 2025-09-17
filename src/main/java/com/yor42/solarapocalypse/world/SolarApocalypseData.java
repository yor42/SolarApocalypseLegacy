package com.yor42.solarapocalypse.world;

import com.yor42.solarapocalypse.Tags;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class SolarApocalypseData extends WorldSavedData {

    private static final String IDENTIFIER = Tags.MOD_ID+":apocalypsedata";
    private int apocalypseStage = -1;
    private long lastApocalypseStageUpdate = 0;
    private long apocalypseStartedTime = -1;

    public SolarApocalypseData() {
        super(IDENTIFIER);
    }

    public SolarApocalypseData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.apocalypseStage = nbt.getInteger("apocalypsestage");
        this.lastApocalypseStageUpdate = nbt.getLong("lastApocalypseStageUpdate");
        this.apocalypseStartedTime = nbt.getLong("apocalypseStartedTime");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("apocalypsestage", this.apocalypseStage);
        compound.setLong("lastApocalypseStageUpdate", this.lastApocalypseStageUpdate);
        compound.setLong("apocalypseStartedTime", this.apocalypseStartedTime);
        return compound;
    }

    public long getApocalypseStartedTime(){
        return this.apocalypseStartedTime;
    }

    public void setApocalypseStartedTime(long value){
        this.apocalypseStartedTime = value;
    }

    public boolean isApocalypseStarted(){
        return this.getApocalypseStartedTime()>=0;
    }

    public void setApocalypseStage(World world, int stage){
        if(!this.isApocalypseStarted() && stage>=0){
            this.setApocalypseStartedTime(world.getWorldTime());
        }
        else if(stage <0){
            this.setApocalypseStartedTime(-1);
        }
        this.apocalypseStage = stage;
        this.lastApocalypseStageUpdate = world.getWorldTime();
        markDirty();
    };

    public void advanceStage(World world) {
        int stage = this.getStage()+1;
        this.setApocalypseStage(world, stage);
        if(world.getMinecraftServer() != null) {
            world.getMinecraftServer().getPlayerList().getPlayers().forEach((entityPlayerMP -> entityPlayerMP.sendStatusMessage(new TextComponentTranslation("text.solarapocalypse.stageprogressed", stage), true)));
        }
    }

    public long getApocalypseElapsedTime(World world){
        if(!this.isApocalypseStarted()){
            return -1;
        }
        return world.getWorldTime() - this.getApocalypseStartedTime();
    }

    public long getStageElapsedTime(World world){
        if(!this.isApocalypseStarted()){
            return -1;
        }
        return world.getWorldTime() - this.getLastApocalypseStageUpdate();
    }

    public int getStage() {
        return this.apocalypseStage;
    }

    public long getLastApocalypseStageUpdate() {
        return lastApocalypseStageUpdate;
    }

    public static SolarApocalypseData get(MapStorage storage) {
        SolarApocalypseData instance = (SolarApocalypseData) storage.getOrLoadData(SolarApocalypseData.class, IDENTIFIER);

        if (instance == null) {
            instance = new SolarApocalypseData();
            storage.setData(IDENTIFIER, instance);
        }
        return instance;
    }

    public static SolarApocalypseData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        return get(storage);
    }
}