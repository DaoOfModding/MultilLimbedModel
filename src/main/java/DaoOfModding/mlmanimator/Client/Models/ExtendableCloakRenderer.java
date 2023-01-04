package DaoOfModding.mlmanimator.Client.Models;

import com.mojang.math.Vector3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;


public class ExtendableCloakRenderer extends ExtendableModelRenderer
{
    public ExtendableCloakRenderer clone()
    {
        ExtendableCloakRenderer copy = new ExtendableCloakRenderer(name);
        copy(copy);

        return copy;
    }

    public ExtendableCloakRenderer(String limbName)
    {
        super(limbName);
    }

    @Override
    public void tick(AbstractClientPlayer player)
    {
        super.tick(player);

        if (!player.isCapeLoaded() || player.isInvisible() || !player.isModelPartShown(PlayerModelPart.CAPE) || player.getCloakTextureLocation() == null || player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA))
        {
            mPart.visible = false;
            return;
        }

        mPart.visible = true;

        updateCloakRotations(player);
    }

    protected void updateCloakRotations(AbstractClientPlayer player)
    {
        // This mess is directly take from the vanilla CapeLayer class and SLIGHTLY altered to make work properly here
        double d0 = player.xCloak - player.getX();
        double d1 = player.yCloak - player.getY();
        double d2 = player.zCloak - player.getZ();
        float f = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO);
        double d3 = Mth.sin(f * ((float)Math.PI / 180F));
        double d4 = (-Mth.cos(f * ((float)Math.PI / 180F)));
        float f1 = (float)d1 * 10.0F;
        f1 = Mth.clamp(f1, -6.0F, 32.0F);
        float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
        f2 = Mth.clamp(f2, 0.0F, 150.0F);
        float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
        f3 = Mth.clamp(f3, -20.0F, 20.0F);
        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        double xRot = Math.toRadians(6.0F + f2 / 2.0F + f1 + 180) * -1;
        double yRot = Math.toRadians(f3 / 2.0F);
        double zRot = Math.toRadians(180.0F - f3 / 2.0F);

        setRotationOffset(new Vec3(xRot, yRot, zRot));
    }
}
