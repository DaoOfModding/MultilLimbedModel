package DaoOfModding.mlmanimator.Client.Physics;

import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.UUID;

public class GravityClientPlayerEntity extends ClientPlayerEntity
{
    private int noJumpDelay;

    private static final UUID SLOW_FALLING_ID = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA");
    private static final AttributeModifier SLOW_FALLING = new AttributeModifier(SLOW_FALLING_ID, "Slow falling acceleration reduction", -0.07, AttributeModifier.Operation.ADDITION); // Add -0.07 to 0.08 so we get the vanilla default of 0.01

    public GravityClientPlayerEntity(Minecraft p_i232461_1_, ClientWorld p_i232461_2_, ClientPlayNetHandler p_i232461_3_, StatisticsManager p_i232461_4_, ClientRecipeBook p_i232461_5_, boolean p_i232461_6_, boolean p_i232461_7_)
    {
        super(p_i232461_1_, p_i232461_2_, p_i232461_3_, p_i232461_4_, p_i232461_5_, p_i232461_6_, p_i232461_7_);
    }

    public void aiStep()
    {
        // TODO: ...movement stuff goes here, ugh...
        super.aiStep();
    }

    public void travel(Vector3d p_213352_1_)
    {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        if (this.isSwimming() && !this.isPassenger())
        {
            double d3 = this.getLookAngle().y;
            double d4 = d3 < -0.2D ? 0.085D : 0.06D;
            if (d3 <= 0.0D || this.jumping || !this.level.getBlockState(new BlockPos(this.getX(), this.getY() + 1.0D - 0.1D, this.getZ())).getFluidState().isEmpty())
            {
                Vector3d vector3d1 = this.getDeltaMovement();
                vector3d1 = vector3d1.add(getGravityHandler().rotateVectorDown(new Vector3d(0.0D, (d3 - vector3d1.y) * d4, 0.0D)));
                this.setDeltaMovement(vector3d1);
            }
        }

        if (this.abilities.flying && !this.isPassenger())
        {
            float f = this.flyingSpeed;
            this.flyingSpeed = this.abilities.getFlyingSpeed() * (float)(this.isSprinting() ? 2 : 1);
            LivingEntityTravel(p_213352_1_);

            Vector3d vector3d = this.getDeltaMovement();
            Vector3d movementMask = getGravityHandler().rotateVectorDown(new Vector3d(1, 0.6, 1));

            this.setDeltaMovement(vector3d.multiply(movementMask));
            this.flyingSpeed = f;
            this.fallDistance = 0.0F;
            this.setSharedFlag(7, false);
        } else {
            LivingEntityTravel(p_213352_1_);
        }

        this.checkMovementStatistics(this.getX() - d0, this.getY() - d1, this.getZ() - d2);
    }

    public void LivingEntityTravel(Vector3d p_213352_1_)
    {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance())
        {
            ModifiableAttributeInstance gravity = this.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());

            Vector3f downVector = getGravityHandler().getDownVector();
            Vector3f upVector = downVector.copy();
            upVector.mul(-1f);

            Vector3d falling = getDeltaMovement().multiply(upVector.x(), upVector.y(), upVector.z());

            boolean flag = (falling.x + falling.y + falling.z) <= 0.0D;
            if (flag && this.hasEffect(Effects.SLOW_FALLING))
            {
                if (!gravity.hasModifier(SLOW_FALLING)) gravity.addTransientModifier(SLOW_FALLING);
                this.fallDistance = 0.0F;
            }
            else if (gravity.hasModifier(SLOW_FALLING))
                gravity.removeModifier(SLOW_FALLING);

            double d0 = gravity.getValue();

