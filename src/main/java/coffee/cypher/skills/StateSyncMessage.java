package coffee.cypher.skills;

import coffee.cypher.skills.ResearchMapState.NodeState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.Map;

final class StateSyncMessage implements IMessage {
    private Map<ResearchNode, NodeState> states;
    private ResearchMap map;

    Map<ResearchNode, NodeState> getStates() {
        return states;
    }

    ResearchMap getMap() {
        return map;
    }

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
            states.put(map.getNodeFromHash(pb.readInt()), NodeState.values()[pb.readInt()]);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        String name = map.getName();
        pb.writeInt(name.length());
        pb.writeString(name);
        pb.writeInt(states.size());
        for (Map.Entry<ResearchNode, NodeState> e : states.entrySet()) {
            pb.writeInt(e.getKey().getName().hashCode());
            pb.writeInt(e.getValue().ordinal());
        }
    }
}
