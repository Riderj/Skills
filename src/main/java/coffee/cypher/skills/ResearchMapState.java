package coffee.cypher.skills;

import com.google.common.collect.BiMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public final class ResearchMapState implements INBTSerializable<NBTTagCompound> {
    private Map<ResearchNode, NodeState> state;
    private final ResearchMap map;

    public ResearchMapState(ResearchMap ref) {
        map = ref;
        state = new HashMap<>();
        for (ResearchNode entry : map.getNodes()) {
            state.put(entry, entry.getDefaultState());
        }
    }

    public NodeState getStateOfNode(ResearchNode node) {
        return state.get(node);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (ResearchNode e : map.getNodes()) {
            tag.setString(e.getName(), state.get(e).name());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            nbt = new NBTTagCompound();
        }

        for (ResearchNode e : map.getNodes()) {
            if (nbt.hasKey(e.getName())) {
                state.put(e, NodeState.valueOf(nbt.getString(e.getName())));
            } else {
                state.put(e, e.getDefaultState());
            }
        }
    }

    public void setState(ResearchNode node, NodeState nodeState) {
        state.put(node, nodeState);

        refreshState();
    }

    private void refreshState() {
        for (Map.Entry<ResearchNode, NodeState> e : state.entrySet()) {
            ResearchNode node = e.getKey();
            if ((e.getValue() == NodeState.LOCKED) && node.prerequisitesCleared(this)) {
                e.setValue(NodeState.AVAILABLE);
            }

            if ((e.getValue() == NodeState.AVAILABLE) && !node.prerequisitesCleared(this)) {
                e.setValue(NodeState.LOCKED);
            }
        }
    }

    public ResearchMap getParentMap() {
        return map;
    }

    Map<ResearchNode, NodeState> getState() {
        return state;
    }

    void setState(Map<ResearchNode, NodeState> state) {
        this.state = state;
    }

    public enum NodeState {
        LOCKED,
        AVAILABLE,
        RESEARCHED
    }
}
