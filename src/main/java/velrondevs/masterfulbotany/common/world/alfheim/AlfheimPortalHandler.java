package velrondevs.masterfulbotany.common.world.alfheim;

import java.util.*;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class AlfheimPortalHandler {

    public static int clientInPortalTime;

    private static final Identifier SWIRL_TEXTURE = Identifier.fromNamespaceAndPath("botania", "textures/block/alfheim_portal_swirl.png");
    
    private static final Set<ServerPlayer> inPortal = new HashSet<>();
    private static final Set<ServerPlayer> portalBlocked = new HashSet<>();
    private static final Map<ServerPlayer, Integer> timesInPortal = new HashMap<>();

    public static void serverStarted(ServerStartedEvent event) {
        inPortal.clear();
        portalBlocked.clear();
        timesInPortal.clear();
    }
    
    public static void endTick(ServerTickEvent.Post event) {
        {
            Iterator<ServerPlayer> itr = timesInPortal.keySet().iterator();
            while (itr.hasNext()) {
                ServerPlayer player = itr.next();
                if (!inPortal.contains(player)) {
                    MasterfulBotany.getNetwork().updatePortalTime(player, 0);
                    itr.remove();
                }
            }
            
            itr = portalBlocked.iterator();
            while (itr.hasNext()) {
                ServerPlayer player = itr.next();
                if (canRemovePortalBlocked(player)) {
                    MasterfulBotany.getNetwork().updatePortalTime(player, 0);
                    itr.remove();
                }
            }
            
            portalBlocked.removeIf(AlfheimPortalHandler::canRemovePortalBlocked);
            inPortal.clear();
        }
    }
    
    private static boolean canRemovePortalBlocked(ServerPlayer player) {
        if (player.isOnPortalCooldown()) return false;
        if (inPortal.contains(player)) return false;
        BlockPos pos = player.blockPosition();
        return player.level().isOutsideBuildHeight(pos) || player.level().isLoaded(pos);
    }
    
    public static boolean setInPortal(Level level, Player playerEntity) {
        if (!level.isClientSide() && (MasterfulConfig.enableAlfheim || Objects.equals(level.dimension(), Alfheim.DIMENSION)) && playerEntity instanceof ServerPlayer player) {
            inPortal.add(player);
            if (!portalBlocked.contains(player) && !playerEntity.isOnPortalCooldown()) {
                int timeInPortal;
                if (!timesInPortal.containsKey(player)) {
                    timesInPortal.put(player, 1);
                    timeInPortal = 1;
                } else {
                    timeInPortal = timesInPortal.get(player) + 1;
                    timesInPortal.put(player, timeInPortal);
                }
                MasterfulBotany.getNetwork().updatePortalTime(player, timeInPortal);
                if (timeInPortal >= 120) {
                    portalBlocked.add(player);
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean shouldCheck(Level level) {
        if (!MasterfulConfig.enableAlfheim && !Objects.equals(level.dimension(), Alfheim.DIMENSION)) {
            return false;
        } else if (level instanceof ServerLevel) {
            return !timesInPortal.isEmpty() || !portalBlocked.isEmpty() || ((ServerLevel) level).getServer().getTickCount() % 4 == 3;
        } else {
            return false;
        }
    }
    
    public static void renderGameOverlay(RenderGuiEvent.Post event) {
        if (clientInPortalTime > 0 && !(Minecraft.getInstance().screen instanceof PauseScreen)) {
            GuiGraphicsExtractor graphics = event.getGuiGraphics();
            int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int size = Math.max(w, h);
            int x = w < h ? (w - h) / 2 : 0;
            int y = w > h ? (h - w) / 2 : 0;
            float alpha = Mth.clamp((clientInPortalTime + event.getPartialTick().getGameTimeDeltaPartialTick(false)) / 120f, 0.05f, 0.8f);
            long gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
            int frame = (int) (gameTime % 16);
            graphics.blit(RenderPipelines.GUI_TEXTURED, SWIRL_TEXTURE, x, y, 0, frame * 48, size, size, 48, 48, 48, 768, ARGB.white(alpha));
        }
    }
}
