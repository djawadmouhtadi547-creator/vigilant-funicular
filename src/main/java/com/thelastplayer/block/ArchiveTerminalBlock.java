package com.thelastplayer.block;

import com.thelastplayer.story.StoryManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/** Old terminal that holds one story log (property "log" = log id 0..15). */
public class ArchiveTerminalBlock extends HorizontalBlock {
    public static final IntegerProperty LOG = IntegerProperty.create("log", 0, 15);

    public ArchiveTerminalBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LOG, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOG);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide && player instanceof ServerPlayerEntity) {
            StoryManager.readTerminal((ServerPlayerEntity) player, state.getValue(LOG), pos);
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
}
