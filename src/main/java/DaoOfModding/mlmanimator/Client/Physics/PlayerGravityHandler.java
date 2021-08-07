package DaoOfModding.mlmanimator.Client.Physics;

import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.UUID;

public class PlayerGravityHandler
{
    PlayerEntity player;

    private Vector3f downRotation = new Vector3f(0, 0, 0);
    private Quaternion downXRot = Vector3f.XP.rotation(0);
    private Quaternion downYRot = Vector3f.YP.rotation(0);
    private Quaternion downZRot = Vector3f.ZP.rotation(0);
    public static final Vector3f defaultDown = new Vector3f(0, -1, 0);

    private Vector3d oldPos = new Vector3d(0, 0, 0);
    private Vector3d newPos = new Vector3d(0, 0, 0);
    private Vector3d movement = new Vector3d(0, 0, 0);

    public PlayerGravityHandler(PlayerEntity newPlayer)
    {
        player = newPlayer;
    }

    public UUID getID()
    {
        return player.getUUID();
    }

    public void updatePosition(Vector3d pos)
    {
        oldPos = newPos;
        newPos = pos;
        movement = newPos.subtract(oldPos);
    }

    public void updateBB()
    {
        if (movement.length() > 0)
            rotateBB();
    }

    public Vector3d getMovement()
    {
        return movement;
    }

    public Vector3f getDownVector()
    {
        return rotateVectorDown(defaultDown);
    }

    // Set the down rotation in degrees
    public void setDownRotation(Vector3f newRotation)
    {
        unrotateBB();

        downRotation = newRotation;

        downXRot = Vector3f.XP.rotation((float)Math.toRadians(downRotation.x()));
        downYRot = Vector3f.YP.rotation((float)Math.toRadians(downRotation.y()));
        downZRot = Vector3f.ZP.rotation((float)Math.toRadians(downRotation.z()));

        rotateBB();
    }

    // Reverse any rotations applied to the players boundingBox
    private void unrotateBB()
    {
        downRotation.mul(-1);

        downXRot = Vector3f.XP.rotation((float)Math.toRadians(downRotation.x()));
        downYRot = Vector3f.YP.rotation((float)Math.toRadians(downRotation.y()));
        downZRot = Vector3f.ZP.rotation((float)Math.toRadians(downRotation.z()));

        rotateBB();
    }

    // Rotate the players bounding box around the down vector
    private void rotateBB()
    {
        Vector3d minBB = new Vector3d(player.getBoundingBox().minX, player.getBoundingBox().minY, player.getBoundingBox().minZ);
        Vector3d maxBB = new Vector3d(player.getBoundingBox().maxX, player.getBoundingBox().maxY, player.getBoundingBox().maxZ);

        minBB = rotateVectorDown(minBB);
        maxBB = rotateVectorDown(maxBB);

        player.setBoundingBox(new AxisAlignedBB(minBB.x, minBB.y, minBB.z, maxBB.x, maxBB.y, maxBB.z));
    }

    public Vector3f getDownRotationDegrees()
    {
        return downRotation;
    }

    // Rotate a vector to have the same down direction at this player
    public Vector3f rotateVectorDown(Vector3f toRotate)
    {
        Vector3f rotating = toRotate.copy();
        rotating.transform(downZRot);
        rotating.transform(downYRot);
        rotating.transform(downXRot);

        return rotating;
    }

    public Vector3d rotateVectorDown(Vector3d toRotate)
    {
        Vector3f rotating = new Vector3f((float)toRotate.x, (float)toRotate.y, (float)toRotate.z);
        rotating.transform(downZRot);
        rotating.transform(downYRot);
        rotating.transform(downXRot);

        return new Vector3d(rotating.x(), rotating.y(), rotating.z());
    }

    public void rotateMatrixDown(MatrixStack stack)
    {
        stack.mulPose(downZRot);
        stack.mulPose(downYRot);
        stack.mulPose(downXRot);
    }
}
