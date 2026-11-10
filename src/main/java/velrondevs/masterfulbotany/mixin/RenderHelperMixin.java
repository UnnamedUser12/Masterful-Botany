package velrondevs.masterfulbotany.mixin;

import velrondevs.masterfulbotany.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import velrondevs.botania.client.core.helper.RenderHelper;

@Mixin(RenderHelper.class)
public class RenderHelperMixin {

    @Inject(method = "renderHUDBox", at = @At("HEAD"), cancellable = true, remap = false)
    private static void masterfulbotany$noHudBackground(GuiGraphicsExtractor gui, int startX, int startY, int endX, int endY, CallbackInfo ci) {
        if (!ClientConfig.hudBackgrounds.get()) {
            ci.cancel();
        }
    }
}
