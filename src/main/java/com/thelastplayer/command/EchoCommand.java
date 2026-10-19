package com.thelastplayer.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.thelastplayer.registry.ModItems;
import com.thelastplayer.story.StoryData;
import com.thelastplayer.story.StoryManager;
import com.thelastplayer.structure.StoryStructures;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;

/** /echo choice <id> (used by clickable chat) and /echo dev ... (ops, for testing). */
public class EchoCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> d = event.getDispatcher();
        d.register(Commands.literal("echo")
                .then(Commands.literal("choice")
                        .then(Commands.argument("id", StringArgumentType.word()).executes(ctx -> {
                            ServerPlayerEntity p = ctx.getSource().getPlayerOrException();
                            StoryManager.onChoice(p, StringArgumentType.getString(ctx, "id"));
                            return 1;
                        })))
                .then(Commands.literal("dev").requires(src -> src.hasPermission(2))
                        .then(Commands.literal("day").then(Commands.argument("n", IntegerArgumentType.integer(1, 4)).executes(ctx -> {
                            StoryManager.devSetDay(ctx.getSource().getServer(), IntegerArgumentType.getInteger(ctx, "n"));
                            ctx.getSource().sendSuccess(new StringTextComponent("day advanced"), false);
                            return 1;
                        })))
                        .then(Commands.literal("tp").then(Commands.argument("where", StringArgumentType.word()).executes(ctx -> {
                            ServerPlayerEntity p = ctx.getSource().getPlayerOrException();
                            StoryData sd = StoryData.get(ctx.getSource().getServer());
                            BlockPos t = null;
                            switch (StringArgumentType.getString(ctx, "where")) {
                                case "device": t = sd.devicePos; break;
                                case "a": t = sd.hiddenA; break;
                                case "b": t = sd.hiddenB; break;
                                case "lab": t = sd.labPos; break;
                                case "final": t = sd.labPos == null ? null : sd.labPos.offset(0, 0, 44); break;
                                default: break;
                            }
                            if (t == null) { ctx.getSource().sendFailure(new StringTextComponent("not built yet")); return 0; }
                            p.teleportTo(t.getX() + 0.5, t.getY() + 1, t.getZ() + 0.5);
                            return 1;
                        })))
                        .then(Commands.literal("items").executes(ctx -> {
                            ServerPlayerEntity p = ctx.getSource().getPlayerOrException();
                            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(ModItems.BLUE_GLASS_SHARD.get()));
                            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(ModItems.OLD_DEVELOPER_LETTER.get()));
                            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(ModItems.MEMORY_LOG.get()));
                            return 1;
                        }))
                        .then(Commands.literal("unlockfinal").executes(ctx -> {
                            StoryData sd = StoryData.get(ctx.getSource().getServer());
                            sd.set("reunion_done");
                            sd.bond = Math.max(sd.bond, 5);
                            sd.setDirty();
                            return 1;
                        }))
                        .then(Commands.literal("bond").then(Commands.argument("v", IntegerArgumentType.integer(-5, 10)).executes(ctx -> {
                            StoryData sd = StoryData.get(ctx.getSource().getServer());
                            sd.bond = IntegerArgumentType.getInteger(ctx, "v");
                            sd.setDirty();
                            return 1;
                        })))
                        .then(Commands.literal("status").executes(ctx -> {
                            StoryData sd = StoryData.get(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(new StringTextComponent("day=" + sd.day + " bond=" + sd.bond
                                    + " phase=" + sd.finalPhase + " ending=" + sd.ending + " logs=" + sd.logs
                                    + " flags=" + sd.flags), false);
                            return 1;
                        }))));
    }
}
