package com.yor42.solarapocalypse.mixin;

import com.yor42.solarapocalypse.Main;
import com.yor42.solarapocalypse.Tags;
import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Shadow
    @Final
    @Mutable
    private static ResourceLocation SUN_TEXTURES;

    @Shadow
    private WorldClient world;

    @Inject(method= "renderSky*", at = @At("HEAD"))
    private void onRendersky(float partialTicks, int pass, CallbackInfo ci) {
        int level = SolarApocalypseData.get(this.world).getStage();

        if (level > 0) {
            level = Math.min(level,7);
            SUN_TEXTURES = new ResourceLocation(Tags.MOD_ID, "textures/environment/" + level + ".png");
        }
    }

}
