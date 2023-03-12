package DaoOfModding.mlmanimator.Network.Packets;

import DaoOfModding.mlmanimator.Client.Models.MultiLimbedDimensions;
import DaoOfModding.mlmanimator.Common.Reflection;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.math.Vector3f;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

    public class BoundingBoxPacket extends Packet
    {
        Vector3f minSize;
        Vector3f maxSize;

        public BoundingBoxPacket(Vector3f min, Vector3f max)
        {
            minSize = min;
            maxSize = max;
        }

        @Override
        public void encode(FriendlyByteBuf buffer)
        {
            buffer.writeFloat(minSize.x());
            buffer.writeFloat(minSize.y());
            buffer.writeFloat(minSize.z());
            buffer.writeFloat(maxSize.x());
            buffer.writeFloat(maxSize.y());
            buffer.writeFloat(maxSize.z());
        }

        public static BoundingBoxPacket decode(FriendlyByteBuf buffer)
        {
            BoundingBoxPacket returnValue = new BoundingBoxPacket(null, null);

            try
            {
                // Read in the sent values
                Vector3f min = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
                Vector3f max = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());

                return new BoundingBoxPacket(min, max);

            }
            catch (IllegalArgumentException | IndexOutOfBoundsException e)
            {
                mlmanimator.LOGGER.warn("Exception while reading BoundingBox message: " + e);
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
                mlmanimator.LOGGER.warn("BoundingBoxPacket was received by client - This should not happen");
                return;
            }

            // Check to ensure that the packet has valid data values
            if (minSize == null)
            {
                mlmanimator.LOGGER.warn("BoundingBoxPacket was invalid: " + this.toString());
                return;
            }

            ctx.enqueueWork(() -> processPacket(ctx.getSender()));
        }

        // Process received packet on the Server
        protected void processPacket(ServerPlayer sender)
        {
            MultiLimbedDimensions dimensions = new MultiLimbedDimensions(minSize, maxSize);

            Reflection.setDimensions(sender, new EntityDimensions(dimensions.getBiggestWidth(), dimensions.getHeight(), false));
            sender.setBoundingBox(dimensions.makeBoundingBox(sender.position()));
        }
    }
