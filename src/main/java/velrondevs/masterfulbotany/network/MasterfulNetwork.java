package velrondevs.masterfulbotany.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

public class MasterfulNetwork extends NetworkX {

    public MasterfulNetwork(ModX mod) {
        super(mod);
        this.register(new ParticleMessage.Handler());
        this.register(new InfusionMessage.Handler());
        this.register(new PylonMessage.Handler());
        this.register(new UpdatePortalTimeMessage.Handler());
        this.register(new MagnetImmunityMessage.Handler());

        this.register(new AlfSwordLeftClickMessage.Handler());
    }

    @Override
    protected String getVersion() {
        return "8";
    }

    public void spawnParticle(Level level, SimpleParticleType particle, int amount, double x, double y, double z, double xm, double ym, double zm, double xd, double yd, double zd) {
        this.spawnParticle(level, particle, amount, x, y, z, xm, ym, zm, xd, yd, zd, false);
    }

    public void spawnParticle(Level level, SimpleParticleType particle, int amount, double x, double y, double z, double xm, double ym, double zm, double xd, double yd, double zd, boolean randomizePosition) {
        if (level.isClientSide) {
            for (int i = 0; i < amount; i++) {
                if (randomizePosition) {
                    level.addParticle(particle,
                            x + (level.random.nextDouble() * 2 * xd) - xd,
                            y + (level.random.nextDouble() * 2 * yd) - yd,
                            z + (level.random.nextDouble() * 2 * zd) - zd,
                            xm, ym, zm);
                } else {
                    level.addParticle(particle, x, y, z,
                            xm + (level.random.nextDouble() * 2 * xd) - xd,
                            ym + (level.random.nextDouble() * 2 * yd) - yd,
                            zm + (level.random.nextDouble() * 2 * zd) - zd);
                }
            }
        } else if (level instanceof ServerLevel serverLevel) {
            ResourceLocation id = BuiltInRegistries.PARTICLE_TYPE.getKey(particle);
            if (id == null) return;
            PacketDistributor.sendToPlayersNear(serverLevel, null, x, y, z, 100, new ParticleMessage(id, x, y, z, amount, xm, ym, zm, xd, yd, zd, randomizePosition));
        }
    }

    public void spawnInfusionParticles(Level level, BlockPos pos, double progress, int fromColor, int toColor) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new InfusionMessage(pos, progress, fromColor, toColor));
        }
    }

    public void updatePortalTime(ServerPlayer player, int portalTime) {
        if (!player.getCommandSenderWorld().isClientSide) {
            PacketDistributor.sendToPlayer(player, new UpdatePortalTimeMessage(portalTime));
        }
    }

    public void setItemMagnetImmune(ItemEntity ie) {
        if (!ie.level().isClientSide && !ie.getPersistentData().getBoolean("PreventRemoteMovement")) {
            ie.getPersistentData().putBoolean("PreventRemoteMovement", true);
            PacketDistributor.sendToPlayersTrackingEntity(ie, new MagnetImmunityMessage(ie.getId(), true, ie.getX(), ie.getY(), ie.getZ()));
        }
    }

    public void removeItemMagnetImmune(ItemEntity ie) {
        if (!ie.level().isClientSide && ie.getPersistentData().getBoolean("PreventRemoteMovement")) {
            ie.getPersistentData().putBoolean("PreventRemoteMovement", false);
            PacketDistributor.sendToPlayersTrackingEntity(ie, new MagnetImmunityMessage(ie.getId(), false, ie.getX(), ie.getY(), ie.getZ()));
        }
    }
}
