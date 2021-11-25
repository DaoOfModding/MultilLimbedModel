package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.Common.PlayerUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

public class ParticleEmitter extends ExtendableModelRenderer
{
    protected IParticleData particle;
    protected int interval = 0;

    protected Vector3d Velocity = new Vector3d(0, 0, 0);

    protected Vector3d spawnPos = new Vector3d(0, 0, 0);

    protected int tick = 0;

    public ParticleEmitter(BasicParticleType particleType)
    {
        super(0, 0);

        particle = particleType;
    }

    public void setInterval(int tickInterval)
    {
        interval = tickInterval;
    }

    public void setVelocity(Vector3d newVelocity)
    {
        Velocity = newVelocity;
    }

    // Don't draw any model for a particle emitter
    @Override
    protected void compile(MatrixStack.Entry matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) { }

    public void tick(ClientPlayerEntity player)
    {
        // Do nothing if this emitter is disabled
        if (!visible)
            return;

        if (tick >= interval)
        {
            spawnParticle(player);
            tick = 0;
        }
        else
            tick++;
    }

    protected void spawnParticle(ClientPlayerEntity player)
    {
        // Rotate spawnPos based on player rotation
        Vector3d pos = PlayerUtils.rotateAroundY(spawnPos.scale(1/16), player.yBodyRot);
        pos = pos.add(player.position());
        pos = new Vector3d(pos.x, pos.y + PoseHandler.getPlayerPoseHandler(player.getUUID()).getPlayerModel().getHeightAdjustment(), + pos.z);

        Vector3d vel = PlayerUtils.rotateAroundY(Velocity, player.yBodyRot);

        Minecraft.getInstance().particleEngine.createParticle(particle, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
    }

    @Override
    public void calculateMinHeight(MatrixStack matrixStackIn)
    {
        // Update the position of this model first
        updatePosition();

        matrixStackIn.pushPose();
        rotateMatrix(matrixStackIn);

        Matrix4f rotator = matrixStackIn.last().pose();


        float min = Float.MAX_VALUE * -1;

        Vector4f vector4f = new Vector4f(x, y, z, 1);
        vector4f.transform(rotator);

        spawnPos = new Vector3d(vector4f.x(), vector4f.y(), vector4f.z());

        matrixStackIn.popPose();
    }


}
