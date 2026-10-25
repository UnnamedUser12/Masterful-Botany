package velrondevs.masterfulbotany.common.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public class MasterfulPlayerData {
    
    public static CompoundTag getData(Player player) {
        if (player.getPersistentData().contains("MasterfulBotanyPlayerInfo", Tag.TAG_COMPOUND)) {
            return player.getPersistentData().getCompound("MasterfulBotanyPlayerInfo");
        } else {
            CompoundTag nbt = new CompoundTag();
            player.getPersistentData().put("MasterfulBotanyPlayerInfo", nbt);
            return nbt;
        }
    }
    
    public static void copy(Player source, Player target) {
        if (source.getPersistentData().contains("MasterfulBotanyPlayerInfo", Tag.TAG_COMPOUND)) {
            target.getPersistentData().put("MasterfulBotanyPlayerInfo", source.getPersistentData().getCompound("MasterfulBotanyPlayerInfo").copy());
        }
    }
}
