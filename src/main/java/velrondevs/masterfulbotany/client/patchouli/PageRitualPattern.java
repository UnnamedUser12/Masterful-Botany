package velrondevs.masterfulbotany.client.patchouli;

import velrondevs.masterfulbotany.common.crafting.RuneRitualRecipe;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.client.book.gui.GuiBook;

public class PageRitualPattern extends PageRuneRitualBase {

    public static final Identifier OVERLAY_TEXTURE = MasterfulBotany.getInstance().id("textures/gui/patchouli_ritual_pattern.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, 0, 9, 0, 0, 116, 116, 256, 256);
        if (this.recipe != null) {
            this.addRune(graphics, this.recipe.getCenterRune(), 0, 0, true, mouseX, mouseY);
            for (RuneRitualRecipe.RunePosition rune : this.recipe.getRunes()) {
                this.addRune(graphics, rune.getRune(), rune.getX(), rune.getZ(), rune.isConsumed(), mouseX, mouseY);
            }
        }
    }

    private void addRune(GuiGraphicsExtractor graphics, Ingredient rune, int x, int z, boolean consume, int mouseX, int mouseY) {
        int realX = 3 + (10 * (x + 5));
        int realY = 12 + (10 * ((-z) + 5));
        ItemStack[] stacks = rune.items().map(ItemStack::new).toArray(ItemStack[]::new);
        if (stacks.length > 0) {
            ItemStack stack = stacks[(this.parent.getTicksInBook() / 20) % stacks.length];
            graphics.pose().pushMatrix();
            graphics.pose().translate(realX - 1, realY - 1);
            graphics.pose().scale(12 / 16f, 12 / 16f);
            graphics.item(stack, 0, 0);
            graphics.itemDecorations(net.minecraft.client.Minecraft.getInstance().font, stack, realX, realY);
            graphics.pose().popMatrix();
            if (this.parent.isMouseInRelativeRange(mouseX, mouseY, realX, realY, 10, 10)) {
                this.parent.setTooltipStack(stack);
                boolean keep = (x != 0 || z != 0) && !consume;
                String text1 = keep ? I18n.get("tooltip.masterfulbotany.rune_offset", x, z) : I18n.get("tooltip.masterfulbotany.rune_central");
                int color1 = ChatFormatting.GOLD.getColor() == null ? 0x000000 : ChatFormatting.GOLD.getColor();

                String text2 = I18n.get(keep ? "tooltip.masterfulbotany.rune_keep" : "tooltip.masterfulbotany.rune_consume");
                Integer colorInt2 = keep ? ChatFormatting.DARK_GREEN.getColor() : ChatFormatting.DARK_RED.getColor();
                int color2 = colorInt2 == null ? 0x000000 : colorInt2;

                graphics.pose().pushMatrix();
                graphics.pose().translate(GuiBook.PAGE_WIDTH / 2, 126);
                graphics.pose().scale(0.7f, 0.7f);
                this.parent.drawCenteredStringNoShadow(graphics, text1, 0, 0, color1);
                this.parent.drawCenteredStringNoShadow(graphics, text2, 0, 2 + net.minecraft.client.Minecraft.getInstance().font.lineHeight, color2);
                graphics.pose().popMatrix();
            }
        }
    }
}
