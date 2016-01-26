package coffee.cypher.skills;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

class MapSyncMessage implements IMessage, IMessageHandler<MapSyncMessage, IMessage> {
    NBTTagCompound tag;

    public MapSyncMessage() {}

    public MapSyncMessage(NBTTagCompound tag) {
        this.tag = tag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        try {
            tag = pb.readNBTTagCompoundFromBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeNBTTagCompoundToBuffer(tag);
    }

    @Override
    public IMessage onMessage(MapSyncMessage message, MessageContext ctx) {
        ResearchProperty.adaptToServerMaps(message.tag);
        return null;
    }
}
