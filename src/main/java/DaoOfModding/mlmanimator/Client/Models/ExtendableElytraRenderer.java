package DaoOfModding.mlmanimator.Client.Models;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ExtendableElytraRenderer extends ExtendableModelRenderer
{
    public ExtendableElytraRenderer clone()
    {
        ExtendableElytraRenderer copy = new ExtendableElytraRenderer(name);
        copy(copy, "");

        return copy;
    }

    public ExtendableElytraRenderer(String limbName)
    {
        super(limbName);
    }

    @Override
    public void tick(AbstractClientPlayer player)
    {
        super.tick(player);

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        // Set the elytra parts to be invisible if the eyltra is not equipped
        if (chest.getItem() == Items.ELYTRA)
            mPart.visible = true;
        else
            mPart.visible = false;
    }
}
