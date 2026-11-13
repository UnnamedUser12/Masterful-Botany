package velrondevs.masterfulbotany.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimPortalHandler;

/** Client-only swirl drawn over the screen while standing in an Alfheim portal. */
public final class AlfheimPortalOverlay {

    private static final Identifier SWIRL_TEXTURE = Identifier.fromNamespaceAndPath("botania", "textures/block/alfheim_portal_swirl.png");

    private AlfheimPortalOverlay() {}

    public static void renderGameOverlay(RenderGuiEvent.Post event) {
        int portalTime = AlfheimPortalHandler.clientInPortalTime;
        if (portalTime > 0 && !(Minecraft.getInstance().screen instanceof PauseScreen)) {
            GuiGraphicsExtractor graphics = event.getGuiGraphics();
            int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int size = Math.max(w, h);
            int x = w < h ? (w - h) / 2 : 0;
            int y = w > h ? (h - w) / 2 : 0;
            float alpha = Mth.clamp((portalTime + event.getPartialTick().getGameTimeDeltaPartialTick(false)) / 120f, 0.05f, 0.8f);
            long gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
            int frame = (int) (gameTime % 16);
            graphics.blit(RenderPipelines.GUI_TEXTURED, SWIRL_TEXTURE, x, y, 0, frame * 48, size, size, 48, 48, 48, 768, ARGB.white(alpha));
        }
    }
}
