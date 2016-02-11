package coffee.cypher.skills;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResearchMap implements INBTSerializable<NBTTagCompound> {
    private static final int STRING_ID = new NBTTagString("").getId();
    private final BiMap<ResearchNode, String> nodeIds;
    private final Map<Integer, ResearchNode> hashes;
    private final String name;

    public ResearchMap(String name, List<ResearchNode> nodes) {
        this.name = name;

        nodes.forEach(ResearchNode::lock);

        nodeIds = HashBiMap.create(nodes.size());
        hashes = HashBiMap.create(nodes.size());

        for (ResearchNode n : nodes) {
            nodeIds.put(n, n.getName());

            if (hashes.containsKey(n.getName().hashCode())) {
                if (n.getName().equals(hashes.get(n.getName().hashCode()).getName())) {
                    throw new IllegalArgumentException("Detected nodes with same name in research map" + name);
                }
                throw new IllegalStateException("Detected hash collision between nodes " + n.getName()
                        + " and " + hashes.get(n.getName().hashCode()).getName() + " in map " + name
                        + ". Please report this incident to current maintainer of SkillsAPI");
            }

            hashes.put(n.getName().hashCode(), n);
        }

        validate();
    }

    private void validate() {
        HashSet<ResearchNode> scanned = new HashSet<>();
        Set<ResearchNode> nodes = nodeIds.keySet();
        boolean scan = nodes.stream().filter(n -> n.getDependencies().isEmpty())
                .allMatch(n -> isBranchValid(n, scanned, new HashSet<>()));
        if (!(scan && (scanned.size() == nodes.size()))) {
            throw new IllegalArgumentException("Research map " + name +
                    " contains dependency cycle and could not be registered");
        }
    }

    private boolean isBranchValid(ResearchNode start, Set<ResearchNode> scanned, Set<ResearchNode> scanning) {
        scanning.add(start);
        for (ResearchNode next : start.getDependants()) {
            if (scanned.contains(next)) {
                return true;
            }

            if (scanning.contains(next)) {
                return false;
            }

            if (!isBranchValid(next, scanned, scanning)) {
                return false;
            }
        }
        scanning.remove(start);
        scanned.add(start);
        return true;
    }

    public final ResearchNode getNode(String name) {
        return nodeIds.inverse().get(name);
    }

    final Set<ResearchNode> getNodes() {
        return nodeIds.keySet();
    }

    final ResearchNode getNodeFromHash(int hash) {
        return hashes.get(hash);
    }

    public final String getName() {
        return name;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        for (ResearchNode e : nodeIds.keySet()) {
            NBTTagCompound nodeTag = e.serialize();

            NBTTagList depTag1 = new NBTTagList();
            e.getDependencies()
                    .stream().map(ResearchNode::getName)
                    .forEach(n -> depTag1.appendTag(new NBTTagString(n)));

            nodeTag.setTag("dependencies", depTag1);

            NBTTagList depTag2 = new NBTTagList();
            e.getDependants()
                    .stream().map(ResearchNode::getName)
                    .forEach(n -> depTag2.appendTag(new NBTTagString(n)));

            nodeTag.setTag("dependants", depTag2);

            tag.setTag(e.getName(), nodeTag);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        nodeIds.clear();
        for (String e : tag.getKeySet()) {
            ResearchNode node = ResearchNode.deserialize(tag.getCompoundTag(e));
            nodeIds.put(node, node.getName());
        }
        
        for (ResearchNode e : nodeIds.keySet()) {
            e.dependencies.clear();
            NBTTagList dependencies = tag.getCompoundTag(e.getName())
                    .getTagList("dependencies", STRING_ID);

            for (int i = 0; i < dependencies.tagCount(); i++) {
                String depName = dependencies.getStringTagAt(i);
                e.dependencies.add(nodeIds.inverse().get(depName));
            }

            e.dependants.clear();
            NBTTagList dependants = tag.getCompoundTag(e.getName())
                    .getTagList("dependants", STRING_ID);

            for (int i = 0; i < dependants.tagCount(); i++) {
                String depName = dependants.getStringTagAt(i);
                e.dependants.add(nodeIds.inverse().get(depName));
            }
        }

        validate();
    }
}