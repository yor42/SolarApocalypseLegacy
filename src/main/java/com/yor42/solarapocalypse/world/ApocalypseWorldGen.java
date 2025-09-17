package com.yor42.solarapocalypse.world;

import com.yor42.solarapocalypse.util.ApocalypseHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class ApocalypseWorldGen implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

        if (world.provider.getDimension() != 0) {
            return;
        }

        SolarApocalypseData data = SolarApocalypseData.get(world);
        int stage = data.getStage();

        if (stage <= 0) {
            return;
        }

        ApocalypseHelper.doApocalypseCatchUp(world, world.getChunk(chunkX, chunkZ), stage, world.rand, true);
    }
}
