package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.Models.Quads.Quad;
import DaoOfModding.mlmanimator.Client.Models.Quads.QuadLinkage;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.Common.PlayerUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

public class ParticleEmitter extends ExtendableModelRenderer
{
    protected IParticleData particle;
    protected int interval = 0;

    protected Vector3d Velocity = new Vector3d(0, 0, 0);
    protected Vector3d spawnPos = new Vector3d(0, 0, 0);

    protected int tick = 0;

    public ParticleEmitter(IParticleData particleType)
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

    @Override
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
        Vector3d pos = PlayerUtils.rotateAroundY(spawnPos.scale(1.0/16.0), 360-player.yBodyRot);
        pos = pos.add(player.position());

        System.out.println(spawnPos);

        // Pos.Y is a crazy random variable :/

        pos = new Vector3d(pos.x, pos.y + PoseHandler.getPlayerPoseHandler(player.getUUID()).getPlayerModel().getHeightAdjustment(), pos.z);

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

        Vector4f vector4f = new Vector4f(0, 0, 0, 1);
        vector4f.transform(rotator);

        // Why is x not negative? I have no idea
        spawnPos = new Vector3d(vector4f.x(), -vector4f.y(), -vector4f.z());

        // Calculate the min height of children
        for (ExtendableModelRenderer testChild : child)
            testChild.calculateMinHeight(matrixStackIn);

        matrixStackIn.popPose();
    }

    @Override
    public ExtendableModelRenderer clone()
    {
        ParticleEmitter copy = new ParticleEmitter(particle);
        copy.setParent(parent);

        copy.Velocity = Velocity;
        copy.spawnPos = spawnPos;
        copy.interval = interval;
        copy.tick = tick;

        copy.minHeight = minHeight;
        copy.look = look;
        copy.rotationOffset = rotationOffset;
        copy.rotationPoint = rotationPoint;
        copy.renderFirstPerson = renderFirstPerson;

        copy.relativePosition = relativePosition;
        copy.fixedPosition = fixedPosition;

        copy.defaultSize = defaultSize;
        copy.thisSize = thisSize;
        copy.defaultResize = defaultResize;
        copy.thisDelta = thisDelta;

        for (ExtendableModelRenderer children : child)
            copy.addChild(children.clone());

        for (Quad quad : quads)
            copy.addQuad(quad);

        for (QuadLinkage link : quadLinkage)
            copy.addQuadLinkage(link);

        return copy;
    }
}
