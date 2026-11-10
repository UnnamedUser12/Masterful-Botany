package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.common.item.equipment.AlfsteelSword;
import velrondevs.masterfulbotany.common.item.equipment.CommonAlfsteelArmor;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

public class ModDefaultComponents {

    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(ModItems.fadedNetherStar, builder -> builder.set(DataComponents.MAX_DAMAGE, MasterfulConfig.flowers.witherAconiteMana));

        for (Item repairable : new Item[]{ModItems.alfsteelSword, ModItems.alfsteelPick, ModItems.alfsteelAxe, ModItems.alfsteelHelmet, ModItems.alfsteelChestplate, ModItems.alfsteelLeggings, ModItems.alfsteelBoots}) {
            event.modify(repairable, builder -> builder.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(ModItems.alfsteelIngot.builtInRegistryHolder()))));
        }

        int armorDurability = MasterfulConfig.alftools.durability.armor.max_durability();
        event.modify(ModItems.alfsteelHelmet, builder -> builder
                .set(DataComponents.MAX_DAMAGE, armorDurability)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, CommonAlfsteelArmor.createModifiers(ArmorType.HELMET)));
        event.modify(ModItems.alfsteelChestplate, builder -> builder
                .set(DataComponents.MAX_DAMAGE, armorDurability)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, CommonAlfsteelArmor.createModifiers(ArmorType.CHESTPLATE)));
        event.modify(ModItems.alfsteelLeggings, builder -> builder
                .set(DataComponents.MAX_DAMAGE, armorDurability)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, CommonAlfsteelArmor.createModifiers(ArmorType.LEGGINGS)));
        event.modify(ModItems.alfsteelBoots, builder -> builder
                .set(DataComponents.MAX_DAMAGE, armorDurability)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, CommonAlfsteelArmor.createModifiers(ArmorType.BOOTS)));

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
