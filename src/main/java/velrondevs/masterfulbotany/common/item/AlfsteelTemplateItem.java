package velrondevs.masterfulbotany.common.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SmithingTemplateItem;

public class AlfsteelTemplateItem extends SmithingTemplateItem {

    private static final Identifier EMPTY_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier EMPTY_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier EMPTY_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier EMPTY_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
    private static final Identifier EMPTY_SLOT_AXE = Identifier.withDefaultNamespace("container/slot/axe");
    private static final Identifier EMPTY_SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");
    private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.withDefaultNamespace("container/slot/pickaxe");
    private static final Identifier EMPTY_SLOT_INGOT = Identifier.withDefaultNamespace("container/slot/ingot");

    public AlfsteelTemplateItem(Properties properties) {
        super(
                Component.translatable("tooltip.masterfulbotany.alfsteel_template.applies_to").withStyle(ChatFormatting.BLUE),
                Component.translatable("item.masterfulbotany.alfsteel_ingot"),
                Component.translatable("tooltip.masterfulbotany.alfsteel_template.slot_description", Component.translatable("tooltip.masterfulbotany.alfsteel_template.slot_base")),
                Component.translatable("tooltip.masterfulbotany.alfsteel_template.slot_description", Component.translatable("item.masterfulbotany.alfsteel_ingot")),
                List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS),
                List.of(EMPTY_SLOT_INGOT),
                properties
        );
    }
}
