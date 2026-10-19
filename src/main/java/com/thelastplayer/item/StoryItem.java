package com.thelastplayer.item;

import com.thelastplayer.story.EchoTexts;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

/** Right-click to read. Used for the shard, the developer letter and the hidden memory log. */
public class StoryItem extends Item {
    private final String id;
    private final boolean foil;

    public StoryItem(Properties props, String id, boolean foil) {
        super(props);
        this.id = id;
        this.foil = foil;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return foil;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide && player instanceof ServerPlayerEntity) {
            for (String line : EchoTexts.itemText(id)) {
                player.sendMessage(new StringTextComponent(line), Util.NIL_UUID);
            }
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }
}
