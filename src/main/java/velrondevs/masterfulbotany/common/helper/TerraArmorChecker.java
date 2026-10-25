package velrondevs.masterfulbotany.common.helper;

import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import velrondevs.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;

public class TerraArmorChecker {
    
    public static boolean hasTerraHelmet(Player player) {
        return ((TerrasteelHelmItem) ModItems.alfsteelHelmet).hasArmorSet(player);
    }
}
