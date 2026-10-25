package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.common.loot.AlfsteelDisposeModifier;
import velrondevs.masterfulbotany.common.loot.FimbultyrModifier;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Registers the global loot modifier serializers.
 */
public class ModLootModifiers {

    public static void register(RegisterEvent event) {
        MasterfulBotany mod = MasterfulBotany.getInstance();
        event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, mod.resource("dispose"), () -> AlfsteelDisposeModifier.CODEC);
        event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, mod.resource("fimbultyr"), () -> FimbultyrModifier.CODEC);
    }
}
