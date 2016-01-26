package coffee.cypher.skills;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.function.Function;

import static coffee.cypher.skills.ResearchMapState.*;

public class ResearchNode {
    final List<ResearchNode> dependencies;
    final List<ResearchNode> dependants;
    private final String name;
    protected final NodeState defaultState;
    private boolean locked;

    private static Map<String, Function<NBTTagCompound, ResearchNode>> deserializers = new TreeMap<>();
    private static final String ID = "skills_node";

    static {
        registerDeserializer(ID, ResearchNode::deserialize0);
    }

    private static ResearchNode deserialize0(NBTTagCompound tag) {
        String name = tag.getString("name");
        NodeState defState = NodeState.values()[tag.getInteger("defaultState")];
        return new ResearchNode(name, defState);
    }

    NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", name);
        tag.setString("deserializer", ID);
        tag.setInteger("defaultState", defaultState.ordinal());
        return tag;
    }

    static ResearchNode deserialize(NBTTagCompound tag) {
        return deserializers.get(tag.getString("deserializer")).apply(tag);
    }

    static void registerDeserializer(String id, Function<NBTTagCompound, ResearchNode> des) {
        if (deserializers.containsKey(id)) {
            throw new IllegalArgumentException("Node deserializer " + id + " already exists");
        }

        deserializers.put(id, des);
    }

    public ResearchNode(String name, NodeState defState, ResearchNode... pre) {
        this.name = name;
        defaultState = defState;
        dependencies = new ArrayList<>();
        dependants = new ArrayList<>();

        Collections.addAll(dependencies, pre);
        dependencies.forEach(n -> n.addDependant(this));
    }

    public ResearchNode(String name, ResearchNode... pre) {
        this(name, NodeState.LOCKED, pre);
    }

    public final List<ResearchNode> getDependencies() {
        return dependencies;
    }

    public final boolean dependsOn(ResearchNode node) {
        return dependencies.contains(node);
    }

    public NodeState getDefaultState() {
        return defaultState;
    }

    public final String getName() {
        return name;
    }

    private void addDependant(ResearchNode node) {
        if (locked) {
            throw new IllegalStateException("Node " + name + " is locked, cannot add new dependants");
        }

        dependants.add(node);
    }

    public boolean prerequisitesCleared(ResearchMapState state) {
        return dependencies.stream().map(state::getStateOfNode).allMatch(n -> (n == NodeState.RESEARCHED));
    }

    public final List<ResearchNode> getDependants() {
        return dependants;
    }

    final void lock() {
        locked = true;
    }
}
