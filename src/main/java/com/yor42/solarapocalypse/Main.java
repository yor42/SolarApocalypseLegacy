package com.yor42.solarapocalypse;

import com.yor42.solarapocalypse.capabilities.ChunkApocalypse;
import com.yor42.solarapocalypse.capabilities.ChunkApocalypseProvider;
import com.yor42.solarapocalypse.capabilities.IChunkApocalypse;
import com.yor42.solarapocalypse.command.CommandStartApocalypse;
import com.yor42.solarapocalypse.world.ApocalypseWorldGen;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class Main {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final long TICKS_PER_STAGE = 24000L * ApocalypseConfig.DaysBetweenStage;

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        GameRegistry.registerWorldGenerator(new ApocalypseWorldGen(), Integer.MAX_VALUE);
        CapabilityManager.INSTANCE.register(IChunkApocalypse.class, new Capability.IStorage<IChunkApocalypse>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<IChunkApocalypse> capability, IChunkApocalypse instance, EnumFacing side) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setLong("lastUnlodedTime", instance.getLastUnlodedTime());
                compound.setInteger("stage", instance.getStage());
                return compound;
            }


            @Override
            public void readNBT(Capability<IChunkApocalypse> capability, IChunkApocalypse instance, EnumFacing side, NBTBase nbt) {
                NBTTagCompound compound = (NBTTagCompound)nbt;
                instance.setStage(compound.getInteger("lastUnlodedTime"));
                instance.setLastUnlodedTime(compound.getLong("lastUnlodedTime"));
            }
        }, ChunkApocalypse::new);
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(new ResourceLocation(Tags.MOD_ID, "chunk_stage"), new ChunkApocalypseProvider());
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandStartApocalypse());
    }

}
