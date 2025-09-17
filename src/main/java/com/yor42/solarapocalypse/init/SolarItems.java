package com.yor42.solarapocalypse.init;

import com.yor42.solarapocalypse.Tags;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;

public class SolarItems
{
    private static final ArrayList<Item> ITEMS = new ArrayList<>();



    public static void RegisterItems(IForgeRegistry<Item> registry){
        registry.registerAll(ITEMS.toArray(ITEMS.toArray(new Item[0])));
    }

    public static void registerBlockItem(Block block){
        ITEMS.add(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        ITEMS.forEach(SolarItems::registerModel);
    }

    @SideOnly(Side.CLIENT)
    public static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(),"inventory"));
    }

}
