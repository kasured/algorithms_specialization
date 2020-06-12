package space.kasured.coursera;

import org.testng.Assert;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class PrimsMst extends Mst {
    protected PrimsMst() {
        super(PrimsMst::calculateMstNodes);
    }

    private static long calculateMstNodes(Graph graph) {

        final List<Node> mstNodes = new ArrayList<>(graph.nodes.size());
        final BitSet trackMstNodes = new BitSet(graph.nodes.size());

        final PriorityQueue<Node> minHeap = new PriorityQueue<>(
                Comparator.comparing(node -> node.minimalIncomingWeight)
        );

        // 1. Take any arbitrary node assign the incoming edge to zero
        final List<Node> nodes = graph.nodes;
        final Node first = nodes.get(0);
        first.minimalIncomingWeight = 0L;
        System.out.println("First node is initialized " + first);

        // 2. Put all the nodes with initialized weights
        minHeap.addAll(nodes);

        Assert.assertEquals(minHeap.size(), SAMPLE_NODES_SIZE, "minHeap size failed");
        Assert.assertEquals(minHeap.peek(), first, "minHeap peak failed");

        System.out.println("min heap nodes are " + nodes);

        // 3. Extract the node with the minimal incoming from MST weight
        //    Get all the adjacent nodes that are not yet in MST
        //    Update their minimalIncomingWeight
        //    Remove and reinsert them
        while (!minHeap.isEmpty()) {
            final Node extracted = minHeap.poll();
            mstNodes.add(extracted);
            trackMstNodes.set(extracted.id);
            final List<Edge> outgoing = graph.getOutgoingFor(extracted.id);
            final List<Edge> ingoing = graph.getIngoingFor(extracted.id);

            final List<Edge> incident = new ArrayList<>();
            incident.addAll(outgoing);
            incident.addAll(ingoing);

            System.out.println("found incident nodes for node " + extracted);
            System.out.println("" + incident);
            for (Edge inc : incident) {
                if (!trackMstNodes.get(inc.to)) {
                    processIncidentInHeap(minHeap, inc.to, inc.weight);
                }
                if (!trackMstNodes.get(inc.from)) {
                    processIncidentInHeap(minHeap, inc.from, inc.weight);
                }
            }
        }

        return mstNodes.stream().map(node -> node.minimalIncomingWeight).reduce(Long::sum)
                .orElseThrow(() -> new IllegalArgumentException("sum on empty stream"));
    }

    private static void processIncidentInHeap(PriorityQueue<Node> minHeap, int nodeId, long weight) {
        final Node inHeap = findById(minHeap, nodeId);
        System.out.println("found node not in mst " + inHeap);
        final long currentMinimalWeight = inHeap.minimalIncomingWeight;
        final long newWeight = Math.min(weight, currentMinimalWeight);
        //remove previous and insert new one
        minHeap.remove(inHeap);
        final Node newNode = new Node();
        newNode.id = nodeId;
        newNode.minimalIncomingWeight = newWeight;
        System.out.println("reinsert node " + newNode);
        minHeap.add(newNode);
    }

    // this one is linear which sucks but can be improved
    private static Node findById(final PriorityQueue<Node> minHeap, final int id) {
        return minHeap.stream().filter(node -> node.id == id)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("cannot find node by id " + id));
    }
}
