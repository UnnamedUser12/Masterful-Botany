package velrondevs.masterfulbotany.network;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

public record MagnetImmunityMessage(int entityId, boolean immune, double x, double y, double z) implements CustomPacketPayload {

    public static final Type<MagnetImmunityMessage> TYPE = new Type<>(MasterfulBotany.getInstance().resource("magnet_immunity"));
    public static final StreamCodec<FriendlyByteBuf, MagnetImmunityMessage> CODEC = StreamCodec.ofMember(
            (msg, buffer) -> {
                buffer.writeInt(msg.entityId());
                buffer.writeBoolean(msg.immune());
                buffer.writeDouble(msg.x());
                buffer.writeDouble(msg.y());
                buffer.writeDouble(msg.z());
            },
            buffer -> {
                int entityId = buffer.readInt();
                boolean immune = buffer.readBoolean();
                double x = buffer.readDouble();
                double y = buffer.readDouble();
                double z = buffer.readDouble();
                return new MagnetImmunityMessage(entityId, immune, x, y, z);
            }
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler extends PacketHandler<MagnetImmunityMessage> {

        public Handler() {
            super(TYPE, PacketFlow.CLIENTBOUND, CODEC, HandlerThread.MAIN);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void handle(MagnetImmunityMessage msg, IPayloadContext ctx) {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                Entity entity = level.getEntity(msg.entityId());
                if (entity != null) {
                    entity.getPersistentData().putBoolean("PreventRemoteMovement", msg.immune());
                    entity.setPos(msg.x(), msg.y(), msg.z());
                }
            }
        }
    }
}
