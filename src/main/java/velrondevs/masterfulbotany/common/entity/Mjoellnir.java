package velrondevs.masterfulbotany.common.entity;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.BlockMjoellnir;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.event.EventListener;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModCriteria;
import velrondevs.masterfulbotany.registry.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.EventHooks;

public class Mjoellnir extends Projectile {

    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(Mjoellnir.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(Mjoellnir.class, EntityDataSerializers.ITEM_STACK);

    private boolean returning = false;
    private ItemStack stack = ItemStack.EMPTY;
    private UUID thrower = null;
    private Vec3 throwPos = null;
    private int hotbarSlot = 0;
    private int lifeLeft = 40;

    public Mjoellnir(Level level) {
        this(ModBlocks.mjoellnir.getEntityType(), level);
    }

    public Mjoellnir(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(@Nonnull SynchedEntityData.Builder builder) {
        builder.define(RETURNING, false);
        builder.define(STACK, ItemStack.EMPTY);
    }

    @Override
    public void onSyncedDataUpdated(@Nonnull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (RETURNING.equals(key)) {
            this.returning = this.entityData.get(RETURNING);
        } else if (STACK.equals(key)) {
            this.stack = this.entityData.get(STACK);
        }
    }

    @Override
    public void tick() {
        super.tick();

        boolean returningTickStart = this.returning;

        if (!this.returning) {
            this.checkForCollision();
        } else if (!this.level().isClientSide()) {
            Vec3 returnPoint = this.getReturnPoint();
            if (returnPoint != null) {
                this.applyReturnMotion(returnPoint);
            }
            this.tryReturn(returnPoint);
        }

        Vec3 motion = this.getDeltaMovement();
        if (returningTickStart) {
            Vec3 position = this.position();
            this.setPos(position.x + motion.x, position.y + motion.y, position.z + motion.z);
        } else {
            this.move(MoverType.SELF, motion);
        }

        this.lifeLeft -= 1;

        if (this.lifeLeft <= 0) {
            this.startReturn();
        }

        Vec3 position = this.position();
        this.setPos(position.x, position.y, position.z);
    }

    private void checkForCollision() {
        Vec3 motion = this.getDeltaMovement();
        Vec3 position = this.position();
        Vec3 rayCast = position.add(motion);

        if (this.isAlive() && !this.entityData.get(RETURNING)) {
            HitResult result = ProjectileUtil.getEntityHitResult(this.level(), this, position, rayCast, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1),
                    entity -> entity != this && !entity.isSpectator() && entity.isAlive() && entity.isPickable() && entity != this.getThrower());

            if (result == null || result.getType() == HitResult.Type.MISS) {
                result = this.level().clip(new ClipContext(position, rayCast, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            }
            if (result != null && result.getType() != HitResult.Type.MISS) {
                if (!this.level().isClientSide()) {
                    this.onHit(result);
                } else {
                    Vec3 returnPoint = this.getReturnPoint();
                    if (returnPoint != null) {
                        this.applyReturnMotionClient(returnPoint);
                    }
                }
            }
        }
    }

    @Override
    protected void onHit(@Nonnull HitResult result) {
        if (!this.returning) {
            super.onHit(result);
            this.startReturn();
        }
    }

    @Override
    protected void onHitEntity(@Nonnull EntityHitResult result) {
        if (result.getEntity() instanceof LivingEntity) {
            this.attackEntities((LivingEntity) result.getEntity());
        }
    }

    @Override
    protected void onHitBlock(@Nonnull BlockHitResult hit) {
        AABB aabb = new AABB(hit.getLocation(), hit.getLocation()).inflate(2);
        List<LivingEntity> living = this.level().getEntitiesOfClass(LivingEntity.class, aabb, entity -> !entity.isSpectator() && entity.isAlive() && entity != this.getThrower());
        if (!living.isEmpty()) {
            this.attackEntities(living.get(this.level().getRandom().nextInt(living.size())));
        }
    }

    @Override
    public boolean canTeleport(@Nonnull Level from, @Nonnull Level to) {
        return false;
    }
    
    @Nullable
    private Vec3 getReturnPoint() {
        Player throwerEntity = this.getThrower();
        if (throwerEntity != null) {
            return throwerEntity.position();
        } else if (this.throwPos != null) {
            return this.throwPos;
        } else {
            return null;
        }
    }

    private void applyReturnMotion(@Nonnull Vec3 returnPoint) {
        if (!this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();
            Vec3 position = this.position();
            Vec3 returnVec = new Vec3(returnPoint.x - position.x, returnPoint.y - position.y, returnPoint.z - position.z).normalize().multiply(0.6, 0.6, 0.6);
            double loyalty = 1 + (0.07 * Mth.clamp(ModEnchantments.level(this.stack, Enchantments.LOYALTY), 0, 3));
            Vec3 newMotion = new Vec3(((3 * motion.x) + returnVec.x) / 4, ((3 * motion.y) + returnVec.y) / 4, ((3 * motion.z) + returnVec.z) / 4).multiply(loyalty, loyalty, loyalty);
            this.setDeltaMovement(newMotion);
        }
    }

    private void applyReturnMotionClient(@Nonnull Vec3 returnPoint) {
        if (this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();
            Vec3 position = this.position();
            Vec3 returnVec = new Vec3(returnPoint.x - position.x, returnPoint.y - position.y, returnPoint.z - position.z).normalize().multiply(0.6, 0.6, 0.6);
            Vec3 newMotion = new Vec3(((3 * motion.x) + returnVec.x) / 4, ((3 * motion.y) + returnVec.y) / 4, ((3 * motion.z) + returnVec.z) / 4);
            this.setDeltaMovement(newMotion);
        }
    }

    private void tryReturn(@Nullable Vec3 returnPoint) {
        if (returnPoint == null) {
            BlockMjoellnir.putInWorld(this.stack, this.level(), this.blockPosition());
            this.remove(RemovalReason.DISCARDED);
        } else if (returnPoint.distanceToSqr(this.position()) < 2) {
            Player throwerEntity = this.getThrower();
            if (throwerEntity != null) {
                if (!BlockMjoellnir.putInInventory(throwerEntity, this.stack, this.hotbarSlot)) {
                    BlockMjoellnir.putInWorld(this.stack, this.level(), BlockPos.containing(returnPoint));
                }
            } else {
                BlockMjoellnir.putInWorld(this.stack, this.level(), BlockPos.containing(returnPoint));
            }
            this.remove(RemovalReason.DISCARDED);
        }
    }

    private void startReturn() {
        if (!this.returning) {
            this.setLifeLeft(0);
            this.setReturning(true);
        }
    }

    private void attackEntities(LivingEntity target) {
        AABB aabb = new AABB(target.position(), target.position()).inflate(4);
        List<LivingEntity> found = this.level().getEntitiesOfClass(LivingEntity.class, aabb, entity -> !entity.isSpectator() && entity.isAlive() && entity != this.getThrower());
        LightningBolt lightning = this.attackEntity(target);
        for (LivingEntity entity : found) {
            if (entity != target) {
                this.areaDamage(entity, lightning);
            }
        }
    }

    @Nullable
    private LightningBolt attackEntity(LivingEntity target) {
        if (!this.level().isClientSide()) {
            if (ModEnchantments.level(this.stack, Enchantments.FLAME) >= 1) {
                target.igniteForSeconds(5);
            }
            int knockback = ModEnchantments.level(this.stack, Enchantments.PUNCH);
            if (knockback > 0) {
                Vec3 vector3d = this.getDeltaMovement().multiply(1, 0, 1).normalize().scale(knockback * 0.6);
                if (vector3d.lengthSqr() > 0) {
                    target.push(vector3d.x, 0.1D, vector3d.z);
                }
            }
            int power = ModEnchantments.level(this.stack, Enchantments.POWER);
            float dmg = MasterfulConfig.mjoellnir.base_damage_ranged + 1;
            if (power > 0) {
                dmg += (MasterfulConfig.mjoellnir.enchantment_multiplier * (1.0f + Math.max(0, power - 1) * 0.5f));
            }
            Player thrower = this.getThrower();
            if (thrower instanceof ServerPlayer) {
                ModCriteria.MJOELLNIR.trigger((ServerPlayer) thrower, this.getStack(), target);
            }
            target.hurtServer((ServerLevel) this.level(), thrower == null ? this.level().damageSources().generic() : this.level().damageSources().mobProjectile(this, thrower), dmg);
            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, this.level());
            lightning.setPos(target.getX(), target.getY(), target.getZ());
            lightning.setVisualOnly(true);
            lightning.setCause(thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
            this.level().addFreshEntity(lightning);
            if (this.level() instanceof ServerLevel && !EventHooks.onEntityStruckByLightning(target, lightning)) {
                int fireTicks = target.getRemainingFireTicks();
                LivingEntity oldImmune = EventListener.lightningImmuneEntity;
                EventListener.lightningImmuneEntity = target;
                target.thunderHit((ServerLevel) this.level(), lightning);
                EventListener.lightningImmuneEntity = oldImmune;
                target.setRemainingFireTicks(fireTicks);
            }
            return lightning;
        } else {
            return null;
        }
    }

    private void areaDamage(LivingEntity target, @Nullable LightningBolt lightning) {
        if (ModEnchantments.level(this.stack, Enchantments.FLAME) >= 1) {
            target.igniteForSeconds(2);
        }
        int power = ModEnchantments.level(this.stack, Enchantments.POWER);
        float dmg = MasterfulConfig.mjoellnir.base_damage_ranged + 1;
        if (power > 0) {
            dmg += (MasterfulConfig.mjoellnir.enchantment_multiplier * (1.0f + Math.max(0, power - 1) * 0.5f));
        }
        dmg *= MasterfulConfig.mjoellnir.secondary_target_multiplier;
        Player thrower = this.getThrower();
        if (this.level() instanceof ServerLevel serverLevel) {
            target.hurtServer(serverLevel, thrower == null ? this.level().damageSources().generic() : this.level().damageSources().mobProjectile(this, thrower), dmg);
        }
        if (lightning != null && this.level().getRandom().nextFloat() < MasterfulConfig.mjoellnir.secondary_lightning_chance) {
            int fireTicks = target.getRemainingFireTicks();
            LivingEntity oldImmune = EventListener.lightningImmuneEntity;
            EventListener.lightningImmuneEntity = target;
            target.thunderHit((ServerLevel) this.level(), lightning);
            EventListener.lightningImmuneEntity = oldImmune;
            target.setRemainingFireTicks(fireTicks);
        }
    }

    public boolean isReturning() {
        return this.returning;
    }

    public void setReturning(boolean returning) {
        this.entityData.set(RETURNING, returning);
        this.returning = returning;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.entityData.set(STACK, stack);
        this.stack = stack;
    }

    @Nullable
    public Player getThrower() {
        if (this.thrower == null) {
            return null;
        } else {
            return this.level().getPlayerByUUID(this.thrower);
        }
    }

    @Nullable
    public UUID getThrowerId() {
        return this.thrower;
    }

    public void setThrower(@Nullable Player thrower) {
        this.setThrowerId(thrower == null ? null : thrower.getUUID());
    }

    public void setThrowerId(@Nullable UUID thrower) {
        this.thrower = thrower;
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return this.getThrower();
    }

    @Nullable
    public Vec3 getThrowPos() {
        return this.throwPos;
    }

    public void setThrowPos(@Nullable Vec3 throwPos) {
        this.throwPos = throwPos;
    }

    public int getHotBarSlot() {
        return this.hotbarSlot;
    }

    public void setHotBarSlot(int hotBarSlot) {
        this.hotbarSlot = hotBarSlot;
    }

    public int getLifeLeft() {
        return this.lifeLeft;
    }

    public void setLifeLeft(int lifeLeft) {
        this.lifeLeft = lifeLeft;
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Stack", ItemStack.OPTIONAL_CODEC, this.stack);
        if (this.thrower != null) {
            output.putBoolean("thrower", true);
            output.putLong("throwerl", this.thrower.getLeastSignificantBits());
            output.putLong("throwerm", this.thrower.getMostSignificantBits());
        } else {
            output.putBoolean("thrower", false);
        }
        if (this.throwPos != null) {
            output.putBoolean("throwPos", true);
            output.putDouble("throwX", this.throwPos.x);
            output.putDouble("throwY", this.throwPos.y);
            output.putDouble("throwZ", this.throwPos.z);
        } else {
            output.putBoolean("throwPos", false);
        }
        output.putInt("hotbar", this.hotbarSlot);
        output.putInt("lifeTime", this.lifeLeft);
        output.putBoolean("returning", this.returning);
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setStack(input.read("Stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        if (input.getBooleanOr("thrower", false)) {
            this.setThrowerId(new UUID(input.getLongOr("throwerm", 0L), input.getLongOr("throwerl", 0L)));
        } else {
            this.setThrowerId(null);
        }
        if (input.getBooleanOr("throwPos", false)) {
            this.setThrowPos(new Vec3(input.getDoubleOr("throwX", 0), input.getDoubleOr("throwY", 0), input.getDoubleOr("throwZ", 0)));
        } else {
            this.setThrowPos(null);
        }
        this.setHotBarSlot(input.getIntOr("hotbar", 0));
        this.setLifeLeft(input.getIntOr("lifeTime", 40));
        this.setReturning(input.getBooleanOr("returning", false));
    }
}
