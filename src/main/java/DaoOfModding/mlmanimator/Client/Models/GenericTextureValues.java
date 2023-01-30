package DaoOfModding.mlmanimator.Client.Models;

import net.minecraft.client.model.geom.builders.UVPair;

public class GenericTextureValues
{
    public static final UVPair skin_Size = new UVPair(64, 64);
    public static final UVPair armor_Size = new UVPair(64, 32);


    public static final UVPair head = new UVPair(0, 0);
    public static final UVPair hat = new UVPair(32, 0);
    public static final UVPair chest = new UVPair(16, 16);
    public static final UVPair jacket = new UVPair(16, 32);
    public static final UVPair leftArm = new UVPair(32, 48);
    public static final UVPair leftSleeve = new UVPair(48, 48);
    public static final UVPair leftArmArmor = new UVPair(40, 16);
    public static final UVPair rightArm = new UVPair(40, 16);
    public static final UVPair rightSleeve = new UVPair(40, 32);
    public static final UVPair rightArmArmor = new UVPair(40, 16);
    public static final UVPair leftLeg = new UVPair(16, 48);
    public static final UVPair leftPants = new UVPair(0, 48);
    public static final UVPair leftLegArmor = new UVPair(0, 16);
    public static final UVPair rightLeg = new UVPair(0, 16);
    public static final UVPair rightPants = new UVPair(0, 32);
    public static final UVPair rightLegArmor = new UVPair(0, 16);

    public static final UVPair elytra = new UVPair(22, 0);
    public static final UVPair cloak = new UVPair(0, 0);

    public static final float innerExtention = 0f;
    public static final float outerExtention = 0.5f;
    public static final float ArmorExtention = 1f;
    public static final float chestLegArmorExtention = 0.51f;
    public static final float leftlegArmorExtention = 0.511f;
    public static final float rightlegArmorExtention = 0.512f;
    public static final float ArmArmorExtention = 1.01f;
    public static final float leftFootArmorExtention = 1.01f;
    public static final float rightFootArmorExtention = 1f;

    public static void addGenericBodyLayers(ExtendableModelRenderer body)
    {
        addGenericBodyLayers(body, 0, 0);
    }

