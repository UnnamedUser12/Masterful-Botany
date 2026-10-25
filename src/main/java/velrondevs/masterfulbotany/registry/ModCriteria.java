package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.common.advancement.AlfRepairTrigger;
import velrondevs.masterfulbotany.common.advancement.MjoellnirTrigger;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

public class ModCriteria {

    public static final AlfRepairTrigger ALF_REPAIR = new AlfRepairTrigger();
    public static final MjoellnirTrigger MJOELLNIR = new MjoellnirTrigger();

    public static void register(RegisterEvent event) {
        MasterfulBotany mod = MasterfulBotany.getInstance();
        event.register(Registries.TRIGGER_TYPE, mod.resource("alf_repair"), () -> ALF_REPAIR);
        event.register(Registries.TRIGGER_TYPE, mod.resource("mjoellnir"), () -> MJOELLNIR);
    }
}
