package velrondevs.masterfulbotany.network;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.item.equipment.AlfsteelSword;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

public record AlfSwordLeftClickMessage() implements CustomPacketPayload {

    public static final Type<AlfSwordLeftClickMessage> TYPE = new Type<>(MasterfulBotany.getInstance().resource("alf_sword_left_click"));
    public static final StreamCodec<FriendlyByteBuf, AlfSwordLeftClickMessage> CODEC = StreamCodec.unit(new AlfSwordLeftClickMessage());

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler extends PacketHandler<AlfSwordLeftClickMessage> {

        public Handler() {
            super(TYPE, PacketFlow.SERVERBOUND, CODEC, HandlerThread.MAIN);
        }

        @Override
        public void handle(AlfSwordLeftClickMessage msg, IPayloadContext ctx) {
            if (ctx.player() instanceof ServerPlayer sender && (sender.getItemBySlot(EquipmentSlot.MAINHAND).getItem() == ModItems.alfsteelSword || sender.getItemBySlot(EquipmentSlot.OFFHAND).getItem() == ModItems.alfsteelSword)) {
                ((AlfsteelSword) ModItems.alfsteelSword).trySpawnAlfBurst(sender);
            }
        }
    }
}
