package com.thelastplayer.registry;

import com.thelastplayer.TheLastPlayer;
import com.thelastplayer.item.StoryItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TheLastPlayer.MODID);

    public static final ItemGroup TAB = new ItemGroup("thelastplayer") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.BLUE_GLASS_SHARD.get());
        }
    };

    private static Item.Properties story() {
        return new Item.Properties().tab(TAB).stacksTo(1).rarity(Rarity.RARE);
    }

    // The 3 key items of the secret ending
    public static final RegistryObject<Item> BLUE_GLASS_SHARD = ITEMS.register("blue_glass_shard",
            () -> new StoryItem(story(), "shard", true));
    public static final RegistryObject<Item> OLD_DEVELOPER_LETTER = ITEMS.register("old_developer_letter",
            () -> new StoryItem(story(), "letter", false));
    public static final RegistryObject<Item> MEMORY_LOG = ITEMS.register("memory_log",
            () -> new StoryItem(story(), "memory", true));

    private static RegistryObject<Item> blockItem(String name, Supplier<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(TAB)));
    }

    public static final RegistryObject<Item> ECHO_DEVICE = blockItem("echo_device", ModBlocks.ECHO_DEVICE);
    public static final RegistryObject<Item> ARCHIVE_TERMINAL = blockItem("archive_terminal", ModBlocks.ARCHIVE_TERMINAL);
    public static final RegistryObject<Item> RESET_TERMINAL = blockItem("reset_terminal", ModBlocks.RESET_TERMINAL);
    public static final RegistryObject<Item> LAB_LAMP = blockItem("lab_lamp", ModBlocks.LAB_LAMP);
    public static final RegistryObject<Item> LAB_TILE = blockItem("lab_tile", ModBlocks.LAB_TILE);
    public static final RegistryObject<Item> LAB_PANEL = blockItem("lab_panel", ModBlocks.LAB_PANEL);
}
