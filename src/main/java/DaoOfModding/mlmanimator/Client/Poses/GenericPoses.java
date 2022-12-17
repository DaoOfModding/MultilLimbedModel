package DaoOfModding.mlmanimator.Client.Poses;

import DaoOfModding.mlmanimator.Client.AnimationFramework.AnimationBuilder;
import DaoOfModding.mlmanimator.Client.AnimationFramework.AnimationSpeedCalculator;
import DaoOfModding.mlmanimator.Client.Models.GenericLimbNames;
import net.minecraft.world.phys.Vec3;

public class GenericPoses
{
    public static final int walkLegPriority = 10;
    public static final int walkArmPriority = 5;

    public static final int jumpLegPriority = 15;
    public static final int jumpArmPriority = 10;

    public static final int armHoldPriority = 50;

    public static final int armAttackPriority = 99;

    public static final int swimBodyPriority = 99;
    public static final int swimLegPriority = 20;

    public static PlayerPose Idle = new PlayerPose();
    public static PlayerPose HoldingMain = new PlayerPose();
    public static PlayerPose HoldingOff = new PlayerPose();
    public static PlayerPose Walking = new PlayerPose();
    public static PlayerPose Jumping = new PlayerPose();
    public static PlayerPose slashing = new PlayerPose();
    public static PlayerPose Swimming = new PlayerPose();
    public static PlayerPose SwimmingMoving = new PlayerPose();

    public static void init()
    {
        setupIdle();
        setupHolding();
        setupWalking();
        setupJumping();
        setupSwimming();

        setupAction();
    }

    // TODO: Add swimming, sleeping, sitting, etc poses
    // TODO: Crouch, FlyFalling Animations + Elytra animations

