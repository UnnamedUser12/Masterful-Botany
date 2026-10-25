package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.common.item.equipment.AlfsteelSword;
import velrondevs.masterfulbotany.common.item.equipment.CommonAlfsteelArmor;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * Applies the config dependent values (durability, attribute modifiers) to the default components of the items.
 */
public class ModDefaultComponents {

    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(ModItems.fadedNetherStar, builder -> builder.set(DataComponents.MAX_DAMAGE, MasterfulConfig.flowers.witherAconiteMana));

        int armorDurability = MasterfulConfig.alftools.durability.armor.max_durability();
        event.modify(ModItems.alfsteelHelmet, builder -> builder
                .set(DataComponents.MAX_DAMAGE, armorDurability)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, CommonAlfsteelArmor.createModifiers(ArmorItem.Type.HELMET)));
        event.modify(ModItems.alfsteelChestplate, builder -> builder
                .set(DataComponents.MAX_DAMAGE, armorDurability)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, CommonAlfsteelArmor.createModifiers(ArmorItem.Type.CHESTPLATE)));
        event.modify(ModItems.alfsteelLeggings, builder -> builder
                .set(DataComponents.MAX_DAMAGE, armorDurability)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, CommonAlfsteelArmor.createModifiers(ArmorItem.Type.LEGGINGS)));
        event.modify(ModItems.alfsteelBoots, builder -> builder
                .set(DataComponents.MAX_DAMAGE, armorDurability)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, CommonAlfsteelArmor.createModifiers(ArmorItem.Type.BOOTS)));

        // The tool constructors apply the durability of the tier (terrasteel) after the item properties, so it has to be set here.
        event.modify(ModItems.alfsteelSword, builder -> builder
                .set(DataComponents.MAX_DAMAGE, MasterfulConfig.alftools.durability.sword.max_durability())
                .set(DataComponents.ATTRIBUTE_MODIFIERS, AlfsteelSword.createModifiers()));
        event.modify(ModItems.alfsteelPick, builder -> builder.set(DataComponents.MAX_DAMAGE, MasterfulConfig.alftools.durability.pickaxe.max_durability()));
        event.modify(ModItems.alfsteelAxe, builder -> builder.set(DataComponents.MAX_DAMAGE, MasterfulConfig.alftools.durability.axe.max_durability()));

        event.modify(ModBlocks.mjoellnir, builder -> builder.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, MasterfulConfig.mjoellnir.base_damage_melee - 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, MasterfulConfig.mjoellnir.base_attack_speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build()));
    }
}
