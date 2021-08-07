package DaoOfModding.mlmanimator.Client.Physics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.math.vector.Vector3d;

public class GravityClientPlayerEntity extends ClientPlayerEntity
{
    public GravityClientPlayerEntity(Minecraft p_i232461_1_, ClientWorld p_i232461_2_, ClientPlayNetHandler p_i232461_3_, StatisticsManager p_i232461_4_, ClientRecipeBook p_i232461_5_, boolean p_i232461_6_, boolean p_i232461_7_)
    {
        super(p_i232461_1_, p_i232461_2_, p_i232461_3_, p_i232461_4_, p_i232461_5_, p_i232461_6_, p_i232461_7_);
    }


    public void travel(Vector3d p_213352_1_)
    {
        super.travel(p_213352_1_);
    }
}