    public static void addGenericBodyLayers(ExtendableModelRenderer body, int u, int v)
    {
        body.addLayer(new UVPair(GenericTextureValues.chest.u() + u, GenericTextureValues.chest.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        body.addLayer(new UVPair(GenericTextureValues.jacket.u() + u, GenericTextureValues.jacket.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        body.addLayer(new UVPair(GenericTextureValues.chest.u() + u, GenericTextureValues.chest.v() + v), GenericTextureValues.armor_Size, GenericTextureValues.chestLegArmorExtention, TextureHandler.LEG_ARMOR);
        body.addLayer(new UVPair(GenericTextureValues.chest.u() + u, GenericTextureValues.chest.v() + v), GenericTextureValues.armor_Size, GenericTextureValues.ArmorExtention, TextureHandler.CHEST_ARMOR);
    }

    public static void addGenericHeadLayers(ExtendableModelRenderer head)
    {
        addGenericHeadLayers(head, 0, 0);
    }

    public static void addGenericHeadLayers(ExtendableModelRenderer head, int u, int v)
    {
        GenericTextureValues.addGenericHeadLayers(head);
        head.addLayer(new UVPair(GenericTextureValues.head.u() + u, GenericTextureValues.head.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        head.addLayer(new UVPair(GenericTextureValues.hat.u() + u, GenericTextureValues.hat.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        head.addLayer(new UVPair(GenericTextureValues.head.u() + u, GenericTextureValues.head.v() + v), GenericTextureValues.armor_Size, GenericTextureValues.ArmorExtention, TextureHandler.HEAD_ARMOR);
    }

    public static void addGenericLeftArmLayers(ExtendableModelRenderer leftArm)
    {
        addGenericLeftArmLayers(leftArm, 0, 0);
    }

    public static void addGenericLeftArmLayers(ExtendableModelRenderer leftArm, int u, int v)
    {
        leftArm.addLayer(new UVPair(GenericTextureValues.leftArm.u() + u, GenericTextureValues.leftArm.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        leftArm.addLayer(new UVPair(GenericTextureValues.leftSleeve.u() + u, GenericTextureValues.leftSleeve.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        leftArm.addLayer(new UVPair(GenericTextureValues.leftArmArmor.u() + u, GenericTextureValues.leftArmArmor.v() + v), GenericTextureValues.armor_Size, GenericTextureValues.ArmArmorExtention, TextureHandler.CHEST_ARMOR);
    }

    public static void addGenericRightArmLayers(ExtendableModelRenderer rightArm)
    {
        addGenericRightArmLayers(rightArm, 0, 0);
    }

    public static void addGenericRightArmLayers(ExtendableModelRenderer rightArm, int u, int v)
    {
        rightArm.addLayer(new UVPair(GenericTextureValues.rightArm.u() + u, GenericTextureValues.rightArm.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        rightArm.addLayer(new UVPair(GenericTextureValues.rightSleeve.u() + u, GenericTextureValues.rightSleeve.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        rightArm.addLayer(new UVPair(GenericTextureValues.rightArmArmor.u() + u, GenericTextureValues.rightArmArmor.v() + v), GenericTextureValues.armor_Size, GenericTextureValues.ArmArmorExtention, TextureHandler.CHEST_ARMOR, true);
    }

    public static void addGenericLeftLegLayers(ExtendableModelRenderer leftLeg)
    {
        addGenericLeftLegLayers(leftLeg, 0, 0);
    }

    public static void addGenericLeftLegLayers(ExtendableModelRenderer leftLeg, int u, int v)
    {
        leftLeg.addLayer(new UVPair(GenericTextureValues.leftLeg.u() + u, GenericTextureValues.leftLeg.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        leftLeg.addLayer(new UVPair(GenericTextureValues.leftPants.u() + u, GenericTextureValues.leftPants.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        leftLeg.addLayer(new UVPair(GenericTextureValues.leftLegArmor.u() + u, GenericTextureValues.leftLegArmor.v() + v), GenericTextureValues.armor_Size, GenericTextureValues.leftlegArmorExtention, TextureHandler.LEG_ARMOR);
        leftLeg.addLayer(new UVPair(GenericTextureValues.leftLegArmor.u() + u, GenericTextureValues.leftLegArmor.v() + v), GenericTextureValues.armor_Size,  GenericTextureValues.leftFootArmorExtention, TextureHandler.FOOT_ARMOR);
    }

    public static void addGenericRightLegLayers(ExtendableModelRenderer rightLeg)
    {
        addGenericRightLegLayers(rightLeg, 0, 0);
    }

    public static void addGenericRightLegLayers(ExtendableModelRenderer rightLeg, int u, int v)
    {
        rightLeg.addLayer(new UVPair(GenericTextureValues.rightLeg.u() + u, GenericTextureValues.rightLeg.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        rightLeg.addLayer(new UVPair(GenericTextureValues.rightPants.u() + u, GenericTextureValues.rightPants.v() + v), GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        rightLeg.addLayer(new UVPair(GenericTextureValues.rightLegArmor.u() + u, GenericTextureValues.rightLegArmor.v() + v), GenericTextureValues.armor_Size, GenericTextureValues.rightlegArmorExtention, TextureHandler.LEG_ARMOR, true);
        rightLeg.addLayer(new UVPair(GenericTextureValues.rightLegArmor.u() + u, GenericTextureValues.rightLegArmor.v() + v), GenericTextureValues.armor_Size, GenericTextureValues.rightFootArmorExtention, TextureHandler.FOOT_ARMOR, true);
    }
}
