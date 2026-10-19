package com.thelastplayer.network;

import com.thelastplayer.TheLastPlayer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

/** Small private channel used to send generated speech audio to a single player. */
public final class EchoNetwork {
    private static final String PROTOCOL = "1";
    private static final int MAX_AUDIO_BYTES = 900_000;
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TheLastPlayer.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;

    private EchoNetwork() { }

    public static void register() {
        if (registered) return;
        CHANNEL.registerMessage(0, VoicePacket.class, VoicePacket::encode, VoicePacket::decode, VoicePacket::handle);
        registered = true;
    }

    public static void sendVoice(ServerPlayerEntity player, byte[] audio) {
        if (player == null || audio == null || audio.length == 0 || audio.length > MAX_AUDIO_BYTES) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new VoicePacket(audio));
    }

    private static final class VoicePacket {
        private final byte[] audio;

        private VoicePacket(byte[] audio) {
            this.audio = audio;
        }

        private static void encode(VoicePacket packet, PacketBuffer buffer) {
            buffer.writeByteArray(packet.audio);
        }

        private static VoicePacket decode(PacketBuffer buffer) {
            return new VoicePacket(buffer.readByteArray(MAX_AUDIO_BYTES));
        }

        private static void handle(VoicePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> com.thelastplayer.client.EchoVoiceClient.play(packet.audio)));
            context.setPacketHandled(true);
        }
    }
}
