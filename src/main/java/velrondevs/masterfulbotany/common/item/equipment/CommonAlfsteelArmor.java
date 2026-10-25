package velrondevs.masterfulbotany.common.item.equipment;

import java.util.List;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;

public class CommonAlfsteelArmor {

    /**
     * Builds the attribute modifiers for an alfsteel armor piece. Armor and toughness come from the config
     * as the material stats are the ones from terrasteel.
     */
    public static ItemAttributeModifiers createModifiers(ArmorItem.Type type) {
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(type.getSlot());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ARMOR, new AttributeModifier(id("armor/" + type.getName()), getDefense(type), AttributeModifier.Operation.ADD_VALUE), group);
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id("armor_toughness/" + type.getName()), getToughness(type), AttributeModifier.Operation.ADD_VALUE), group);
        switch (type) {
            case HELMET -> builder.add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(id("reach/" + type.getName()), MasterfulConfig.alftools.reach_modifier, AttributeModifier.Operation.ADD_VALUE), group);
            case CHESTPLATE -> builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id("knockback_resistance/" + type.getName()), MasterfulConfig.alftools.knockback_resistance_modifier, AttributeModifier.Operation.ADD_VALUE), group);
            case LEGGINGS -> {
                builder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(id("speed/" + type.getName()), MasterfulConfig.alftools.speed_modifier, AttributeModifier.Operation.ADD_VALUE), group);
                builder.add(NeoForgeMod.SWIM_SPEED, new AttributeModifier(id("swim_speed/" + type.getName()), MasterfulConfig.alftools.speed_modifier, AttributeModifier.Operation.ADD_VALUE), group);
            }
            default -> {}
        }
        return builder.build();
    }

    private static ResourceLocation id(String path) {
        return MasterfulBotany.getInstance().resource("alfsteel_" + path);
    }

    public static void addArmorSetDescription(ItemStack stack, List<Component> list) {
        if (stack.getItem() == ModItems.alfsteelHelmet) {
            list.add(Component.translatable("item.masterfulbotany.alfsteel_helmet.description").withStyle(ChatFormatting.GOLD));
        } else if (stack.getItem() == ModItems.alfsteelChestplate) {
            list.add(Component.translatable("item.masterfulbotany.alfsteel_chestplate.description").withStyle(ChatFormatting.GOLD));
        } else if (stack.getItem() == ModItems.alfsteelLeggings) {
            list.add(Component.translatable("item.masterfulbotany.alfsteel_leggings.description").withStyle(ChatFormatting.GOLD));
        } else if (stack.getItem() == ModItems.alfsteelBoots) {
            list.add(Component.translatable("item.masterfulbotany.alfsteel_boots.description").withStyle(ChatFormatting.GOLD));
        }
    }

    public static boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        if (player == null) {
            return false;
        } else {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                return false;
            } else {
                return switch (slot) {
                    case HEAD -> stack.getItem() == ModItems.alfsteelHelmet;
                    case CHEST -> stack.getItem() == ModItems.alfsteelChestplate;
                    case LEGS -> stack.getItem() == ModItems.alfsteelLeggings;
                    case FEET -> stack.getItem() == ModItems.alfsteelBoots;
                    default -> false;
                };
            }
        }
    }

    public static int getDefense(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> MasterfulConfig.alftools.armor_values.helmet.defense();
            case CHESTPLATE, BODY -> MasterfulConfig.alftools.armor_values.chestplate.defense();
            case LEGGINGS -> MasterfulConfig.alftools.armor_values.leggings.defense();
            case BOOTS -> MasterfulConfig.alftools.armor_values.boots.defense();
        };
    }

    public static float getToughness(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> MasterfulConfig.alftools.armor_values.helmet.toughness();
            case CHESTPLATE, BODY -> MasterfulConfig.alftools.armor_values.chestplate.toughness();
            case LEGGINGS -> MasterfulConfig.alftools.armor_values.leggings.toughness();
            case BOOTS -> MasterfulConfig.alftools.armor_values.boots.toughness();
        };
    }
}
