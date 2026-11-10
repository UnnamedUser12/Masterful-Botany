package velrondevs.masterfulbotany.common.item.equipment;

import java.util.List;
import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.pylon.PylonRepairable;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModItems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import velrondevs.botania.common.item.equipment.tool.terrasteel.TerraTruncatorItem;

public class AlfsteelAxe extends TerraTruncatorItem implements PylonRepairable {

    public static final int ITEM_COLLECT_RANGE = 8;

    public AlfsteelAxe(Properties props) {
        super(props.durability(MasterfulConfig.alftools.durability.axe.max_durability()));
    }

    @Override
    public int getManaPerDamage() {
        return MasterfulConfig.alftools.durability.axe.mana_per_durability();
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player != null) {
            if (!player.isShiftKeyDown()) {
                return super.useOn(ctx);
            }
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull Level level, Player player, @Nonnull InteractionHand hand) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            player.setItemInHand(hand, stack);
        }
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(x - ITEM_COLLECT_RANGE, y - ITEM_COLLECT_RANGE, z - ITEM_COLLECT_RANGE, x + ITEM_COLLECT_RANGE, y + ITEM_COLLECT_RANGE, z + ITEM_COLLECT_RANGE));
        for (ItemEntity item : items) {
            item.snapTo(x + level.getRandom().nextFloat() - 0.5f, y + level.getRandom().nextFloat(), z + level.getRandom().nextFloat() - 0.5f, item.getYRot(), item.getXRot());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getRepairManaPerTick(ItemStack stack) {
        return (int) (2.5 * this.getManaPerDamage());
    }

    @Override
    public ItemStack repairOneTick(ItemStack stack) {
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - 5));
        return stack;
    }
}
