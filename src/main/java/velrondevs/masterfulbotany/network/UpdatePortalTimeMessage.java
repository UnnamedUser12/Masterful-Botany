package velrondevs.masterfulbotany.network;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimPortalHandler;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

public record UpdatePortalTimeMessage(int portalTime) implements CustomPacketPayload {

    public static final Type<UpdatePortalTimeMessage> TYPE = new Type<>(MasterfulBotany.getInstance().resource("update_portal_time"));
    public static final StreamCodec<FriendlyByteBuf, UpdatePortalTimeMessage> CODEC = StreamCodec.ofMember(
            (msg, buffer) -> buffer.writeVarInt(msg.portalTime()),
            buffer -> new UpdatePortalTimeMessage(buffer.readVarInt())
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler extends PacketHandler<UpdatePortalTimeMessage> {

        public Handler() {
            super(TYPE, PacketFlow.CLIENTBOUND, CODEC, HandlerThread.MAIN);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void handle(UpdatePortalTimeMessage msg, IPayloadContext ctx) {
            AlfheimPortalHandler.clientInPortalTime = msg.portalTime();
        }
    }
}
