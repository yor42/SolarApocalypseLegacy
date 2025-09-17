package com.yor42.solarapocalypse.block;

import com.yor42.solarapocalypse.Tags;
import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockLantern extends Block {

    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockLantern() {
        super(Material.ROCK);
        this.setResistance(10.0F);
        this.setHardness(1.5F);
        this.setDefaultState(this.getDefaultState().withProperty(ACTIVE, false));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        SolarApocalypseData data = SolarApocalypseData.get(worldIn);
        boolean isApocalypseStarted = data.isApocalypseStarted();
        worldIn.setBlockState(pos, state.withProperty(ACTIVE, isApocalypseStarted), 3);
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
        return world.provider.getDimension() == 0 && world.getBlockState(pos).getValue(ACTIVE);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);
        SolarApocalypseData data = SolarApocalypseData.get(worldIn);
        boolean isApocalypseStarted = data.isApocalypseStarted();
        if(isApocalypseStarted != state.getValue(ACTIVE)) {
            worldIn.setBlockState(pos, state.withProperty(ACTIVE, isApocalypseStarted), 3);
        }

    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.provider.getDimension() != 0) {
            playerIn.sendStatusMessage(new TextComponentTranslation("text.solarapocalypse.wrongdimension"), true);
            return true;
        }


        SolarApocalypseData data = SolarApocalypseData.get(worldIn);
        boolean isApocalypseStarted = data.isApocalypseStarted();

        if (isApocalypseStarted) {
            playerIn.sendStatusMessage(new TextComponentTranslation("text.solarapocalypse.apocalypse_already_triggered"), true);
        } else if (!worldIn.canBlockSeeSky(pos.up()) || !worldIn.isDaytime() || worldIn.isRainingAt(pos)) {
            playerIn.sendStatusMessage(new TextComponentTranslation("text.solarapocalypse.nosunlight"), true);
        } else {

            EntityLightningBolt lightningBolt = new EntityLightningBolt(worldIn, pos.getX()+0.5f, pos.getY()+1, pos.getZ()+0.5f, false);
            worldIn.spawnEntity(lightningBolt);
            data.setApocalypseStage(worldIn, 0);
            worldIn.setBlockState(pos, state.withProperty(ACTIVE, true));

            MinecraftServer server = worldIn.getMinecraftServer();
            if(server != null){
                server.getPlayerList().getPlayers().forEach((player)->{
                    ITextComponent text  = player == playerIn? new TextComponentTranslation("text.solarapocalypse.started"): new TextComponentTranslation("text.solarapocalypse.startedby", playerIn.getDisplayName());
                    playerIn.sendStatusMessage(text, true);
                });

            }
        }
        return true;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ACTIVE)? 1:0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(ACTIVE, meta == 1);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(ACTIVE, false);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTIVE);
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return blockState.getValue(ACTIVE) ? -1 : super.getBlockHardness(blockState, worldIn, pos);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(ACTIVE)? 15:0;
    }
}
