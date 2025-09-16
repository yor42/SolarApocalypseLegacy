package com.yor42.solarapocalypse.mixin;

import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockGrass.class)
public class MixinGrassBlock
{
    @Inject(method = "updateTick", at = @At("HEAD"), cancellable = true)
    private void onUpdate(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci){
        SolarApocalypseData data = SolarApocalypseData.get(worldIn);
        int stage = data.getStage();
        if(stage>=1){
            ci.cancel();
        }
    }
}
