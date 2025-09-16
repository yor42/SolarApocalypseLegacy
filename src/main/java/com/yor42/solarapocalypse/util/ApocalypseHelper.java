package com.yor42.solarapocalypse.util;

import com.yor42.solarapocalypse.Main;
import com.yor42.solarapocalypse.capabilities.ChunkApocalypseProvider;
import com.yor42.solarapocalypse.capabilities.IChunkApocalypse;
import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
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
    public static void applyStageToBlockAt(WorldServer world, BlockPos currentPos, int globalStage, Random rand) {

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
                else{
                    if (world.getBlockState(currentPos.up()).getBlock() == Blocks.AIR){
                        world.setBlockState(currentPos.up(), Blocks.FIRE.getDefaultState(), 2);
                    }
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
                    for(EnumFacing facing : EnumFacing.VALUES){
                        if(facing == EnumFacing.DOWN){
                            continue;
                        }
                        BlockPos pos = currentPos.offset(facing);
                        if (world.getBlockState(pos).getBlock() == Blocks.AIR){
                            world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 2);
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
                }
                else if(block instanceof BlockMushroom || block instanceof BlockCrops || block instanceof BlockVine || block instanceof BlockCocoa || block instanceof BlockCactus || block instanceof BlockSnowBlock || block instanceof BlockReed) {
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

    public static void applyStageToEntireChunk(World world, Chunk chunk, int stageToApply, Random rand, BiPredicate<BlockPos, Random> chance, boolean scanAll) {
        int worldX = chunk.x * 16+8;
        int worldZ = chunk.z * 16+8;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getTopFilledSegment() + 15; y > 0; y--) {
                    BlockPos currentPos = new BlockPos(worldX + x, y, worldZ + z);
                    IBlockState state = world.getBlockState(currentPos);
                    Block block = state.getBlock();

                    if (block == Blocks.AIR || world.isRemote || (!world.canBlockSeeSky(currentPos) && !scanAll)) {
                        continue;
                    }

                    if(chance.test(currentPos, rand)) {
                        ApocalypseHelper.applyStageToBlockAt((WorldServer) world, currentPos, stageToApply, rand);
                    }

                    if(state.isOpaqueCube() && !state.getMaterial().getCanBurn()){
                        break;
                    }
                }
            }
        }

    }
}
