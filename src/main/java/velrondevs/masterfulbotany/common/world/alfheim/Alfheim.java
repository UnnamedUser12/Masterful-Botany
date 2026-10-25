package velrondevs.masterfulbotany.common.world.alfheim;

import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class Alfheim {

    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, MasterfulBotany.getInstance().resource("alfheim"));
}
