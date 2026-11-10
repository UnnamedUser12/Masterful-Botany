package velrondevs.masterfulbotany.common.block.block_entity.flower;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.moddingx.libx.LibX;
import velrondevs.botania.api.block_entity.RadiusDescriptor;
import velrondevs.botania.api.block.PetalApothecary;
import velrondevs.botania.client.fx.WispParticleData;

public class Aquapanthus extends FunctionalFlowerBase {

    public static final int MAX_TICK_TO_NEXT_CHECK = 5;
    public static final int MANA_PER_TICK = 2;
    public static final int TICKS_TO_FILL = 20;
    public static final Set<Identifier> FILLING_SLOW_IDS = ImmutableSet.of(
            Identifier.fromNamespaceAndPath("exnihilosequentia", "barrel_wood"),
            Identifier.fromNamespaceAndPath("exnihilosequentia", "barrel_stone"),
            Identifier.fromNamespaceAndPath("excompressum", "oak_crucible"),
            Identifier.fromNamespaceAndPath("excompressum", "spruce_crucible"),
            Identifier.fromNamespaceAndPath("excompressum", "birch_crucible"),
            Identifier.fromNamespaceAndPath("excompressum", "jungle_crucible"),
            Identifier.fromNamespaceAndPath("excompressum", "acacia_crucible"),
            Identifier.fromNamespaceAndPath("excompressum", "dark_oak_crucible")
    );
    public static final Set<Identifier> FILLING_FAST_IDS = ImmutableSet.of(
            Identifier.fromNamespaceAndPath("exnihilosequentia", "crucible_wood"),
            Identifier.fromNamespaceAndPath("exnihilosequentia", "crucible_fired")
    );

    private transient int tickToNextCheck = 0;
    @Nullable
    private BlockPos currentlyFilling = null;
    private int fillingSince = 0;

