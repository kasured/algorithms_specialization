package space.kasured.coursera;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * Test suite to cover Minimal Spanning Tree problem
 */
public abstract class Mst {
    private static final Path INPUT_FILE_PATH = Paths.get("src/main/resources/primsMst.input");

    static final int SAMPLE_NODES_SIZE = 500;
    private static final int SAMPLE_EDGES_SIZE = 2184;
    private static final long EXPECTED_MST_COST = -3_612_829L;


    /*private static final Path INPUT_FILE_PATH = Paths.get("src/main/resources/primsMst_02.input");

    static final int SAMPLE_NODES_SIZE = 8;
    private static final int SAMPLE_EDGES_SIZE = 14;
    private static final long EXPECTED_MST_COST = 15L;*/

    /*private static final Path INPUT_FILE_PATH = Paths.get("src/main/resources/primsMst_01.input");

    static final int SAMPLE_NODES_SIZE = 4;
    private static final int SAMPLE_EDGES_SIZE = 5;
    private static final long EXPECTED_MST_COST = 7L;*/

    private final Function<Graph, Long> mstCalculator;

    protected Mst(Function<Graph, Long> mstCalculator) {
        this.mstCalculator = mstCalculator;
    }

    private static Stream<String> getTestCaseStream() throws IOException {
        return Files.lines(INPUT_FILE_PATH, StandardCharsets.UTF_8);
    }

    @Test(dependsOnMethods = {"testGraphCreation", "testReadSample"})
    public void testMstCost() throws IOException {
        final Graph graph = Graph.fromSource(INPUT_FILE_PATH);
        final Long mstCost = mstCalculator.apply(graph);

        Assert.assertEquals(EXPECTED_MST_COST, (long) mstCost, "Mst cost failed");
    }

    @Test(dependsOnMethods = "testReadSample")
    public void testGraphCreation() throws IOException {
        final Graph graph = Graph.fromSource(INPUT_FILE_PATH);
        Assert.assertEquals(graph.nodes.size(), SAMPLE_NODES_SIZE, "Num of nodes failed");
        Assert.assertEquals(graph.edges.size(), SAMPLE_EDGES_SIZE, "Num of edges failed");

        System.out.println(graph.getOutgoingFor(1));
        System.out.println(graph.getIngoingFor(1));
    }

    @Test
    public void testReadSample() throws IOException {
        final String header = getTestCaseStream().limit(1).findFirst().orElseThrow(IllegalArgumentException::new);
        Assert.assertTrue(header != null && !header.isEmpty());

        final String[] nodesAndEdges = header.split(" ");

        Assert.assertEquals(SAMPLE_NODES_SIZE, parseInt(nodesAndEdges[0]), "Number of nodes failed");
        Assert.assertEquals(SAMPLE_EDGES_SIZE, parseInt(nodesAndEdges[1]), "Number of edges failed");
    }

    static final class Node {
        int id;
        /**
         * This field reflects the minimal incoming edge going from discovered MST
         */
        long minimalIncomingWeight;

        @Override
        public String toString() {
            return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .add("minimalIncoming=" + minimalIncomingWeight)
                    .toString();
        }
    }

    static final class Edge {
        int from;
        int to;
        long weight;

        @Override
        public String toString() {
            return new StringJoiner(", ", Edge.class.getSimpleName() + "[", "]")
                    .add("from=" + from)
                    .add("to=" + to)
                    .add("weight=" + weight)
                    .toString();
        }
    }

    static final class Graph {

        List<Node> nodes;

        List<Edge> edges;

        private Map<Integer, List<Edge>> nodeIdToOutgoing;

        private Map<Integer, List<Edge>> nodeIdToIngoing;

        private Map<Integer, Node> nodeById;

        static Graph fromSource(final Path pathToSource) throws IOException {

            final List<RowInput> rawInput = Files.lines(pathToSource, StandardCharsets.UTF_8)
                    .skip(1).map(row -> {
                        final String[] split = row.split(" ");
                        final RowInput rowInput = new RowInput();
                        rowInput.from = parseInt(split[0]);
                        rowInput.to = parseInt(split[1]);
                        rowInput.weight = parseLong(split[2]);
                        return rowInput;
                    }).collect(Collectors.toList());

            final List<Edge> edges = rawInput.stream().map(row -> {
                final Edge edge = new Edge();
                edge.from = row.from;
                edge.to = row.to;
                edge.weight = row.weight;
                return edge;
            }).collect(Collectors.toList());

            final List<Node> nodes = rawInput.stream().collect(
                    HashSet<Integer>::new,
                    (sink, input) -> {
                        sink.add(input.from);
                        sink.add(input.to);
                    },
                    AbstractCollection::addAll
            ).stream().map(id -> {
                final Node node = new Node();
                node.id = id;
                node.minimalIncomingWeight = Long.MAX_VALUE;
                return node;
            }).collect(Collectors.toList());

            final Graph graph = new Graph();
            graph.nodes = nodes;
            graph.edges = edges;

            graph.nodeIdToOutgoing = edges.stream().collect(Collectors.groupingBy(edge -> edge.from));

            graph.nodeIdToIngoing = edges.stream().collect(Collectors.groupingBy(edge -> edge.to));

            graph.nodeById = nodes.stream().collect(Collectors.toMap(node -> node.id, Function.identity()));

            return graph;
        }

        public List<Edge> getOutgoingFor(int nodeId) {
            return nodeIdToOutgoing.getOrDefault(nodeId, Collections.emptyList());
        }

        public List<Edge> getIngoingFor(int nodeId) {
            return nodeIdToIngoing.getOrDefault(nodeId, Collections.emptyList());
        }

        public Node getById(int id) {
            return nodeById.get(id);
        }
    }

    private static final class RowInput {
        int from;
        int to;
        long weight;
    }
}
