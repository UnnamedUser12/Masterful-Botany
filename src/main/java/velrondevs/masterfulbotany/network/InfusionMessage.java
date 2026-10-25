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

public record InfusionMessage(BlockPos pos, double progress, int fromColor, int toColor) implements CustomPacketPayload {

    public static final Type<InfusionMessage> TYPE = new Type<>(MasterfulBotany.getInstance().resource("infusion"));
    public static final StreamCodec<FriendlyByteBuf, InfusionMessage> CODEC = StreamCodec.ofMember(
            (msg, buffer) -> {
                buffer.writeBlockPos(msg.pos());
                buffer.writeDouble(msg.progress());
                buffer.writeInt(msg.fromColor());
                buffer.writeInt(msg.toColor());
            },
            buffer -> {
                BlockPos pos = buffer.readBlockPos();
                double progress = buffer.readDouble();
                int fromColor = buffer.readInt();
                int toColor = buffer.readInt();
                return new InfusionMessage(pos, progress, fromColor, toColor);
            }
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler extends PacketHandler<InfusionMessage> {

        public Handler() {
            super(TYPE, PacketFlow.CLIENTBOUND, CODEC, HandlerThread.MAIN);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void handle(InfusionMessage msg, IPayloadContext ctx) {
            Level level = Minecraft.getInstance().level;
            //noinspection ConstantConditions
            if (level == null) return;

            int ticks = (int) (100.0 * msg.progress());

            int totalSpiritCount = 3;
            double tickIncrement = 360D / totalSpiritCount;

            int speed = 5;
            double wticks = ticks * speed - tickIncrement;

            double r = Math.sin((ticks - 100) / 10D) * 2;
            double g = Math.sin(wticks * Math.PI / 180 * 0.55);

            for (int i = 0; i < totalSpiritCount; i++) {
                double x = msg.pos().getX() + Math.sin(wticks * Math.PI / 180) * r + 0.5;
                double y = msg.pos().getY() + 0.25 + Math.abs(r) * 0.7;
                double z = msg.pos().getZ() + Math.cos(wticks * Math.PI / 180) * r + 0.5;

                wticks += tickIncrement;
                float fromR = ((msg.fromColor() >> 16) & 0xFF) / 255f;
                float fromG = ((msg.fromColor() >> 8) & 0xFF) / 255f;
                float fromB = ((msg.fromColor()) & 0xFF) / 255f;
                float toR = ((msg.toColor() >> 16) & 0xFF) / 255f;
                float toG = ((msg.toColor() >> 8) & 0xFF) / 255f;
                float toB = ((msg.toColor()) & 0xFF) / 255f;
                float[] colorsfx = new float[] {
                        fromR + ((toR - fromR) * (float) msg.progress()),
                        fromG + ((toG - fromG) * (float) msg.progress()),
                        fromB + ((toB - fromB) * (float) msg.progress())
                };
                WispParticleData data = WispParticleData.wisp(0.85F, colorsfx[0], colorsfx[1], colorsfx[2], 0.25F);
                level.addParticle(data, x, y, z, 0, (float) (-g * 0.05), 0);
                data = WispParticleData.wisp((float) Math.random() * 0.1F + 0.1F, colorsfx[0], colorsfx[1], colorsfx[2], 0.9F);
                level.addParticle(data, x, y, z, (float) (Math.random() - 0.5) * 0.05F, (float) (Math.random() - 0.5) * 0.05F, (float) (Math.random() - 0.5) * 0.05F);

                if (ticks == 100) {
                    for (int j = 0; j < 15; j++) {
                        data = WispParticleData.wisp((float) Math.random() * 0.15F + 0.15F, colorsfx[0], colorsfx[1], colorsfx[2]);
                        level.addParticle(data, msg.pos().getX() + 0.5, msg.pos().getY() + 0.5, msg.pos().getZ() + 0.5, (float) (Math.random() - 0.5F) * 0.125F, (float) (Math.random() - 0.5F) * 0.125F, (float) (Math.random() - 0.5F) * 0.125F);
                    }
                }
            }
        }
    }
}
