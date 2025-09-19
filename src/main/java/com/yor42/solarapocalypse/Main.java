package com.yor42.solarapocalypse;

import com.yor42.solarapocalypse.capabilities.ChunkApocalypse;
import com.yor42.solarapocalypse.capabilities.ChunkApocalypseProvider;
import com.yor42.solarapocalypse.capabilities.IChunkApocalypse;
import com.yor42.solarapocalypse.command.CommandStartApocalypse;
import com.yor42.solarapocalypse.init.SolarBlocks;
import com.yor42.solarapocalypse.init.SolarItems;
import com.yor42.solarapocalypse.world.ApocalypseWorldGen;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class Main {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final long TICKS_PER_STAGE = 24000L * ApocalypseConfig.DaysBetweenStage;
    public static final CreativeTabs tab = new CreativeTabs("SolarApocalypseTab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(SolarBlocks.PANDORA_LANTERN);
        }
    };

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.addShapedRecipe(
                new ResourceLocation(Tags.MOD_ID, "pandora_lantern"),
                null,
                new ItemStack(SolarBlocks.PANDORA_LANTERN, 1),
                "*#*",
                "#T#",
                "*#*",
                '#', "cobblestone",
                '*', "plankWood",
                'T', Blocks.TORCH
        );
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        GameRegistry.registerWorldGenerator(new ApocalypseWorldGen(), Integer.MAX_VALUE);
        CapabilityManager.INSTANCE.register(IChunkApocalypse.class, new Capability.IStorage<>() {
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
                NBTTagCompound compound = (NBTTagCompound) nbt;
                instance.setStage(compound.getInteger("lastUnlodedTime"));
                instance.setLastUpdateTime(compound.getLong("lastUnlodedTime"));
            }
        }, ChunkApocalypse::new);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        SolarBlocks.registerBlocks(registry);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> reg = event.getRegistry();
        SolarItems.RegisterItems(reg);
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(new ResourceLocation(Tags.MOD_ID, "chunk_stage"), new ChunkApocalypseProvider());
    }



    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandStartApocalypse());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        SolarBlocks.registerModels();
        SolarItems.registerModels();
    }

}
