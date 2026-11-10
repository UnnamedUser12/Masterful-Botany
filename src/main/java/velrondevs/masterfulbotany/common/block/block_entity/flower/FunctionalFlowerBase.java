package velrondevs.masterfulbotany.common.block.block_entity.flower;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.flower.BlockFloatingFunctionalFlower;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.moddingx.libx.base.tile.BlockEntityBase;
import org.moddingx.libx.base.tile.TickingBlock;
import org.moddingx.libx.LibX;
import velrondevs.botania.api.block_entity.RadiusDescriptor;
import velrondevs.botania.api.block.Wandable;
import velrondevs.botania.api.block.WandBindable;
import velrondevs.botania.api.block.WandHUD;
import velrondevs.botania.api.BotaniaAPI;
import velrondevs.botania.api.BotaniaAPIClient;
import velrondevs.botania.api.internal.ManaNetwork;
import velrondevs.botania.api.mana.ManaCollector;
import velrondevs.botania.api.mana.ManaPool;
import velrondevs.botania.client.core.helper.RenderHelper;

public abstract class FunctionalFlowerBase extends BlockEntityBase implements TickingBlock, WandBindable, Wandable, WandHUD {

    public static final Identifier POOL_ID = Identifier.fromNamespaceAndPath("botania", "mana_pool");
    public static final Identifier SPREADER_ID = Identifier.fromNamespaceAndPath("botania", "mana_spreader");
    
    public static final int DEFAULT_MAX_MANA = 300;
    public static final int DEFAULT_MAX_TRANSFER = 30;

    public final int maxMana;
    public final int maxTransfer;
    public final int color;
    public final boolean isGenerating;

    @Nullable
    private BlockPos pool = null;
    @Nullable
    private ManaPool poolTile = null;
    @Nullable
    private ManaCollector spreaderTile = null;
    protected int mana = 0;
    private boolean floating = false;

    private transient int sizeLastCheck = -1;
    protected transient int redstoneIn = 0;
    protected transient boolean didWork;

