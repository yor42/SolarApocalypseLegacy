package com.yor42.solarapocalypse.mixin;

import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow
    public abstract MapStorage getMapStorage();

    @Shadow
    @Final
    public WorldProvider provider;

    @Inject(method = "canBlockFreezeBody", at = @At("HEAD"), cancellable = true, remap = false)
    private void canBlockFreezeModified(BlockPos pos, boolean noWaterAdj, CallbackInfoReturnable<Boolean> cir) {
        if (this.provider.getDimension() != 0) {
            return;
        }
        if(SolarApocalypseData.get(this.getMapStorage()).getStage()>0){
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canSnowAtBody", at = @At("HEAD"), cancellable = true, remap = false)
    private void canSnowAtModified(BlockPos pos, boolean checkLight, CallbackInfoReturnable<Boolean> cir) {
        if (this.provider.getDimension() != 0) {
            return;
        }
        if(SolarApocalypseData.get(this.getMapStorage()).getStage()>0){
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateWeatherBody", at = @At("HEAD"), cancellable = true, remap = false)
    private void canSnowAtModified(CallbackInfo ci) {
        if (this.provider.getDimension() != 0) {
            return;
        }
        if (SolarApocalypseData.get(this.getMapStorage()).getStage() > 4) {
            ci.cancel();
        }
    }
}
