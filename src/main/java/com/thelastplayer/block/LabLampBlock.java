package com.thelastplayer.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;

/** Lab lamp: LIT=true gives light 15. The final scene switches them off. */
public class LabLampBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public LabLampBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}
