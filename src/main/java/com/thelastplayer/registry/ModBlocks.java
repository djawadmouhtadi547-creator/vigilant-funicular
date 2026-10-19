package com.thelastplayer.registry;

import com.thelastplayer.TheLastPlayer;
import com.thelastplayer.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TheLastPlayer.MODID);

    /** Story blocks cannot be broken in survival (they carry the plot). */
    private static AbstractBlock.Properties unbreakable() {
        return AbstractBlock.Properties.of(Material.METAL).strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noDrops();
    }

    public static final RegistryObject<Block> ECHO_DEVICE = BLOCKS.register("echo_device",
            () -> new EchoDeviceBlock(unbreakable().lightLevel(s -> 7)));
    public static final RegistryObject<Block> ARCHIVE_TERMINAL = BLOCKS.register("archive_terminal",
            () -> new ArchiveTerminalBlock(unbreakable().lightLevel(s -> 5)));
    public static final RegistryObject<Block> RESET_TERMINAL = BLOCKS.register("reset_terminal",
            () -> new ResetTerminalBlock(unbreakable().lightLevel(s -> 8)));
    public static final RegistryObject<Block> LAB_LAMP = BLOCKS.register("lab_lamp",
            () -> new LabLampBlock(AbstractBlock.Properties.of(Material.GLASS).strength(2.0F, 10.0F)
                    .sound(SoundType.GLASS).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 15 : 0)));
    public static final RegistryObject<Block> LAB_TILE = BLOCKS.register("lab_tile",
            () -> new Block(AbstractBlock.Properties.of(Material.STONE).strength(4.0F, 30.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> LAB_PANEL = BLOCKS.register("lab_panel",
            () -> new Block(AbstractBlock.Properties.of(Material.METAL).strength(4.0F, 30.0F).sound(SoundType.METAL)));
}
