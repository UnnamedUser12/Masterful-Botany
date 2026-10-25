package velrondevs.masterfulbotany.common.item.equipment.bauble;

import velrondevs.masterfulbotany.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import velrondevs.botania.common.item.equipment.bauble.BaubleItem;

public class ItemIceRing extends BaubleItem {

    public ItemIceRing(Properties properties) {
        super(properties);
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity player) {
        if (player.level().isClientSide) {
            this.displayParticles(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void displayParticles(LivingEntity player) {
        if (Math.random() < 0.03 && (ClientConfig.ringParticles.get() || player != Minecraft.getInstance().player)) {
            player.getCommandSenderWorld().addParticle(ParticleTypes.ITEM_SNOWBALL,
                    player.getX(),
                    player.getY() + 1.85,
                    player.getZ(),
                    0, 0, 0);
        }
    }
}
