package coffee.cypher.skills;

import coffee.cypher.skills.ResearchMapState.NodeState;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

final class StateSyncMessage implements IMessage, IMessageHandler<StateSyncMessage, IMessage> {
    private Map<Integer, NodeState> states;
    private ResearchMap map;

    public StateSyncMessage(ResearchMapState state) {
        this.map = state.getParentMap();
        this.states = state.getState();
    }

    public StateSyncMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        String name = pb.readStringFromBuffer(pb.readInt());
        map = ResearchProperty.getResearchMap(name);
        states = new HashMap<>();
        for (int i = pb.readInt(); i > 0; i--) {
            states.put(pb.readInt(), NodeState.values()[pb.readInt()]);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        String name = map.getName();
        pb.writeInt(name.length());
        pb.writeString(name);
        pb.writeInt(states.size());
        for (Map.Entry<Integer, NodeState> e : states.entrySet()) {
            pb.writeInt(e.getKey());
            pb.writeInt(e.getValue().ordinal());
        }
    }

    @Override
    public IMessage onMessage(StateSyncMessage message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(this::apply);
        return null;
    }

    public void apply() {
        ResearchMapState curState = ResearchProperty.getResearchMapState(Minecraft.getMinecraft().thePlayer, map);
        curState.setState(states);
    }
}