    public FunctionalFlowerBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int color, boolean isGenerating) {
        super(type, pos, state);
        this.color = color;
        this.maxMana = DEFAULT_MAX_MANA;
        this.maxTransfer = isGenerating ? DEFAULT_MAX_TRANSFER : Integer.MAX_VALUE;
        this.isGenerating = isGenerating;
    }

    public FunctionalFlowerBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int color, int maxMana, int maxTransfer, boolean isGenerating) {
        super(type, pos, state);
        this.color = color;
        this.maxMana = maxMana;
        this.maxTransfer = maxTransfer;
        this.isGenerating = isGenerating;
    }

    @Override
    public final void tick() {
        boolean prevFloating = this.floating;
        this.floating = this.getBlockState().getBlock() instanceof BlockFloatingFunctionalFlower<?>;
        if (prevFloating != this.floating)
            this.setChanged();

        this.linkPool();

        if (!this.level.isClientSide()) {
            if (this.isGenerating) {
                if (this.spreaderTile != null) {
                    if (this.mana > 0) {
                        int manaTransfer = Math.min(this.maxTransfer, Math.min(this.mana, this.spreaderTile.getMaxMana() - this.spreaderTile.getCurrentMana()));
                        this.spreaderTile.receiveMana(manaTransfer);
                        this.mana = Mth.clamp(this.mana - manaTransfer, 0, this.maxMana);
                        this.setChanged();
                        this.setPoolChanged();
                    }
                }
            } else {
                if (this.poolTile != null) {
                    if (this.mana < this.maxMana) {
                        int manaTransfer = Math.min(this.maxTransfer, Math.min(this.maxMana - this.mana, this.poolTile.getCurrentMana()));
                        this.poolTile.receiveMana(-manaTransfer);
                        this.mana = Mth.clamp(this.mana + manaTransfer, 0, this.maxMana);
                        this.setChanged();
                        this.setPoolChanged();
                    }
                }
            }

            this.redstoneIn = 0;
            for (Direction dir : Direction.values()) {
                int redstonePower = this.level.getSignal(this.getBlockPos().relative(dir), dir);
                this.redstoneIn = Math.max(redstonePower, this.redstoneIn);
            }
        }

        double particleChance = 1.0D - this.mana / (double) this.maxMana / 3.5;

        this.didWork = false;
        this.tickFlower();

        if (this.level.isClientSide()) {
            if (this.didWork) particleChance = 3 * particleChance;
            float red = (float) (this.color >> 16 & 0xFF) / 255.0f;
            float green = (float) (this.color >> 8 & 0xFF) / 255.0f;
            float blue = (float) (this.color & 255) / 255.0f;
            if (Math.random() > particleChance) {
                BotaniaAPI.instance().sparkleFX(this.getLevel(), (double) this.getBlockPos().getX() + 0.3D + Math.random() * 0.5D, (double) this.getBlockPos().getY() + 0.5D + Math.random() * 0.5D, (double) this.getBlockPos().getZ() + 0.3D + Math.random() * 0.5D, red, green, blue, (float) Math.random(), 5);
            }
        }
    }

    protected abstract void tickFlower();

    @Override
    public boolean canSelect(Player player, ItemStack stack, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public boolean bindTo(Player player, ItemStack stack, BlockPos pos, Direction direction) {
        int range = 10;
        range = range * range;
        double dist = pos.distSqr(this.getBlockPos());
        if ((double) range >= dist) {
            BlockEntity tile = player.level().getBlockEntity(pos);
            if (this.isGenerating && tile instanceof ManaCollector) {
                this.pool = tile.getBlockPos();
                this.spreaderTile = (ManaCollector) tile;
                this.poolTile = null;
                this.setChanged();
                return true;
            } else if (!this.isGenerating && tile instanceof ManaPool) {
                this.pool = tile.getBlockPos();
                this.poolTile = (ManaPool) tile;
                this.spreaderTile = null;
                this.setChanged();
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public BlockPos getBinding() {
        return this.pool;
    }

    public void linkPool() {
        Object theTileObj = this.isGenerating ? this.spreaderTile : this.poolTile;
        BlockEntity theTile;
        if (!(theTileObj instanceof BlockEntity)) {
            theTile = null;
        } else {
            theTile = (BlockEntity) theTileObj;
        }
        if ((this.pool != null && theTile == null) || this.pool != null && !this.pool.equals(theTile.getBlockPos()) || (this.pool != null && this.level.getBlockEntity(this.pool) != theTile)) {
            BlockEntity te = this.level.getBlockEntity(this.pool);
            if (this.isGenerating) {
                this.poolTile = null;
                if (!(te instanceof ManaCollector)) {
                    this.spreaderTile = null;
                } else {
                    this.spreaderTile = (ManaCollector) te;
                }
            } else {
                if (!(te instanceof ManaPool)) {
                    this.poolTile = null;
                } else {
                    this.poolTile = (ManaPool) te;
                }
                this.spreaderTile = null;
            }
        }

        if (this.pool == null) {
            if (this.isGenerating) {
                ManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
                int size = network.getAllCollectorsInWorld(this.getLevel()).size();
                if (size != this.sizeLastCheck) {
                    ManaCollector te = network.getClosestCollector(this.getBlockPos(), this.getLevel(), 10);
                    if (te != null) {
                        this.pool = te.getManaReceiverPos();
                        this.poolTile = null;
                        this.spreaderTile = te;
                        this.setChanged();
                    }
                    this.sizeLastCheck = size;
                }
            } else {
                ManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
                int size = network.getAllPoolsInWorld(this.getLevel()).size();
                if (size != this.sizeLastCheck) {
                    ManaPool te = network.getClosestPool(this.getBlockPos(), this.getLevel(), 10);
                    if (te != null) {
                        this.pool = te.getManaReceiverPos();
                        this.poolTile = te;
                        this.spreaderTile = null;
                        this.setChanged();
                    }
                    this.sizeLastCheck = size;
                }
            }
        }

        this.setChanged();
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        this.mana = Mth.clamp(input.getIntOr("mana", 0), 0, this.maxMana);
        this.pool = input.child("pool")
                .map(poolTag -> new BlockPos(poolTag.getIntOr("x", 0), poolTag.getIntOr("y", 0), poolTag.getIntOr("z", 0)))
                .orElse(null);
        this.floating = input.getBooleanOr("floating", false);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("mana", Mth.clamp(this.mana, 0, this.maxMana));
        if (this.pool != null) {
            ValueOutput poolTag = output.child("pool");
            poolTag.putInt("x", this.pool.getX());
            poolTag.putInt("y", this.pool.getY());
            poolTag.putInt("z", this.pool.getZ());
        }
        output.putBoolean("floating", this.floating);
    }

    public boolean isValidBinding() {
        Object theTileObj = this.isGenerating ? this.spreaderTile : this.poolTile;
        if (!(theTileObj instanceof BlockEntity theTile))
            return false;
        return this.pool != null && theTile != null && theTile.hasLevel() && !theTile.isRemoved() && this.level.hasChunkAt(theTile.getBlockPos()) && this.getLevel().getBlockEntity(this.pool) == theTile;
    }

    public int getCurrentMana() {
        return this.mana;
    }

    public boolean isFloating() {
        return this.floating;
    }

    public void setFloating(boolean floating) {
        this.floating = floating;
        this.setChanged();
    }

    public RadiusDescriptor getRadius() {
        return null;
    }
    
    public RadiusDescriptor getSecondaryRadius() {
        return null;
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        if (this.level != null && this.level.isClientSide()) {
            LibX.getNetwork().requestBE(this.level, this.worldPosition);
        }
        return true;
    }
    
    @Override
    public void renderHUD(GuiGraphicsExtractor graphics, Minecraft minecraft) {
        if (this.level == null) return;
        String name = I18n.get(this.getBlockState().getBlock().getDescriptionId());

        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
        int left = (Math.max(102, minecraft.font.width(name)) + 4) / 2;
        int right = left + 20;

        RenderHelper.renderHUDBox(graphics, centerX - left, centerY + 8, centerX + right, centerY + 30);
        BotaniaAPIClient.instance().drawComplexManaHUD(graphics, this.color, this.getCurrentMana(), this.maxMana, name, new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.getValue(this.isGenerating ? SPREADER_ID : POOL_ID))), this.isValidBinding());
    }
    
    private void setPoolChanged() {
        if (this.poolTile instanceof BlockEntity) {
            ((BlockEntity) this.poolTile).setChanged();
        }
        if (this.spreaderTile instanceof BlockEntity) {
            ((BlockEntity) this.spreaderTile).setChanged();
        }
    }
}
