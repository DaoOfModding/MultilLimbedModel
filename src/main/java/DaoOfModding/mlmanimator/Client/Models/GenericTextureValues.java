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
        body.addLayer(GenericTextureValues.chest, GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        body.addLayer(GenericTextureValues.jacket, GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        body.addLayer(GenericTextureValues.chest, GenericTextureValues.armor_Size, GenericTextureValues.chestLegArmorExtention, TextureHandler.LEG_ARMOR);
        body.addLayer(GenericTextureValues.chest, GenericTextureValues.armor_Size, GenericTextureValues.ArmorExtention, TextureHandler.CHEST_ARMOR);
    }

    public static void addGenericHeadLayers(ExtendableModelRenderer head)
    {
        head.addLayer(GenericTextureValues.head, GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        head.addLayer(GenericTextureValues.hat, GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        head.addLayer(GenericTextureValues.head, GenericTextureValues.armor_Size, GenericTextureValues.ArmorExtention, TextureHandler.HEAD_ARMOR);
    }

    public static void addGenericLeftArmLayers(ExtendableModelRenderer leftArm)
    {
        leftArm.addLayer(GenericTextureValues.leftArm, GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        leftArm.addLayer(GenericTextureValues.leftSleeve, GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        leftArm.addLayer(GenericTextureValues.leftArmArmor, GenericTextureValues.armor_Size, GenericTextureValues.ArmArmorExtention, TextureHandler.CHEST_ARMOR);
    }

    public static void addGenericRightArmLayers(ExtendableModelRenderer rightArm)
    {
        rightArm.addLayer(GenericTextureValues.rightArm, GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        rightArm.addLayer(GenericTextureValues.rightSleeve, GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        rightArm.addLayer(GenericTextureValues.rightArmArmor, GenericTextureValues.armor_Size, GenericTextureValues.ArmArmorExtention, TextureHandler.CHEST_ARMOR);
    }

    public static void addGenericLeftLegLayers(ExtendableModelRenderer leftLeg)
    {
        leftLeg.addLayer(GenericTextureValues.leftLeg, GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        leftLeg.addLayer(GenericTextureValues.leftPants, GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        leftLeg.addLayer(GenericTextureValues.leftLegArmor, GenericTextureValues.armor_Size, GenericTextureValues.leftlegArmorExtention, TextureHandler.LEG_ARMOR);
        leftLeg.addLayer(GenericTextureValues.leftLegArmor, GenericTextureValues.armor_Size,  GenericTextureValues.leftFootArmorExtention, TextureHandler.FOOT_ARMOR);
    }

    public static void addGenericRightLegLayers(ExtendableModelRenderer rightLeg)
    {
        rightLeg.addLayer(GenericTextureValues.rightLeg, GenericTextureValues.skin_Size, GenericTextureValues.innerExtention, TextureHandler.PLAYER_SKIN);
        rightLeg.addLayer(GenericTextureValues.rightPants, GenericTextureValues.skin_Size, GenericTextureValues.outerExtention, TextureHandler.PLAYER_SKIN);
        rightLeg.addLayer(GenericTextureValues.rightLegArmor, GenericTextureValues.armor_Size, GenericTextureValues.rightlegArmorExtention, TextureHandler.LEG_ARMOR);
        rightLeg.addLayer(GenericTextureValues.rightLegArmor, GenericTextureValues.armor_Size, GenericTextureValues.rightFootArmorExtention, TextureHandler.FOOT_ARMOR);
    }
}
