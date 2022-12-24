package DaoOfModding.mlmanimator.Client.Poses;

import net.minecraft.world.InteractionHand;

public class Arm
{
    public InteractionHand hand;
    public String upperLimb;
    public String lowerLimb;
    public boolean mirrored;

    public Arm(InteractionHand newHand, String newUpper, String newLower, boolean mirror)
    {
        hand = newHand;
        upperLimb = newUpper;
        lowerLimb = newLower;
        mirrored = mirror;
    }
}
