package com.thelastplayer.structure;

import com.thelastplayer.block.ArchiveTerminalBlock;
import com.thelastplayer.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * A list of block placements relative to an origin. Built eagerly (deterministic, seeded by origin),
 * placed a few thousand blocks per tick by BuildQueue so the server never freezes.
 */
public class Blueprint {
    private static final class P {
        final long pos;
        final BlockState st;
        final Consumer<ServerWorld> after;

        P(long pos, BlockState st, Consumer<ServerWorld> after) { this.pos = pos; this.st = st; this.after = after; }
    }

    private final BlockPos origin;
    private final List<P> list = new ArrayList<>();
    private final Random rnd;
    private int idx = 0;
    private Runnable onDone;
    public final List<Long> lamps = new ArrayList<>();

    public Blueprint(BlockPos origin) {
        this.origin = origin;
        this.rnd = new Random(origin.asLong() * 31L + 7L);
    }

    public Random rnd() { return rnd; }
    public BlockPos origin() { return origin; }
    public int size() { return list.size(); }

    public Blueprint set(int x, int y, int z, BlockState s) {
        list.add(new P(origin.offset(x, y, z).asLong(), s, null));
        return this;
    }

    public Blueprint fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState s) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    set(x, y, z, s);
        return this;
    }

    /** Randomly overwrite some blocks of a box (mossy / cracked variation). */
    public Blueprint sprinkle(int x1, int y1, int z1, int x2, int y2, int z2, BlockState s, double chance) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    if (rnd.nextDouble() < chance) set(x, y, z, s);
        return this;
    }

    public Blueprint lamp(int x, int y, int z) {
        set(x, y, z, ModBlocks.LAB_LAMP.get().defaultBlockState());
        lamps.add(origin.offset(x, y, z).asLong());
        return this;
    }

    public Blueprint terminal(int x, int y, int z, Direction facing, int logId) {
        BlockState st = ModBlocks.ARCHIVE_TERMINAL.get().defaultBlockState()
                .setValue(HorizontalBlock.FACING, facing)
                .setValue(ArchiveTerminalBlock.LOG, logId);
        return set(x, y, z, st);
    }

    public Blueprint chest(int x, int y, int z, Direction facing, ItemStack... items) {
        BlockState st = net.minecraft.block.Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
        final ItemStack[] copy = items;
        list.add(new P(origin.offset(x, y, z).asLong(), st, w -> {
            TileEntity te = w.getBlockEntity(origin.offset(x, y, z));
            if (te instanceof ChestTileEntity) {
                for (int i = 0; i < copy.length; i++) ((ChestTileEntity) te).setItem(13 + i, copy[i].copy());
            }
        }));
        return this;
    }

    public Blueprint onDone(Runnable r) { this.onDone = r; return this; }

    /** Places up to `budget` blocks. Returns true when everything is placed. */
    public boolean run(ServerWorld w, int budget) {
        int n = 0;
        while (idx < list.size() && n < budget) {
            P p = list.get(idx++);
            w.setBlock(BlockPos.of(p.pos), p.st, 2);
            if (p.after != null) p.after.accept(w);
            n++;
        }
        if (idx >= list.size()) {
            if (onDone != null) onDone.run();
            return true;
        }
        return false;
    }
}
