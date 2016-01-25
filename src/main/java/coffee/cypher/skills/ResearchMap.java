package coffee.cypher.skills;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.List;
import java.util.Set;

public class ResearchMap {
    protected BiMap<ResearchNode, Integer> nodeIds;
    protected String name;

    public ResearchMap(String name, List<ResearchNode> nodes) {
        this.name = name;

        int id = 0;
        nodeIds = HashBiMap.create(nodes.size());
        for (ResearchNode node : nodes) {
            nodeIds.put(node, id++);
        }
    }

    int getId(ResearchNode node) {
        return nodeIds.get(node);
    }

    ResearchNode getNode(int id) {
        return nodeIds.inverse().get(id);
    }

    BiMap<ResearchNode, Integer> getNodes() {
        return nodeIds;
    }

    public String getName() {
        return name;
    }
}
