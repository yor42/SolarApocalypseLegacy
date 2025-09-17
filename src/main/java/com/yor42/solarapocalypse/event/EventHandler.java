package com.yor42.solarapocalypse.event;

import com.yor42.solarapocalypse.ApocalypseConfig;
import com.yor42.solarapocalypse.Main;
import com.yor42.solarapocalypse.capabilities.ChunkApocalypseProvider;
import com.yor42.solarapocalypse.capabilities.IChunkApocalypse;
import com.yor42.solarapocalypse.util.ApocalypseHelper;
import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

@Mod.EventBusSubscriber
public class EventHandler {

    private static int COUNTDOWN = 40;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {

        WorldServer world = (WorldServer) event.world;
        if (world.provider.getDimension() != 0) {
            return;
        }
        
        SolarApocalypseData data = SolarApocalypseData.get(world);
        int globalStage = data.getStage();
        if (globalStage < 0) return;

        long time = event.world.getWorldTime();

        if (event.phase == TickEvent.Phase.START && !event.world.isRemote) {

            long delay = Main.TICKS_PER_STAGE - data.getStageElapsedTime(world);

            if (time > 0 && delay <= 0) {
                data.advanceStage(event.world);
            }
        }

        if (event.phase != TickEvent.Phase.END || event.world.isRemote || --COUNTDOWN > 0 || !world.isDaytime() || globalStage < 2) {
            return;
        }

        COUNTDOWN = 20+world.rand.nextInt(40);

        for (Iterator<Chunk> it = world.getPersistentChunkIterable(world.getPlayerChunkMap().getChunkIterator()); it.hasNext(); ) {
            Chunk chunk = it.next();
            for (int i = 0; i < world.rand.nextInt(ApocalypseConfig.ChunkUpdatesPerTick); i++) {

                int x = chunk.x * 16 + world.rand.nextInt(16);
                int z = chunk.z * 16 + world.rand.nextInt(16);

                for (int y = chunk.getTopFilledSegment() + 15; y > 0; y--) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    Block block = world.getBlockState(currentPos).getBlock();
                    if (block == Blocks.AIR) {
                        continue;
                    }
                    ApocalypseHelper.applyStageToBlockAt(world, currentPos, globalStage, false, world.rand);
                    Main.LOGGER.debug("Stage Applied @ {}", currentPos);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        ApocalypseHelper.BurnBabyBurn(event.player);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.provider.getDimension() != 0) {
            return;
        }
        if (world.isRemote) return;

        Chunk chunk = event.getChunk();

        SolarApocalypseData globalData = SolarApocalypseData.get(world);
        int globalStage = globalData.getStage();

        if (globalStage < 0) return;

        IChunkApocalypse chunkCap = chunk.getCapability(ChunkApocalypseProvider.CHUNK_APOCALYPSE_CAP, null);
        if (chunkCap == null) return;

        int chunkStage = chunkCap.getStage();

        if (chunkStage < globalStage) {
            ApocalypseHelper.doApocalypseCatchUp(world, chunk, globalStage, world.rand);
            chunkCap.setStage(globalStage);
        }
    }

    @SubscribeEvent
    public static void onChunkUnLoad(ChunkEvent.Unload event) {
        Chunk chunk = event.getChunk();
        World world = event.getWorld();
        if (world.provider.getDimension() != 0) {
            return;
        }
        SolarApocalypseData globalData = SolarApocalypseData.get(world);
        int globalStage = globalData.getStage();
        IChunkApocalypse chunkCap = chunk.getCapability(ChunkApocalypseProvider.CHUNK_APOCALYPSE_CAP, null);

        if(chunkCap == null){
            return;
        }

        chunkCap.setStage(globalStage);
        chunkCap.setLastUnlodedTime(world.getTotalWorldTime());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.PlayerTickEvent event) {

    }
}