    public Aquapanthus(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0x4444FF, false);
    }

    @Override
    protected void tickFlower() {
        if (!this.level.isClientSide()) {
            if (this.currentlyFilling != null) {
                if (this.mana >= MANA_PER_TICK) {
                    if (this.fill()) {
                        this.mana = Mth.clamp(this.mana - MANA_PER_TICK, 0, this.maxMana);
                        this.didWork = true;
                        this.fillingSince += 1;
                    } else {
                        this.fillingSince = 0;
                        this.currentlyFilling = null;
                    }
                }
                if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) LibX.getNetwork().updateBE(serverLevel, this.worldPosition);
                this.setChanged();
            } else {
                if (this.tickToNextCheck > 0) {
                    this.tickToNextCheck -= 1;
                    return;
                }
                this.tickToNextCheck = MAX_TICK_TO_NEXT_CHECK;

                BlockPos basePos = this.worldPosition.immutable();
                outer: for (int xd = -3; xd <= 3; xd++) {
                    for (int zd = -3; zd <= 3; zd++) {
                        BlockPos pos = basePos.offset(xd, 0, zd);
                        BlockState state = this.level.getBlockState(pos);
                        BlockEntity te = this.level.getBlockEntity(pos);
                        if (this.canFill(state, te)) {
                            this.currentlyFilling = pos;
                            this.fillingSince = 0;
                            this.setChanged();
                            break outer;
                        }
                    }
                }
            }
        } else {
            if (this.currentlyFilling != null && this.fillingSince > 0) {
                double progress = this.fillingSince / (double) TICKS_TO_FILL;

                double x = ((this.currentlyFilling.getX() - this.worldPosition.getX()) * progress) + this.worldPosition.getX() + 0.5;
                double y = this.worldPosition.getY() + (1.5 * Math.sin(progress * Math.PI));
                double z = ((this.currentlyFilling.getZ() - this.worldPosition.getZ()) * progress) + this.worldPosition.getZ() + 0.5;

                double xd = ((this.currentlyFilling.getX() - this.worldPosition.getX()) * progress) / 10;
                double yd = Math.sin(progress * Math.PI) / 10;
                double zd = ((this.currentlyFilling.getZ() - this.worldPosition.getZ()) * progress) / 10;

                WispParticleData data = WispParticleData.wisp(0.85F, 0.1f, 0.1f, 1, 0.25F);
                this.level.addParticle(data, x, y, z, xd, yd, zd);
                data = WispParticleData.wisp((float) Math.random() * 0.1F + 0.1F, 0.2f, 0.2f, 1, 0.9F);
                this.level.addParticle(data, x, y, z, (float) (Math.random() - 0.5) * 0.05F, (float) (Math.random() - 0.5) * 0.05F, (float) (Math.random() - 0.5) * 0.05F);
            }
        }
    }

    private boolean canFill(BlockState state, @Nullable BlockEntity te) {
        if (state.getBlock() == Blocks.CAULDRON || (state.getBlock() == Blocks.WATER_CAULDRON && state.getValue(LayeredCauldronBlock.LEVEL) < 3)) {
            return true;
        } else if (te instanceof PetalApothecary && ((PetalApothecary) te).getFluid() == PetalApothecary.State.EMPTY) {
            return true;
        } else if ((FILLING_SLOW_IDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock())) || FILLING_FAST_IDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) && te != null) {
            ResourceHandler<FluidResource> handler = this.level.getCapability(Capabilities.Fluid.BLOCK, te.getBlockPos(), Direction.UP);
            if (handler != null) {
                FluidResource water = FluidResource.of(Fluids.WATER);
                int amount = FILLING_FAST_IDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock())) ? FluidType.BUCKET_VOLUME : (FluidType.BUCKET_VOLUME / 3) + 1;
                int filled;
                try (Transaction transaction = Transaction.openRoot()) {
                    filled = handler.insert(water, amount, transaction);
                }
                return filled > 0 && handler.size() > 0 && handler.getAmountAsInt(0) < handler.getCapacityAsInt(0, water);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    private boolean fill() {
        BlockState state = this.level.getBlockState(this.currentlyFilling);
        BlockEntity be = this.level.getBlockEntity(this.currentlyFilling);
        if (state.getBlock() == Blocks.CAULDRON || (state.getBlock() == Blocks.WATER_CAULDRON && state.getValue(LayeredCauldronBlock.LEVEL) < 3) || (be instanceof PetalApothecary && ((PetalApothecary) be).getFluid() == PetalApothecary.State.EMPTY)) {
            if (this.fillingSince >= TICKS_TO_FILL) {
                if (state.getBlock() == Blocks.CAULDRON) {
                    this.level.setBlockAndUpdate(this.currentlyFilling, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
                } else if (state.getBlock() == Blocks.WATER_CAULDRON) {
                    this.level.setBlockAndUpdate(this.currentlyFilling, state.setValue(LayeredCauldronBlock.LEVEL, Mth.clamp(state.getValue(LayeredCauldronBlock.LEVEL) + 1, 0, 3)));
                } else if (be instanceof PetalApothecary) {
                    ((PetalApothecary) be).setFluid(PetalApothecary.State.WATER);
                    be.setChanged();
                }
                return false;
            }
            return true;
        } else if ((FILLING_SLOW_IDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock())) || FILLING_FAST_IDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) && be != null) {
            if (this.fillingSince >= TICKS_TO_FILL) {
                ResourceHandler<FluidResource> handler = this.level.getCapability(Capabilities.Fluid.BLOCK, be.getBlockPos(), Direction.UP);
                if (handler != null) {
                    int amount = FILLING_FAST_IDS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock())) ? FluidType.BUCKET_VOLUME : (FluidType.BUCKET_VOLUME / 3) + 1;
                    try (Transaction transaction = Transaction.openRoot()) {
                        handler.insert(FluidResource.of(Fluids.WATER), amount, transaction);
                        transaction.commit();
                    }
                    be.setChanged();
                }
                return false;
            } else {
                return this.canFill(state, be);
            }
        } else {
            return false;
        }
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(this.worldPosition, 3);
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        this.currentlyFilling = input.child("waterFilling")
                .map(fillingTag -> new BlockPos(fillingTag.getIntOr("x", 0), fillingTag.getIntOr("y", 0), fillingTag.getIntOr("z", 0)))
                .orElse(null);
        this.fillingSince = input.getIntOr("filling_since", 0);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        if (this.currentlyFilling != null) {
            ValueOutput fillingTag = output.child("waterFilling");
            fillingTag.putInt("x", this.currentlyFilling.getX());
            fillingTag.putInt("y", this.currentlyFilling.getY());
            fillingTag.putInt("z", this.currentlyFilling.getZ());
            output.putInt("filling_since", this.fillingSince);
        }
    }
}
