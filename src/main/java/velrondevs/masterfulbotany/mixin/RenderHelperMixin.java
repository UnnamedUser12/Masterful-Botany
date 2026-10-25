package velrondevs.masterfulbotany.mixin;

import velrondevs.masterfulbotany.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import velrondevs.botania.client.core.helper.RenderHelper;

/**
 * Allows disabling the background of Botania HUDs via the client config. This replaces the
 * {@code hud_backgrounds} coremod of the original mod.
 */
@Mixin(RenderHelper.class)
public class RenderHelperMixin {

    @Inject(method = "renderHUDBox", at = @At("HEAD"), cancellable = true, remap = false)
    private static void masterfulbotany$noHudBackground(GuiGraphics gui, int startX, int startY, int endX, int endY, CallbackInfo ci) {
        if (!ClientConfig.hudBackgrounds.get()) {
            ci.cancel();
        }
    }
}
