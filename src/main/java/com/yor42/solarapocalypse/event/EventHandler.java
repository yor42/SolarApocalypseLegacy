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
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber
public class EventHandler {

    private static int COUNTDOWN = 40;
    private static ConcurrentLinkedQueue<Chunk> UPDATELIST = new ConcurrentLinkedQueue<>();

    // 성능 최적화 변수들
    private static int chunksProcessedThisTick = 0;
    private static final int MAX_CHUNKS_PER_TICK = 1; // 틱당 최대 처리할 chunk 수
    private static byte updateCounter = 0; // 로깅 최적화용

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

            // START 단계에서 chunk catch-up 처리 (CoFHWorld 스타일)
            processChunkUpdates(world, globalStage);
        }

        if (event.phase != TickEvent.Phase.END || event.world.isRemote || --COUNTDOWN > 0 || !world.isDaytime() || globalStage < 2) {
            return;
        }

        // END 단계에서 일반 apocalypse 처리
        COUNTDOWN = 20 + world.rand.nextInt(40);

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

    /**
     * CoFHWorld 스타일의 chunk 처리 - 한번에 제한된 수만 처리
     */
    private static void processChunkUpdates(WorldServer world, int globalStage) {
        // 틱당 처리할 수 있는 최대 chunk 수 제한
        int processed = 0;

        while (!UPDATELIST.isEmpty() && processed < MAX_CHUNKS_PER_TICK) {
            Chunk chunk = UPDATELIST.poll();
            if (chunk == null) break;

            // 로딩 상태 확인
            if (!chunk.isLoaded()) {
                continue;
            }

            IChunkApocalypse chunkCap = chunk.getCapability(ChunkApocalypseProvider.CHUNK_APOCALYPSE_CAP, null);
            if (chunkCap == null) continue;

            int chunkStage = chunkCap.getStage();

            if (chunkStage < globalStage) {
                // 로깅 최적화 - 32번마다 또는 큐가 작을 때만 info 로그
                if (updateCounter++ == 0 || UPDATELIST.size() < 3) {
                    Main.LOGGER.info("Updating chunk to stage {} at [{}, {}]", globalStage, chunk.x, chunk.z);
                } else {
                    Main.LOGGER.debug("Updating chunk to stage {} at [{}, {}]", globalStage, chunk.x, chunk.z);
                }
                updateCounter &= 31; // 32번마다 리셋

                ApocalypseHelper.doApocalypseCatchUp(world, chunk, globalStage, world.rand, false);
                chunkCap.setStage(globalStage);
                chunkCap.setLastUpdateTime(world.getTotalWorldTime());
                processed++;
            }
        }

        // 처리된 chunk 수를 로그로 남김 (디버깅용)
        if (processed > 0) {
            Main.LOGGER.debug("Processed {} chunks this tick, {} remaining", processed, UPDATELIST.size());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        ApocalypseHelper.BurnBabyBurn(event.player);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        Chunk chunk = event.getChunk();
        if (world.provider.getDimension() != 0 || !chunk.isLoaded()) {
            return;
        }
        if (world.isRemote) return;

        SolarApocalypseData globalData = SolarApocalypseData.get(world);
        int globalStage = globalData.getStage();

        if (globalStage < 0) return;

        IChunkApocalypse chunkCap = chunk.getCapability(ChunkApocalypseProvider.CHUNK_APOCALYPSE_CAP, null);
        if (chunkCap == null) return;

        int chunkStage = chunkCap.getStage();

        if (chunkStage >= globalStage) {
            return;
        }

        // 큐에 추가 - thread-safe
        UPDATELIST.offer(chunk);

        Main.LOGGER.debug("Queued chunk [{}, {}] for apocalypse catch-up (stage {} -> {})",
                chunk.x, chunk.z, chunkStage, globalStage);
    }

    @SubscribeEvent
    public static void onChunkSave(ChunkEvent.Unload event) {
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
        chunkCap.setLastUpdateTime(world.getTotalWorldTime());
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
        chunkCap.setLastUpdateTime(world.getTotalWorldTime());
    }
}