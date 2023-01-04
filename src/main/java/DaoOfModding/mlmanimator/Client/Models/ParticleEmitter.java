package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.Models.Quads.Quad;
import DaoOfModding.mlmanimator.Client.Models.Quads.QuadLinkage;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.Common.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleOptions;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;

public class ParticleEmitter extends ExtendableModelRenderer
{
    protected ParticleOptions particle;
    protected int interval = 0;

    protected Vec3 Velocity = new Vec3(0, 0, 0);
    protected Vec3 spawnPos = new Vec3(0, 0, 0);

    protected int tick = 0;

    public ParticleEmitter(ParticleOptions particleType, String name)
    {
        super(name);

        particle = particleType;
    }

    public void setInterval(int tickInterval)
    {
        interval = tickInterval;
    }

    public void setVelocity(Vec3 newVelocity)
    {
        Velocity = newVelocity;
    }

    /*
    // Don't draw any model for a particle emitter
    @Override
    protected void compile(PoseStack.Pose PoseStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) { }*/

    @Override
    public void tick(AbstractClientPlayer player)
    {
        // Do nothing if this emitter is disabled
        if (!mPart.visible)
            return;

        if (tick >= interval)
        {
            spawnParticle(player);
            tick = 0;
        }
        else
            tick++;
    }

    protected void spawnParticle(Player player)
    {
        // Rotate spawnPos based on player rotation
        Vec3 pos = PlayerUtils.rotateAroundY(spawnPos.scale(1.0/16.0), 360-player.yBodyRot);
        pos = pos.add(player.position());

        pos = new Vec3(pos.x, pos.y + PoseHandler.getPlayerPoseHandler(player.getUUID()).getPlayerModel().getHeightAdjustment(), pos.z);

        Vec3 vel = PlayerUtils.rotateAroundY(Velocity, 360-player.yBodyRot);

        Minecraft.getInstance().particleEngine.createParticle(particle, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
    }

    @Override
    public MultiLimbedDimensions calculateMinHeight(PoseStack PoseStackIn, double yRot)
    {
        // Update the position of this model first
        updatePosition();

        PoseStackIn.pushPose();
        rotateMatrix(PoseStackIn);

        Matrix4f rotator = PoseStackIn.last().pose();

        Vector4f vector4f = new Vector4f(0, 0, 0, 1);
        vector4f.transform(rotator);

        // Why is x not negative? I have no idea
        spawnPos = new Vec3(vector4f.x(), -vector4f.y(), -vector4f.z());

        // Calculate the min height of children
        for (ExtendableModelRenderer testChild : child)
            testChild.calculateMinHeight(PoseStackIn, yRot);

        PoseStackIn.popPose();

        return dimensions;
    }

    @Override
    public ExtendableModelRenderer clone()
    {
        ParticleEmitter copy = new ParticleEmitter(particle, name);
        copy.setParent(parent);

        copy.Velocity = Velocity;
        copy.spawnPos = spawnPos;
        copy.interval = interval;
        copy.tick = tick;

        copy.dimensions = new MultiLimbedDimensions(dimensions);
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
