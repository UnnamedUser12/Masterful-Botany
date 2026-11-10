package velrondevs.masterfulbotany.common.item.equipment;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.pylon.PylonRepairable;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.moddingx.libx.util.lazy.LazyValue;
import velrondevs.botania.api.mana.ManaItemHandler;
import velrondevs.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;
import velrondevs.botania.common.item.equipment.tool.ToolCommons;

public class AlfsteelHelm extends TerrasteelHelmItem implements PylonRepairable {

    private static final LazyValue<ItemStack[]> armorSet = new LazyValue<>(() -> new ItemStack[]{new ItemStack(ModItems.alfsteelHelmet), new ItemStack(ModItems.alfsteelChestplate), new ItemStack(ModItems.alfsteelLeggings), new ItemStack(ModItems.alfsteelBoots)});

    public AlfsteelHelm(Properties props) {
        super(props);
    }

    @Override
    public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
        return MasterfulBotany.getInstance().modid + ":textures/model/armor_alfsteel.png";
    }

    @Override
    protected int getManaPerDamage() {
        return MasterfulConfig.alftools.durability.armor.mana_per_durability();
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull ServerLevel level, @Nonnull Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof Player player && player.getItemBySlot(this.getEquipmentSlot()) == stack) {
            if (stack.getDamageValue() > 0 && ManaItemHandler.instance().requestManaExact(stack, player, this.getManaPerDamage() * 2, true)) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - 2));
            }
            int food = player.getFoodData().getFoodLevel();
            if (food > 0 && food < 18 && player.isHurt() && player.tickCount % 80 == 0) {
                player.heal(1);
            }
            if (player.tickCount % 10 == 0) {
                ManaItemHandler.instance().dispatchManaExact(stack, player, 10, true);
            }
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return ToolCommons.damageItemIfPossible(stack, amount, entity, this.getManaPerDamage());
    }

    @Override
    public ItemStack[] getArmorSetStacks() {
        return armorSet.get();
    }

    @Override
    public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        return CommonAlfsteelArmor.hasArmorSetItem(player, slot);
    }

    @Override
    public void addArmorSetDescription(ItemStack stack, Consumer<Component> list) {
        super.addArmorSetDescription(stack, list);
        CommonAlfsteelArmor.addArmorSetDescription(stack, list);
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

    public int getDefense() {
        return CommonAlfsteelArmor.getDefense(this.getType());
    }

    public float getToughness() {
        return CommonAlfsteelArmor.getToughness(this.getType());
    }
}