            FluidState fluidstate = this.level.getFluidState(this.blockPosition());
            if (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType()))
            {
                double d8 = this.getY();
                float f5 = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float f6 = 0.02F;
                float f7 = (float) EnchantmentHelper.getDepthStrider(this);
                if (f7 > 3.0F) {
                    f7 = 3.0F;
                }

                if (!this.onGround) {
                    f7 *= 0.5F;
                }

                if (f7 > 0.0F) {
                    f5 += (0.54600006F - f5) * f7 / 3.0F;
                    f6 += (this.getSpeed() - f6) * f7 / 3.0F;
                }

                if (this.hasEffect(Effects.DOLPHINS_GRACE)) {
                    f5 = 0.96F;
                }

                f6 *= (float)this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
                this.moveRelative(f6, p_213352_1_);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vector3d vector3d6 = this.getDeltaMovement();
                if (this.horizontalCollision && this.onClimbable())
                {
                    Vector3d toAdd = getGravityHandler().rotateVectorDown(new Vector3d(0, 0.2, 0));
                    Vector3d toMult = getGravityHandler().rotateVectorDown(new Vector3d(1, 0, 1));
                    vector3d6 = vector3d6.multiply(toMult).add(toAdd);
                    //vector3d6 = new Vector3d(vector3d6.x, 0.2D, vector3d6.z);
                }

                Vector3d toMult = getGravityHandler().rotateVectorDown(new Vector3d(1, 0.8, 1));
                this.setDeltaMovement(vector3d6.multiply(toMult));

                Vector3d vector3d2 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
                Vector3d toAdd = getGravityHandler().rotateVectorDown(new Vector3d(0, 0.6F + d8, 0));
                toMult = getGravityHandler().rotateVectorDown(new Vector3d(0, 1, 0));
                toAdd = toAdd.add(toMult.multiply(position()));

                Vector3d vector3d3 = new Vector3d(vector3d2.x, vector3d2.y, vector3d2.z);
                vector3d3.add(toAdd);

                this.setDeltaMovement(vector3d2);
                if (this.horizontalCollision && this.isFree(vector3d3.x, vector3d3.y, vector3d3.z))
                {
                    toMult = getGravityHandler().rotateVectorDown(new Vector3d(1, 0, 1));
                    toAdd = getGravityHandler().rotateVectorDown(new Vector3d(0, 0.3, 0));

                    vector3d2 = vector3d2.multiply(toMult).add(toAdd);

                    this.setDeltaMovement(vector3d2.x, vector3d2.y, vector3d2.z);
                }
            }
            else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType()))
            {
                double d7 = this.getY();
                this.moveRelative(0.02F, p_213352_1_);
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold())
                {

                    Vector3d toMult = getGravityHandler().rotateVectorDown(new Vector3d(0.5, 0.8, 0.5));
                    this.setDeltaMovement(this.getDeltaMovement().multiply(toMult));
                    Vector3d vector3d3 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
                    this.setDeltaMovement(vector3d3);
                } else {
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
                }

                if (!this.isNoGravity())
                {
                    Vector3d toAdd = getGravityHandler().rotateVectorDown(new Vector3d(0, -d0 / 4.0D, 0));

                    this.setDeltaMovement(this.getDeltaMovement().add(toAdd));
                }

                Vector3d vector3d4 = this.getDeltaMovement();

                Vector3d toAdd = getGravityHandler().rotateVectorDown(new Vector3d(0, 0.6F + d7, 0));
                Vector3d toMult = getGravityHandler().rotateVectorDown(new Vector3d(0, 1, 0));
                toAdd = toAdd.add(toMult.multiply(position()));

                Vector3d vector3d3 = new Vector3d(vector3d4.x, vector3d4.y, vector3d4.z);
                vector3d3.add(toAdd);

                if (this.horizontalCollision && this.isFree(vector3d3.x, vector3d3.y, vector3d3.z))
                {
                    toMult = getGravityHandler().rotateVectorDown(new Vector3d(1, 0, 1));
                    toAdd = getGravityHandler().rotateVectorDown(new Vector3d(0, 0.3, 0));
                    this.setDeltaMovement(vector3d4.multiply(toMult).add(toAdd));
                }
            }
            else if (this.isFallFlying())
            {
                // TODO: Make this gravity adjusted
                Vector3d vector3d = this.getDeltaMovement();

                Vector3d fallVector = getGravityHandler().getDownMovement(vector3d);
                double fallAmount = fallVector.x + fallVector.y + fallVector.z;

                if (fallAmount > -0.5D) {
                    this.fallDistance = 1.0F;
                }

                Vector3d vector3d1 = this.getLookAngle();
                float f = this.xRot * ((float)Math.PI / 180F);
                double d1 = Math.sqrt(vector3d1.x * vector3d1.x + vector3d1.z * vector3d1.z);
                double d3 = Math.sqrt(getHorizontalDistanceSqr(vector3d));
                double d4 = vector3d1.length();
                float f1 = MathHelper.cos(f);
                f1 = (float)((double)f1 * (double)f1 * Math.min(1.0D, d4 / 0.4D));
                vector3d = this.getDeltaMovement().add(0.0D, d0 * (-1.0D + (double)f1 * 0.75D), 0.0D);
                if (vector3d.y < 0.0D && d1 > 0.0D) {
                    double d5 = vector3d.y * -0.1D * (double)f1;
                    vector3d = vector3d.add(vector3d1.x * d5 / d1, d5, vector3d1.z * d5 / d1);
                }

                if (f < 0.0F && d1 > 0.0D) {
                    double d9 = d3 * (double)(-MathHelper.sin(f)) * 0.04D;
                    vector3d = vector3d.add(-vector3d1.x * d9 / d1, d9 * 3.2D, -vector3d1.z * d9 / d1);
                }

                if (d1 > 0.0D) {
                    vector3d = vector3d.add((vector3d1.x / d1 * d3 - vector3d.x) * 0.1D, 0.0D, (vector3d1.z / d1 * d3 - vector3d.z) * 0.1D);
                }

                this.setDeltaMovement(vector3d.multiply((double)0.99F, (double)0.98F, (double)0.99F));
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.horizontalCollision && !this.level.isClientSide) {
                    double d10 = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
                    double d6 = d3 - d10;
                    float f2 = (float)(d6 * 10.0D - 3.0D);
                    if (f2 > 0.0F) {
                        this.playSound(this.getFallDamageSound((int)f2), 1.0F, 1.0F);
                        this.hurt(DamageSource.FLY_INTO_WALL, f2);
                    }
                }

                if (this.onGround && !this.level.isClientSide) {
                    this.setSharedFlag(7, false);
                }
            }
            else
            {
                BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
                float f3 = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getSlipperiness(level, this.getBlockPosBelowThatAffectsMyMovement(), this);
                float f4 = this.onGround ? f3 * 0.91F : 0.91F;
                Vector3d vector3d5 = this.handleRelativeFrictionAndCalculateMovement(p_213352_1_, f3);

                Vector3d downMovement = getGravityHandler().getDownMovement(vector3d5);

                double d2 = downMovement.x + downMovement.y + downMovement.z;
                if (this.hasEffect(Effects.LEVITATION))
                {
                    d2 += (0.05D * (double)(this.getEffect(Effects.LEVITATION).getAmplifier() + 1) - d2) * 0.2D;
                    this.fallDistance = 0.0F;
                }
                else if (this.level.isClientSide && !this.level.hasChunkAt(blockpos))
                {
                    if (this.getY() > 0.0D) {
                        d2 = -0.1D;
                    } else {
                        d2 = 0.0D;
                    }
                }
                else if (!this.isNoGravity())
                {
                    d2 -= d0;
                }

                Vector3d toMult = getGravityHandler().rotateVectorDown(new Vector3d(f4, 0, f4));
                Vector3d toAdd = getGravityHandler().rotateVectorDown(new Vector3d(0, d2 * (double)0.98F, 0));

                vector3d5 = vector3d5.multiply(toMult).add(toAdd);

                this.setDeltaMovement(vector3d5);
            }
        }

        this.calculateEntityAnimation(this, this instanceof IFlyingAnimal);
    }

    public float getFallAmount()
    {
        //Temp hard code
        return -0.08f;
    }

    public boolean isJumping()
    {
        return this.jumping;
    }

    public PlayerGravityHandler getGravityHandler()
    {
        return Gravity.getPlayerGravityHandler(getUUID());
    }

    public void jumpFromGround()
    {
        Vector3d jumping = getGravityHandler().getJumpVector(this, getJumpPower() - getFallAmount());

        setDeltaMovement(jumping);

        this.hasImpulse = true;
        net.minecraftforge.common.ForgeHooks.onLivingJump(this);

        this.awardStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.causeFoodExhaustion(0.2F);
        } else {
            this.causeFoodExhaustion(0.05F);
        }
    }


    public Vector3d getFluidFallingAdjustedMovement(double gravity, boolean p_233626_3_, Vector3d movement)
    {
        if (!isSprinting())
        {
            Vector3f down = getGravityHandler().getDownVector();
            Vector3d downMovement = movement.multiply(down.x(), down.y(), down.z());
            double totalDownMovement = (downMovement.x + downMovement.y + downMovement.z) * -1;

            boolean belowZero = ((downMovement.x + downMovement.y + downMovement.z) <= 0);

            double d0;
            if (belowZero && Math.abs(totalDownMovement - 0.005D) >= 0.003D && Math.abs(totalDownMovement - gravity / 16.0D) < 0.003D)
                d0 = -0.003D;
            else
                d0 = totalDownMovement - gravity / 16.0D;

            // Multiply the down direction by the amount to move down to get the final down vector
            down.mul((float)d0);

            // Subtract the previous down movement from the movement vector and add the new down movement to it
            Vector3d finalMovement = (movement.subtract(downMovement)).add(down.x(), down.y(), down.z());

            return finalMovement;
        } else
            return getDeltaMovement();
    }

    public BlockPos getBlockPosBelowThatAffectsMyMovement()
    {
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(getUUID());
        if (handler == null)
            return super.getBlockPosBelowThatAffectsMyMovement();

        PlayerGravityHandler ghandler = getGravityHandler();

        // Calculate the position of the block below the player
        Vector3d below = position();
        Vector3f heightAdjustment = new Vector3f(0, handler.getPlayerModel().getHeightAdjustment() + 0.5f, 0);

        // Adjust for gravity if gravity adjusted
        heightAdjustment = ghandler.rotateVectorDown(heightAdjustment);

        below.subtract(heightAdjustment.x(), heightAdjustment.y(), heightAdjustment.z());

        return new BlockPos(below.x, below.y, below.z);
    }
}
