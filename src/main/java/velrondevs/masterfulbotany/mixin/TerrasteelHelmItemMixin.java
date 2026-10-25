package velrondevs.masterfulbotany.mixin;

import velrondevs.masterfulbotany.common.helper.TerraArmorChecker;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import velrondevs.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;

/**
 * Makes the alfsteel armor set count as a terrasteel armor set. This replaces the
 * {@code terra_helmet_check} coremod of the original mod.
 */
@Mixin(TerrasteelHelmItem.class)
public class TerrasteelHelmItemMixin {

    @Inject(method = "hasTerraArmorSet", at = @At("HEAD"), cancellable = true, remap = false)
    private static void masterfulbotany$alfsteelCountsAsTerrasteel(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (TerraArmorChecker.hasTerraHelmet(player)) {
            cir.setReturnValue(true);
        }
    }
}
