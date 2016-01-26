package coffee.cypher.skills;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

final class MapSyncMessage implements IMessage {
    private NBTTagCompound tag;

    NBTTagCompound getTag() {
        return tag;
    }

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


}
