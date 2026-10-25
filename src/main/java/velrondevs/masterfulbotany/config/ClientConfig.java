package velrondevs.masterfulbotany.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec CLIENT_CONFIG;
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    static {
        init(CLIENT_BUILDER);
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static ModConfigSpec.ConfigValue<Boolean> ringParticles;
    public static ModConfigSpec.ConfigValue<Boolean> hudBackgrounds;

    public static void init(ModConfigSpec.Builder builder) {
        ringParticles = builder.comment("Set to false to disable particles from the masterfulbotany rings for your own player. You'll still see them from other players.").define("ring_particles", true);
        hudBackgrounds = builder.comment("Set to false to disable the background on Botania HUDs.").define("hud_backgrounds", true);
    }
}
