package com.yor42.solarapocalypse.util;

import com.yor42.solarapocalypse.Main;
import com.yor42.solarapocalypse.capabilities.ChunkApocalypseProvider;
import com.yor42.solarapocalypse.capabilities.IChunkApocalypse;
import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ApocalypseHelper {
    public static void applyStageToBlockAt(WorldServer world, BlockPos currentPos, int globalStage, boolean catchup_mode, Random rand) {

        if(globalStage<=0){
            return;
        }

        IBlockState state = world.getBlockState(currentPos);
        Block block = state.getBlock();

        switch (globalStage){
            case 7:
            {
                if(block == Blocks.STONE){
                    world.setBlockState(currentPos, Blocks.MAGMA.getDefaultState(), 2);
                }
            }
            case 5:
            {
                if(block == Blocks.COBBLESTONE){
                    world.setBlockState(currentPos, Blocks.STONE.getDefaultState(), 2);
                }
                else if(block instanceof BlockGlass){
                    world.destroyBlock(currentPos, false);
                }
                else if(block == Blocks.HARDENED_CLAY || block == Blocks.STAINED_HARDENED_CLAY){
                    world.setBlockState(currentPos, Blocks.SAND.getDefaultState(), 2);
                }
            }
            case 4:
            {
                if(block == Blocks.GRAVEL){
                    world.setBlockState(currentPos, Blocks.COBBLESTONE.getDefaultState(), 2);
                }else if(block == Blocks.CLAY){
                    world.setBlockState(currentPos, Blocks.HARDENED_CLAY.getDefaultState(), 2);
                }
            }
            case 3:
                if(state.getMaterial().getCanBurn()){
                    if(catchup_mode){
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 2);
                    }
                    else {
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            if (facing == EnumFacing.DOWN) {
                                continue;
                            }
                            BlockPos pos = currentPos.offset(facing);
                            if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
                                world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 2);
                                break;
                            }
                        }
                    }
                }
                else if(block instanceof BlockSand){
                    world.setBlockState(currentPos, Blocks.GLASS.getDefaultState(), 2);
                }
                else if(block instanceof BlockDirt){
                    world.setBlockState(currentPos, Blocks.GRAVEL.getDefaultState(), 2);
                }
            case 2:
                if(block instanceof BlockSapling){
                    world.setBlockState(currentPos, Blocks.DEADBUSH.getDefaultState(), 2);
                    break;
                }
                else if(block instanceof BlockBush || block instanceof BlockVine || block instanceof BlockCocoa || block instanceof BlockCactus || block instanceof BlockSnowBlock || block instanceof BlockReed) {
                    world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 2);
                }
                else if(block instanceof BlockGrassPath){
                    world.setBlockState(currentPos, Blocks.DIRT.getDefaultState(), 2);
                }
                else if(block instanceof BlockIce){
                    world.setBlockState(currentPos, Blocks.WATER.getDefaultState(), 2);
                }
            case 1:
                if(block instanceof BlockLeaves || block instanceof BlockDoublePlant || block instanceof BlockSnow || block instanceof BlockTallGrass || block instanceof BlockLilyPad){
                    world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 2);
                }
                else if(block instanceof BlockGrass || block instanceof BlockMycelium){
                    world.setBlockState(currentPos, Blocks.DIRT.getDefaultState(), 2);
                }
                else if(block == Blocks.MOSSY_COBBLESTONE){
                    world.setBlockState(currentPos, Blocks.COBBLESTONE.getDefaultState(), 2);
                }
        }

    }

    public static void doApocalypseCatchUp(World world, Chunk chunk, int to, Random rand) {
        for(int i = 1; i<=to; i++) {
            ApocalypseHelper.applyStageToEntireChunk(world, chunk, i, rand, ((blockPos, random) -> {
                SolarApocalypseData globalData = SolarApocalypseData.get(world);
                long stageElapsedTime = globalData.getStageElapsedTime(world);
                long halfphase = Main.TICKS_PER_STAGE / 2;
                if (stageElapsedTime >= halfphase) {
                    return true;
                }
                return rand.nextLong() <= (2 * stageElapsedTime) / Main.TICKS_PER_STAGE;
            }), i<to);
        }
    }

    public static void BurnBabyBurn(Entity entity){
        World world = entity.world;
        if (world.provider.getDimension() != 0 || entity.ticksExisted%19 != 0) {
            return;
        }
        SolarApocalypseData data = SolarApocalypseData.get(world);
        int stage = data.getStage();
        if (stage < 3) {
            return;
        }

        if (!world.isDaytime() || world.isRemote || !world.canSeeSky(new BlockPos(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ)) || entity.isInWater() || (entity instanceof EntityPlayer && (((EntityPlayer) entity).isCreative() || ((EntityPlayer) entity).isSpectator()))) {
            return;
        }
        entity.setFire(8);
    }

    public static void applyStageToEntireChunk(World world, Chunk chunk, int stageToApply, Random rand, BiPredicate<BlockPos, Random> chance, boolean scanAll) {
        int worldX = (chunk.x * 16)+8;
        int worldZ = (chunk.z * 16)+8;

        if(!chunk.isLoaded()){
            return;
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getTopFilledSegment() + 15; y > 0; y--) {
                    BlockPos currentPos = new BlockPos(worldX + x, y, worldZ + z);
                    IBlockState state = world.getBlockState(currentPos);
                    Block block = state.getBlock();

                    if (block == Blocks.AIR || world.isRemote) {
                        continue;
                    }

                    if(chance.test(currentPos, rand)) {
                        ApocalypseHelper.applyStageToBlockAt((WorldServer) world, currentPos, stageToApply, scanAll, rand);
                    }

                    if(state.isOpaqueCube() && !state.getMaterial().getCanBurn()){
                        break;
                    }
                }
            }
        }

    }
}
