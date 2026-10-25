package velrondevs.masterfulbotany.network;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;
import velrondevs.botania.client.fx.WispParticleData;

public record PylonMessage(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PylonMessage> TYPE = new Type<>(MasterfulBotany.getInstance().resource("pylon"));
    public static final StreamCodec<FriendlyByteBuf, PylonMessage> CODEC = StreamCodec.ofMember(
            (msg, buffer) -> buffer.writeBlockPos(msg.pos()),
            buffer -> new PylonMessage(buffer.readBlockPos())
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler extends PacketHandler<PylonMessage> {

        public Handler() {
            super(TYPE, PacketFlow.CLIENTBOUND, CODEC, HandlerThread.MAIN);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void handle(PylonMessage msg, IPayloadContext ctx) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;
            WispParticleData data = WispParticleData.wisp(0.85f, 1f, 0.6f, 0f, 0.25f);
            level.addParticle(data, msg.pos().getX() + 0.25 + (level.random.nextFloat() / 2), msg.pos().getY() + 0.75 + (level.random.nextFloat() / 4), msg.pos().getZ() + 0.25 + (level.random.nextFloat() / 2), 0, 0.3, 0);
        }
    }
}
