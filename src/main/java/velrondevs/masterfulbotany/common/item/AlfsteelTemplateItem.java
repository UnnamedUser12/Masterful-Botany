package velrondevs.masterfulbotany.common.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

public class AlfsteelTemplateItem extends SmithingTemplateItem {

    private static final ResourceLocation EMPTY_SLOT_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_SLOT_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_SLOT_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SLOT_AXE = ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_SLOT_INGOT = ResourceLocation.withDefaultNamespace("item/empty_slot_ingot");
    
    public AlfsteelTemplateItem() {
        super(
                Component.translatable("tooltip.masterfulbotany.alfsteel_template.applies_to").withStyle(ChatFormatting.BLUE),
                Component.translatable("item.masterfulbotany.alfsteel_ingot"),
                Component.translatable("tooltip.masterfulbotany.alfsteel_template.upgrade").withStyle(ChatFormatting.BLUE),
                Component.translatable("tooltip.masterfulbotany.alfsteel_template.slot_description", Component.translatable("tooltip.masterfulbotany.alfsteel_template.slot_base")),
                Component.translatable("tooltip.masterfulbotany.alfsteel_template.slot_description", Component.translatable("item.masterfulbotany.alfsteel_ingot")),
                List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS),
                List.of(EMPTY_SLOT_INGOT)
        );
    }
}
