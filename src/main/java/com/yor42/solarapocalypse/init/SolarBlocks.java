package com.yor42.solarapocalypse.init;

import com.yor42.solarapocalypse.Main;
import com.yor42.solarapocalypse.Tags;
import com.yor42.solarapocalypse.block.BlockLantern;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;

@Mod.EventBusSubscriber
public class SolarBlocks
{

    private static final ArrayList<Block> BLOCKS = new ArrayList<>();

    public static Block PANDORA_LANTERN = register(new BlockLantern(), "pandora_lantern");

    public static void registerBlocks(IForgeRegistry<Block> registry){
        registry.registerAll(BLOCKS.toArray(BLOCKS.toArray(new Block[0])));
    }

    private static Block register(Block block, String name){
        block = block.setRegistryName(new ResourceLocation(Tags.MOD_ID, name)).setTranslationKey(name).setCreativeTab(Main.tab);
        BLOCKS.add(block);
        SolarItems.registerBlockItem(block);
        return block;
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        BLOCKS.forEach(block -> {
            Item item = Item.getItemFromBlock(block);
            SolarItems.registerModel(item);
        });
    }
}