    public static void setupSwimming()
    {
        Swimming.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90), 0, 0), swimBodyPriority);

        SwimmingMoving.addAngle(GenericLimbNames.leftLeg, new Vec3(Math.toRadians(-30), 0, 0), swimLegPriority, 15f, 1);
        SwimmingMoving.addAngle(GenericLimbNames.leftLeg, new Vec3(Math.toRadians(30), 0, 0), swimLegPriority, 15f, 1);
        SwimmingMoving.addAngle(GenericLimbNames.rightLeg, new Vec3(Math.toRadians(30), 0, 0), swimLegPriority, 15f, 1);
        SwimmingMoving.addAngle(GenericLimbNames.rightLeg, new Vec3(Math.toRadians(-30), 0, 0), swimLegPriority, 15f, 1);
    }

    public static void setupIdle()
    {
        Idle.addAngle(GenericLimbNames.body, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.leftArm, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.rightArm, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.lowerLeftArm, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.lowerRightArm, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.leftLeg, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.rightLeg, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.lowerLeftLeg, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.lowerRightLeg, new Vec3(0, 0, 0), 0);
        Idle.addAngle(GenericLimbNames.leftWingElytra, new Vec3(0, 0, Math.toRadians(-10)), 0);
        Idle.addAngle(GenericLimbNames.rightWingElytra, new Vec3(0, 0, Math.toRadians(10)), 0);
    }

    public static void addToIdle(PlayerPose toAdd)
    {
        Idle = Idle.combine(toAdd);
    }

    public static void addToWalking(PlayerPose toAdd)
    {
        Walking = Walking.combine(toAdd);
    }

    public static void addToJumping(PlayerPose toAdd)
    {
        Jumping = Jumping.combine(toAdd);
    }

    public static void addToSwimming(PlayerPose toAdd)
    {
        SwimmingMoving = SwimmingMoving.combine(toAdd);
    }

    public static void setupHolding()
    {
        HoldingMain.addAngle(GenericLimbNames.lowerRightArm, new Vec3(Math.toRadians(-35), 0, 0), armHoldPriority);
        //HoldingMain.addAngle(GenericLimbNames.rightArm, new Vec3(Math.toRadians(-30), 0, 0), 1);
        HoldingOff.addAngle(GenericLimbNames.lowerLeftArm, new Vec3(Math.toRadians(-35), 0, 0), armHoldPriority);
        //HoldingOff.addAngle(GenericLimbNames.leftArm, new Vec3(Math.toRadians(-30), 0, 0), 1);
    }

    public static void setupAction()
    {
        // TODO: Make this... not shit
        slashing.addAngle(GenericLimbNames.lowerRightArm, new Vec3(Math.toRadians(-130), 0, 0), armAttackPriority, 1f, -1);
    }

    public static void setupWalking()
    {
        Vec3[] walkAngle = new Vec3[5];

        walkAngle[0] = new Vec3(Math.toRadians(-45), Math.toRadians(0), Math.toRadians(0));
        walkAngle[1] = new Vec3(Math.toRadians(-45), Math.toRadians(0), Math.toRadians(0));
        walkAngle[2] = new Vec3(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0));
        walkAngle[3] = new Vec3(Math.toRadians(30), Math.toRadians(0), Math.toRadians(0));
        walkAngle[4] = new Vec3(Math.toRadians(30), Math.toRadians(0), Math.toRadians(0));

        Walking = AnimationBuilder.generateRepeatingMirroredLimbs(GenericLimbNames.leftLeg, GenericLimbNames.rightLeg, walkAngle, walkLegPriority, AnimationSpeedCalculator.defaultSpeedInTicks / 2, 1);


        Vec3[] lowerWalkAngle = new Vec3[5];
        lowerWalkAngle[0] = new Vec3(Math.toRadians(45), Math.toRadians(0), Math.toRadians(0));
        lowerWalkAngle[1] = new Vec3(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0));
        lowerWalkAngle[2] = new Vec3(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0));
        lowerWalkAngle[3] = new Vec3(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0));
        lowerWalkAngle[4] = new Vec3(Math.toRadians(15), Math.toRadians(0), Math.toRadians(0));

        Walking = Walking.combine(AnimationBuilder.generateRepeatingMirroredLimbs(GenericLimbNames.lowerLeftLeg, GenericLimbNames.lowerRightLeg, lowerWalkAngle, walkLegPriority, AnimationSpeedCalculator.defaultSpeedInTicks / 2, 1));


        Walking.addAngle(GenericLimbNames.leftArm, new Vec3(Math.toRadians(45), Math.toRadians(0), 0), walkArmPriority);
        Walking.addAngle(GenericLimbNames.leftArm, new Vec3(Math.toRadians(-45), Math.toRadians(0), 0), walkArmPriority);

        Walking.addAngle(GenericLimbNames.rightArm, new Vec3(Math.toRadians(-45), Math.toRadians(0), 0), walkArmPriority);
        Walking.addAngle(GenericLimbNames.rightArm, new Vec3(Math.toRadians(45), Math.toRadians(0), 0), walkArmPriority);
    }

    public static void setupJumping()
    {
        Jumping.addAngle(GenericLimbNames.leftArm, new Vec3(Math.toRadians(-180), Math.toRadians(0), Math.toRadians(30)), jumpArmPriority, 5f, -1);
        Jumping.addAngle(GenericLimbNames.rightArm, new Vec3(Math.toRadians(-180), Math.toRadians(0), Math.toRadians(-30)), jumpArmPriority, 5f, -1);

        Jumping.addAngle(GenericLimbNames.leftLeg, new Vec3(Math.toRadians(0), Math.toRadians(0), 0), jumpLegPriority, 1f, -1);
        Jumping.addAngle(GenericLimbNames.rightLeg, new Vec3(Math.toRadians(0), Math.toRadians(0), 0), jumpLegPriority, 1f, -1);
        Jumping.addAngle(GenericLimbNames.lowerLeftLeg, new Vec3(Math.toRadians(0), Math.toRadians(0), 0), jumpLegPriority, 1f, -1);
        Jumping.addAngle(GenericLimbNames.lowerRightLeg, new Vec3(Math.toRadians(0), Math.toRadians(0), 0), jumpLegPriority, 1f, -1);
    }
}
