package space.kasured.coursera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KruskalsMst extends Mst {

    private static final Logger logger = LoggerFactory.getLogger(KruskalsMst.class);

    protected KruskalsMst() throws IOException {
        super(KruskalsMst::calculateMstNodes);
    }

    private static long calculateMstNodes(GraphSuite.Graph otherGraph) {

        final Graph graph = Graph.from(otherGraph);

        graph.edges.sort(edgeComparator);

        logger.debug("Sorted list of edges {}", graph.edges);

        // upper bound on the number of edges
        final List<GraphSuite.Edge> mst = new ArrayList<>(graph.edges.size());
        final UnionFindKruskal unionFind = new UnionFindKruskal(graph);

        for (GraphSuite.Edge edge : graph.edges) {
            // check if the edge forms the cycle when added to MST
            // if not then add to the mst otherwise just skip
            logger.debug("processing edge {}", edge);
            if(!formsCycle(edge, graph, unionFind)) {
                logger.debug("adding edge to MST {}", edge);
                mst.add(edge);
                updateStructure(edge, graph, unionFind);
                logger.debug("UnionFind after update {}", unionFind);
            }
        }

        return mst.stream().mapToLong(edge -> edge.weight).sum();
    }

    private static void updateStructure(GraphSuite.Edge edge, Graph graph, UnionFindKruskal unionFind) {

        final Node from = graph.getNodeById(edge.from);
        final Node to = graph.getNodeById(edge.to);

        unionFind.union(from, to);
    }

    private static boolean formsCycle(GraphSuite.Edge edge, Graph graph, UnionFindKruskal unionFind) {

        final Node from = graph.getNodeById(edge.from);
        final Node to = graph.getNodeById(edge.to);

        logger.debug("Checking cycle for nodes {} {}", from, to);

        return unionFind.findGroupFor(from).equals(unionFind.findGroupFor(to));
    }

    static class Node {
        int nodeId;
        int leaderId;

        @Override
        public String toString() {
            return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                    .add("nodeId=" + nodeId)
                    .add("leaderId=" + leaderId)
                    .toString();
        }
    }

    static class Graph {
        final List<Node> nodes;
        final List<GraphSuite.Edge> edges;
        private final GraphSuite.Graph generalGraph;

        final Map<Integer, Node> idToNode;

        private Graph(GraphSuite.Graph generalGraph) {
            this.nodes = generalGraph.nodes.stream().map(gNode -> {
                Node n = new Node();
                n.nodeId = gNode.id;
                n.leaderId = n.nodeId;
                return n;
            }).collect(Collectors.toList());

            this.idToNode = this.nodes.stream().collect(Collectors.toMap(node -> node.nodeId, Function.identity()));

            this.edges = generalGraph.edges;
            this.generalGraph = generalGraph;
        }

        Node getNodeById(int id) {
            return idToNode.get(id);
        }

        static Graph from(GraphSuite.Graph graph) {
            return new Graph(graph);
        }
    }

    static class UnionFindKruskal implements UnionFindStructure<Group, Node> {

        private final Map<Group, List<Node>> groups;

        public UnionFindKruskal(Graph graph) {
            // init the union find with the nodes
            this.groups = graph.nodes.stream().collect(Collectors.groupingBy(node -> new Group(node.leaderId)));
        }

        public int numOfGroups() {
            return groups.keySet().size();
        }

        @Override
        public Group findGroupFor(Node element) {
            final Group group = new Group(element.leaderId);
            logger.debug("found group {} for element {}", group, element);
            return group;
        }

        @Override
        public void union(Node element, Node otherElement) {
            // check what is the bigger group and fuse smaller into the bigger
            final Group group = new Group(element.leaderId);
            final Group otherGroup = new Group(otherElement.leaderId);

            final int elementGroupSize = groups.get(group).size();
            final int otherElementGroupSize = groups.get(otherGroup).size();

            // start fusing
            if(elementGroupSize <= otherElementGroupSize) {
                logger.debug("fusing group {} with size {} into group {} with size {}",
                        group, elementGroupSize, otherGroup, otherElementGroupSize
                );
                fuseInto(group, otherGroup);
            } else {
                logger.debug("fusing group {} with size {} into group {} with size {}",
                        otherGroup, otherElementGroupSize, group, elementGroupSize
                );
                fuseInto(otherGroup, group);
            }
        }

        private void fuseInto(Group source, Group target) {
            final List<Node> sourceElements = groups.remove(source);
            // update the leaders to correspond to the target's group leader id
            sourceElements.forEach(node -> node.leaderId = target.groupId);
            // finally add those elements to the target group
            groups.get(target).addAll(sourceElements);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", UnionFindKruskal.class.getSimpleName() + "[", "]")
                    .add("groups=" + groups)
                    .toString();
        }
    }

    private static final class Group {

        final int groupId;

        Group(int groupId) {
            this.groupId = groupId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Group group = (Group) o;
            return groupId == group.groupId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Group.class.getSimpleName() + "[", "]")
                    .add("groupId=" + groupId)
                    .toString();
        }
    }
}
