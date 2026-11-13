package velrondevs.masterfulbotany.network;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

public record ParticleMessage(Identifier particleId, double x, double y, double z, int amount, double xm, double ym, double zm, double xd, double yd, double zd, boolean randomizePosition) implements CustomPacketPayload {

    public static final Type<ParticleMessage> TYPE = new Type<>(MasterfulBotany.getInstance().id("particle"));
    public static final StreamCodec<FriendlyByteBuf, ParticleMessage> CODEC = StreamCodec.ofMember(
            (msg, buffer) -> {
                buffer.writeIdentifier(msg.particleId());
                buffer.writeDouble(msg.x());
                buffer.writeDouble(msg.y());
                buffer.writeDouble(msg.z());
                buffer.writeInt(msg.amount());
                buffer.writeDouble(msg.xm());
                buffer.writeDouble(msg.ym());
                buffer.writeDouble(msg.zm());
                buffer.writeDouble(msg.xd());
                buffer.writeDouble(msg.yd());
                buffer.writeDouble(msg.zd());
                buffer.writeBoolean(msg.randomizePosition());
            },
            buffer -> {
                Identifier particleId = buffer.readIdentifier();
                double x = buffer.readDouble();
                double y = buffer.readDouble();
                double z = buffer.readDouble();
                int amount = buffer.readInt();
                double xm = buffer.readDouble();
                double ym = buffer.readDouble();
                double zm = buffer.readDouble();
                double xd = buffer.readDouble();
                double yd = buffer.readDouble();
                double zd = buffer.readDouble();
                boolean randomizePosition = buffer.readBoolean();
                return new ParticleMessage(particleId, x, y, z, amount, xm, ym, zm, xd, yd, zd, randomizePosition);
            }
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler extends PacketHandler<ParticleMessage> {

        public Handler() {
            super(TYPE, PacketFlow.CLIENTBOUND, CODEC, HandlerThread.MAIN);
        }

        @Override
        public void handle(ParticleMessage msg, IPayloadContext ctx) {
            Level level = ctx.player().level();
            if (level == null) return;
            ParticleType<?> particle = BuiltInRegistries.PARTICLE_TYPE.getValue(msg.particleId());
            if (particle instanceof SimpleParticleType) {
                MasterfulBotany.getNetwork().spawnParticle(level, (SimpleParticleType) particle, msg.amount(), msg.x(), msg.y(), msg.z(), msg.xm(), msg.ym(), msg.zm(), msg.xd(), msg.yd(), msg.zd(), msg.randomizePosition());
            }
        }
    }
}
