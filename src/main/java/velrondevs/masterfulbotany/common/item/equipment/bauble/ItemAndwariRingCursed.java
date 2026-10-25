package velrondevs.masterfulbotany.common.item.equipment.bauble;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import velrondevs.botania.api.mana.ManaItemHandler;
import velrondevs.botania.common.item.equipment.bauble.BaubleItem;

public class ItemAndwariRingCursed extends BaubleItem {
    
    public ItemAndwariRingCursed(Properties properties) {
        super(properties);
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity entity) {
        super.onWornTick(stack, entity);
        if (entity instanceof Player) {
            ManaItemHandler.instance().requestMana(stack, (Player) entity, 500, true);
        }
        if (entity.tickCount % 20 == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 30, 0));
        }
    }
}
