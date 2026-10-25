package velrondevs.masterfulbotany.common.item.equipment;

import java.util.List;
import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.pylon.PylonRepairable;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.network.AlfSwordLeftClickMessage;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import velrondevs.botania.api.internal.ManaBurst;
import velrondevs.botania.common.entity.ManaBurstEntity;
import velrondevs.botania.common.item.equipment.tool.terrasteel.TerraBladeItem;
import velrondevs.botania.common.lib.BotaniaTags;
import velrondevs.botania.registry.BotaniaSounds;

public class AlfsteelSword extends TerraBladeItem implements PylonRepairable {

    public AlfsteelSword(Properties props) {
        super(props.durability(MasterfulConfig.alftools.durability.sword.max_durability()));
        NeoForge.EVENT_BUS.addListener(this::onLeftClick);
    }

    public static ItemAttributeModifiers createModifiers() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, MasterfulConfig.alftools.tool_values.sword_damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, MasterfulConfig.alftools.tool_values.sword_speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    private void onLeftClick(PlayerInteractEvent.LeftClickEmpty evt) {
        if (!evt.getItemStack().isEmpty() && evt.getItemStack().getItem() == this) {
            PacketDistributor.sendToServer(new AlfSwordLeftClickMessage());
        }
    }

    @Override
    public int getManaPerDamage() {
        return 2 * super.getManaPerDamage();
    }

    public float getDamage() {
        return (float) MasterfulConfig.alftools.tool_values.sword_damage;
    }

    @Override
    public boolean isValidRepairItem(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
        return repair.getItem() == ModItems.alfsteelIngot || (!Ingredient.of(BotaniaTags.Items.INGOTS_TERRASTEEL).test(repair) && super.isValidRepairItem(toRepair, repair));
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

    public ManaBurstEntity getAlfBurst(Player player, ItemStack stack) {
        ManaBurstEntity burst = TerraBladeItem.getBurst(player, stack);
        burst.setColor(0xF79100);
        burst.setMana(this.getManaPerDamage());
        burst.setStartingMana(this.getManaPerDamage());
        burst.setMinManaLoss(20);
        burst.setManaLossPerTick(2.0F);
        return burst;
    }

    public void trySpawnAlfBurst(Player player) {
        if ((player.getItemBySlot(EquipmentSlot.MAINHAND).getItem() == ModItems.alfsteelSword) && player.getAttackStrengthScale(0) == 1) {
            ManaBurstEntity burst = this.getAlfBurst(player, player.getMainHandItem());
            player.level().addFreshEntity(burst);
            player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.terraBlade, SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void updateBurst(ManaBurst burst, ItemStack stack) {
        Projectile entity = burst.entity();
        AABB aabb = new AABB(
                entity.getX(), entity.getY(), entity.getZ(),
                entity.xOld, entity.yOld, entity.zOld
        ).inflate(1);
        Entity thrower = entity.getOwner();
        List<LivingEntity> entities = entity.level().getEntitiesOfClass(LivingEntity.class, aabb);

        for (LivingEntity living : entities) {
            if (living == thrower || living instanceof Player livingPlayer && thrower instanceof Player throwingPlayer && !throwingPlayer.canHarmPlayer(livingPlayer)) {
                continue;
            }

            if (living.hurtTime == 0) {
                int mana = burst.getMana();
                if (mana >= 33) {
                    burst.setMana(mana - 33);
                    float damage = 4 + this.getDamage();
                    if (!burst.isFake() && !entity.level().isClientSide) {
                        DamageSource source = entity.level().damageSources().magic();
                        if (thrower instanceof Player player) {
                            source = entity.level().damageSources().playerAttack(player);
                        } else if (thrower instanceof LivingEntity livingThrower) {
                            source = entity.level().damageSources().mobAttack(livingThrower);
                        }
                        living.hurt(source, damage);
                        if (burst.getMana() <= 0) entity.discard();
                    }
                }
            }
        }
    }
}
