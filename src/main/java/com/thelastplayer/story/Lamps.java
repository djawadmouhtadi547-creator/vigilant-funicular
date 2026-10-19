package com.thelastplayer.story;

import com.thelastplayer.block.LabLampBlock;
import com.thelastplayer.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/** Switches every registered lab lamp on or off (blackout in the final scene). */
public final class Lamps {
    private Lamps() {}

    public static void set(ServerWorld w, StoryData d, boolean lit) {
        for (long l : d.lamps) {
            BlockPos pos = BlockPos.of(l);
            if (!w.hasChunkAt(pos)) continue;
            BlockState st = w.getBlockState(pos);
            if (st.getBlock() == ModBlocks.LAB_LAMP.get() && st.getValue(LabLampBlock.LIT) != lit) {
                w.setBlock(pos, st.setValue(LabLampBlock.LIT, lit), 3);
            }
        }
    }
}
