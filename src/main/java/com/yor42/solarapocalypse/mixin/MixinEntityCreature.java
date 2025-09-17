package com.yor42.solarapocalypse.mixin;

import com.yor42.solarapocalypse.util.ApocalypseHelper;
import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityCreature.class)
public class MixinEntityCreature extends EntityLiving {

    public MixinEntityCreature(World worldIn) {
        super(worldIn);
    }

    @Unique
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        ApocalypseHelper.BurnBabyBurn(this);
    }
}
