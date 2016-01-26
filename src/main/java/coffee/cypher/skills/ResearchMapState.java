package coffee.cypher.skills;

import com.google.common.collect.BiMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public final class ResearchMapState implements INBTSerializable<NBTTagCompound> {
    private Map<Integer, NodeState> state;
    private final ResearchMap map;

    public ResearchMapState(ResearchMap ref) {
        map = ref;
        state = new HashMap<>();
        for (BiMap.Entry<ResearchNode, Integer> entry : map.getNodes().entrySet()) {
            state.put(entry.getValue(), entry.getKey().getDefaultState());
        }
    }

    public NodeState getStateOfNode(ResearchNode node) {
        return state.get(map.getId(node));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (BiMap.Entry<ResearchNode, Integer> e : map.getNodes().entrySet()) {
            tag.setInteger(e.getKey().getName(), state.get(e.getValue()).ordinal());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            nbt = new NBTTagCompound();
        }

        for (BiMap.Entry<ResearchNode, Integer> e : map.getNodes().entrySet()) {
            if (nbt.hasKey(e.getKey().getName())) {
                state.put(e.getValue(), NodeState.values()[nbt.getInteger(e.getKey().getName())]);
            } else {
                state.put(e.getValue(), e.getKey().getDefaultState());
            }
        }
    }

    public void setState(ResearchNode node, NodeState nodeState) {
        state.put(map.getId(node), nodeState);

        refreshState();
    }

    private void refreshState() {
        for (Map.Entry<Integer, NodeState> e : state.entrySet()) {
            ResearchNode node = map.getNode(e.getKey());
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

    Map<Integer, NodeState> getState() {
        return state;
    }

    void setState(Map<Integer, NodeState> state) {
        this.state = state;
    }

    public enum NodeState {
        LOCKED,
        AVAILABLE,
        RESEARCHED
    }
}
