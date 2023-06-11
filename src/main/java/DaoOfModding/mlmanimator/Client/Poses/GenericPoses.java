package DaoOfModding.mlmanimator.Client.Poses;

import DaoOfModding.mlmanimator.Client.AnimationFramework.AnimationBuilder;
import DaoOfModding.mlmanimator.Client.AnimationFramework.AnimationSpeedCalculator;
import DaoOfModding.mlmanimator.Client.Models.GenericLimbNames;
import DaoOfModding.mlmanimator.Common.PlayerUtils;
import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class GenericPoses
{
    public static final int walkLegPriority = 100;
    public static final int walkArmPriority = 50;

    public static final int crouchPriority = 150;

    public static final int jumpLegPriority = 200;
    public static final int jumpArmPriority = 150;

    public static final int flyFallingPriority = 1000;

    public static final int armHoldPriority = 500;

    public static final int armAttackPriority = 1000;
    public static final int armBlockPriority = 500;

    public static final int swimBodyPriority = 1000;
    public static final int swimLegPriority = 200;

    public static final int sleepBodyPriority = 99;

    public static final int sitPriority = 250;

    public static final int crawlPriority = 300;

    public static final int spinPriority = 999;

    public static PlayerPose Idle = new PlayerPose();
    public static PlayerPose Walking = new PlayerPose();
    public static PlayerPose Jumping = new PlayerPose();
    public static PlayerPose Crouching = new PlayerPose();
    public static PlayerPose CrouchingWalk = new PlayerPose();
    public static PlayerPose Swimming = new PlayerPose();
    public static PlayerPose SwimmingMoving = new PlayerPose();
    public static PlayerPose FlyFalling = new PlayerPose();
    public static PlayerPose Sleeping = new PlayerPose();
    public static PlayerPose Sitting = new PlayerPose();
    public static PlayerPose Crawling = new PlayerPose();
    public static PlayerPose CrawlingWalk = new PlayerPose();
    public static PlayerPose SpinAttack = new PlayerPose();

    public static ArmPose Holding = new ArmPose();
    public static ArmPose slashing = new ArmPose();
    public static ArmPose block = new ArmPose();
    public static ArmPose bow = new ArmPose();
    public static ArmPose bowOff = new ArmPose();
    public static ArmPose spear = new ArmPose();
    public static ArmPose crossbow = new ArmPose();
    public static ArmPose crossbowOff = new ArmPose();
    public static ArmPose spyglass = new ArmPose();
    public static ArmPose horn = new ArmPose();
    public static ArmPose eat = new ArmPose();
    public static ArmPose drink = new ArmPose();

    protected static ArrayList<String> legs = new ArrayList<String>();

    public static void init()
    {
        setupIdle();
        setupHolding();
        setupWalking();
        setupJumping();
        setupFlyFalling();
        setupCrouching();
        setupCrouchingWalk();
        setupSwimming();
        setupSitting();
        setupSleeping();
        setupCrawling();
        setupSpin();

        setupAction();

        addLeg(GenericLimbNames.leftLeg);
        addLeg(GenericLimbNames.lowerLeftLeg);
        addLeg(GenericLimbNames.rightLeg);
        addLeg(GenericLimbNames.lowerRightLeg);
    }

    public static void addLeg(String legname)
    {
        legs.add(legname);
    }

    public static ArrayList<String> getLegs()
    {
        return legs;
    }

    public static PlayerPose getWalkingPose(Player player)
    {
        PlayerPose speedAdjustedWalkPose = Walking.clone();

        /*double speed = player.getDeltaMovement().multiply(1, 0, 1).lengthSqr();

        speed = speed / 0.02;
        speed = speed * speed;*/

        double speed  = PlayerUtils.getDelta(player).multiply(1, 0, 1).length();

        /*speed = speed / 0.175;
        speed = speed * speed;*/

        speed = speed / 0.25f;

        if (speed > 1)
            speed = 1;
        else if (speed <= 0.05)
            return Idle;

        speedAdjustedWalkPose.adjustAllSpeeds((float)speed);

        return speedAdjustedWalkPose;
    }

    public static void setupSwimming()
    {
        Swimming.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90), 0, 0), swimBodyPriority);

        SwimmingMoving.addAngle(GenericLimbNames.leftLeg, new Vec3(Math.toRadians(-30), 0, 0), swimLegPriority, 15f, 1);
        SwimmingMoving.addAngle(GenericLimbNames.leftLeg, new Vec3(Math.toRadians(30), 0, 0), swimLegPriority, 15f, 1);
        SwimmingMoving.addAngle(GenericLimbNames.rightLeg, new Vec3(Math.toRadians(30), 0, 0), swimLegPriority, 15f, 1);
        SwimmingMoving.addAngle(GenericLimbNames.rightLeg, new Vec3(Math.toRadians(-30), 0, 0), swimLegPriority, 15f, 1);
    }

    public static void setupSleeping()
    {
        Sleeping.addAngle(GenericLimbNames.body, new Vec3(0, 0, 0), GenericPoses.sleepBodyPriority);
    }

    public static void setupSitting()
    {
        Sitting.addAngle(GenericLimbNames.leftLeg, new Vec3(Math.toRadians(-90), Math.toRadians(-20), 0), sitPriority);
        Sitting.addAngle(GenericLimbNames.rightLeg, new Vec3(Math.toRadians(-90), Math.toRadians(20), 0), sitPriority);
    }

    public static void setupFlyFalling()
    {
        FlyFalling.addAngle(GenericLimbNames.leftWingElytra, new Vec3(0, 0, Math.toRadians(-70)), flyFallingPriority);
        FlyFalling.addAngle(GenericLimbNames.rightWingElytra, new Vec3(0, 0, Math.toRadians(70)), flyFallingPriority);
    }

    public static void setupCrouching()
    {
        Crouching.addAngle(GenericLimbNames.leftLeg, new Vec3(Math.toRadians(-70), 0, 0), crouchPriority);
        Crouching.addAngle(GenericLimbNames.rightLeg, new Vec3(Math.toRadians(-70), 0, 0), crouchPriority);
        Crouching.addAngle(GenericLimbNames.lowerLeftLeg, new Vec3(Math.toRadians(70), 0, 0), crouchPriority);
        Crouching.addAngle(GenericLimbNames.lowerRightLeg, new Vec3(Math.toRadians(70), 0, 0), crouchPriority);

        Crouching.addAngle(GenericLimbNames.leftWingElytra, new Vec3(0, 0, Math.toRadians(-40)), crouchPriority);
        Crouching.addAngle(GenericLimbNames.rightWingElytra, new Vec3(0, 0, Math.toRadians(40)), crouchPriority);
    }

    public static void setupCrawling()
    {
        Crawling.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90), 0, 0), crawlPriority);

        CrawlingWalk.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90), 0, 0), crawlPriority);
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
        Holding.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-30), Math.toRadians(-5), 0),armHoldPriority);
        Holding.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-35), 0, 0), armHoldPriority + 1);
        Holding.setHolding(true);
    }

    public static void setupSpin()
    {
        SpinAttack.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90), 0, 0), spinPriority, 2f, -1);
        SpinAttack.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90), 0, Math.toRadians(90)), spinPriority, 2f, -1);
        SpinAttack.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90), 0, Math.toRadians(180)), spinPriority, 2f, -1);
        SpinAttack.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90), 0, Math.toRadians(270)), spinPriority, 2f, -1);
    }

    public static void setupAction()
    {
        slashing.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-130), 0, 0), armAttackPriority, 5f, -1);
        slashing.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-40), 0, 0), armAttackPriority, 1f, -1);
        slashing.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-41), 0, 0), armAttackPriority, 999f, -1);

        block.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-45), Math.toRadians(-30), 0), armBlockPriority);
        block.addAngle(ArmPose.lowerArm, new Vec3(0, 0, 0), armBlockPriority + 1);

        bow.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-100), Math.toRadians(-10), 0), armHoldPriority);
        bow.addAngle(ArmPose.lowerArm, new Vec3(0, 0, 0), armHoldPriority + 1);
        bow.setHolding(true);

        bowOff.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-100), Math.toRadians(-35), 0), armHoldPriority);
        bowOff.addAngle(ArmPose.lowerArm, new Vec3(0, 0, 0), armHoldPriority + 1);
        bowOff.setHolding(true);

        spear.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-180), 0, 0), armHoldPriority);
        spear.addAngle(ArmPose.lowerArm, new Vec3(0, 0, 0), armHoldPriority + 1);

        spyglass.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-90), 0, Math.toRadians(25)), armHoldPriority);
        spyglass.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-55), 0, 0), armHoldPriority + 1);
        spyglass.setHolding(true);

        horn.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-80), 0, Math.toRadians(25)), armHoldPriority);
        horn.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-45), 0, 0), armHoldPriority + 1);
        horn.setHolding(true);

        crossbow.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-20), 0, Math.toRadians(-20)), armHoldPriority);
        crossbow.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(0), 0, 0), armHoldPriority + 1);

        crossbowOff.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-30), 0, Math.toRadians(-40)), armHoldPriority, 5f, -1);
        crossbowOff.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-30), 0, Math.toRadians(-50)), armHoldPriority, 70f, -1);
        crossbowOff.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(0), 0, 0), armHoldPriority + 1);

        eat.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-80), 0, Math.toRadians(40)), armHoldPriority);
        eat.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-50), 0, 0), armHoldPriority + 1, 5f, -1);
        eat.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-40), 0, 0), armHoldPriority + 1, 5f, -1);

        drink.addAngle(ArmPose.upperArm, new Vec3(Math.toRadians(-80), 0, Math.toRadians(40)), armHoldPriority);
        drink.addAngle(ArmPose.lowerArm, new Vec3(Math.toRadians(-40), 0, 0), armHoldPriority + 1);
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

    public static void setupCrouchingWalk()
    {
        Vec3[] walkAngle = new Vec3[5];

        walkAngle[0] = new Vec3(Math.toRadians(-95), Math.toRadians(0), Math.toRadians(0));
        walkAngle[1] = new Vec3(Math.toRadians(-95), Math.toRadians(0), Math.toRadians(0));
        walkAngle[2] = new Vec3(Math.toRadians(-70), Math.toRadians(0), Math.toRadians(0));
        walkAngle[3] = new Vec3(Math.toRadians(-30), Math.toRadians(0), Math.toRadians(0));
        walkAngle[4] = new Vec3(Math.toRadians(-30), Math.toRadians(0), Math.toRadians(0));

        CrouchingWalk = AnimationBuilder.generateRepeatingMirroredLimbs(GenericLimbNames.leftLeg, GenericLimbNames.rightLeg, walkAngle, crouchPriority, AnimationSpeedCalculator.defaultSpeedInTicks / 2, 1);


        Vec3[] lowerWalkAngle = new Vec3[5];
        lowerWalkAngle[0] = new Vec3(Math.toRadians(95), Math.toRadians(0), Math.toRadians(0));
        lowerWalkAngle[1] = new Vec3(Math.toRadians(70), Math.toRadians(0), Math.toRadians(0));
        lowerWalkAngle[2] = new Vec3(Math.toRadians(70), Math.toRadians(0), Math.toRadians(0));
        lowerWalkAngle[3] = new Vec3(Math.toRadians(70), Math.toRadians(0), Math.toRadians(0));
        lowerWalkAngle[4] = new Vec3(Math.toRadians(85), Math.toRadians(0), Math.toRadians(0));

        CrouchingWalk = CrouchingWalk.combine(AnimationBuilder.generateRepeatingMirroredLimbs(GenericLimbNames.lowerLeftLeg, GenericLimbNames.lowerRightLeg, lowerWalkAngle, crouchPriority, AnimationSpeedCalculator.defaultSpeedInTicks / 2, 1));


        CrouchingWalk.addAngle(GenericLimbNames.leftWingElytra, new Vec3(0, 0, Math.toRadians(-30)), crouchPriority);
        CrouchingWalk.addAngle(GenericLimbNames.rightWingElytra, new Vec3(0, 0, Math.toRadians(30)), crouchPriority);
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
