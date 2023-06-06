package DaoOfModding.mlmanimator.Network.Packets;

import DaoOfModding.mlmanimator.Common.Reflection;
import DaoOfModding.mlmanimator.Server.ServerBoundingBoxHandler;
import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EyeHeightPacket extends Packet
{
    float eyeHeight;

    public EyeHeightPacket(float height)
    {
        eyeHeight = height;
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeFloat(eyeHeight);
    }

    public static EyeHeightPacket decode(FriendlyByteBuf buffer)
    {
        EyeHeightPacket returnValue = new EyeHeightPacket(0);

        try
        {
            // Read in the sent values
            float height = buffer.readFloat();

            return new EyeHeightPacket(height);

        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e)
        {
            mlmanimator.LOGGER.warn("Exception while reading EyeHeight message: " + e);
            return returnValue;
        }
    }

    // Read the packet received over the network
    public void handle(Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived.isClient())
        {
            mlmanimator.LOGGER.warn("EyeHeightPacket was received by client - This should not happen");
            return;
        }

        ctx.enqueueWork(() -> processPacket(ctx.getSender()));
    }

    // Process received packet on the Server
    protected void processPacket(ServerPlayer sender)
    {
        ServerBoundingBoxHandler.setEyeHeight(sender.getUUID(), eyeHeight);
    }
}
