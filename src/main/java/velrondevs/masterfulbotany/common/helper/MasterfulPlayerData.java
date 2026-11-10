package velrondevs.masterfulbotany.common.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class MasterfulPlayerData {

    private static final String KEY = "MasterfulBotanyPlayerInfo";

    public static CompoundTag getData(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(KEY)) {
            return persistent.getCompoundOrEmpty(KEY);
        } else {
            CompoundTag nbt = new CompoundTag();
            persistent.put(KEY, nbt);
            return nbt;
        }
    }

    public static boolean has(Player player, String flag) {
        return getData(player).getBooleanOr(flag, false);
    }

    public static void copy(Player source, Player target) {
        if (source.getPersistentData().contains(KEY)) {
            target.getPersistentData().put(KEY, source.getPersistentData().getCompoundOrEmpty(KEY).copy());
        }
    }
}
