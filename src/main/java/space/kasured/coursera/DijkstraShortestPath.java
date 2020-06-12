package space.kasured.coursera;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DijkstraShortestPath {
    public static void main(String[] args) throws URISyntaxException, IOException {

        URI uri = DijkstraShortestPath.class.getResource(args[0]).toURI();
        int numNodes = Integer.parseInt(args[1]);
        System.out.println(uri);
        FileSystem fs = initFileSystem(uri);
        Path path = Paths.get(uri);
        System.out.println(path);
        final Graph graph = Graph.fromFile(path, numNodes);
        System.out.println("Graph constructed with number of nodes " + graph.getNodes().size());
        System.out.println("Graph with edges " + graph.edges);
        System.out.println("Graph with edges size " + graph.edges.size());


        final List<Node> destNodes = Stream.of(7, 37, 59, 82, 99, 115, 133, 165, 188, 197)
                .map(graph::byId).collect(Collectors.toList());

        /*final List<Node> destNodes = Stream.of(2,3,4)
                .map(graph::byId).collect(Collectors.toList());*/

        final List<Integer> pathsTo = dijkstra(graph, graph.byId(1), destNodes);

        String result = pathsTo.stream().map(Object::toString).collect(Collectors.joining(","));

        System.out.println(result);

        fs.close();
    }

    private static FileSystem initFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.newFileSystem(uri, Collections.emptyMap());
        }catch(IllegalArgumentException e) {
            return FileSystems.getDefault();
        }
    }

    private static final class Edge {
        int from;
        int to;
        int weight;
        Edge(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Edge.class.getSimpleName() + "[", "]")
                    .add("from=" + from)
                    .add("to=" + to)
                    .add("weight=" + weight)
                    .toString();
        }
    }

    private static List<Integer> dijkstra(final Graph graph, Node source, List<Node> dest) {
        final int len = graph.getNodes().size();
        final BitSet visited = new BitSet(len + 1);

        final PriorityQueue<Node> minHeap = new PriorityQueue<>(Comparator.comparing(Node::getPath));

        //set min to pick it up first
        source.setPath(0);

        minHeap.addAll(graph.getNodes());

        final Map<Integer, Integer> result = new HashMap<>(len);

        while(minHeap.isEmpty() == false) {
            final Node minNode = minHeap.poll();
            if(visited.get(minNode.id) == false) {
                // set as visited
                visited.set(minNode.id);
                result.put(minNode.id, minNode.path == Integer.MAX_VALUE ? 1000000 : minNode.path);
                for(Node adj: minNode.adjacent) {
                    if(visited.get(adj.id) == false) {
                        // update the path with dijkstra criterion and add back to the que
                        // for the simplicity for now we ignore the fact that nodes will get to the queue again
                        final Node updatedNode = new Node(adj.id, minNode.path + graph.getWeight(minNode.id, adj.id), adj.adjacent);
                        minHeap.add(updatedNode);
                    }
                }
            }
        }

        return dest.stream().map(node -> result.get(node.id)).collect(Collectors.toList());

    }

    private static final class Graph {

        private final List<Edge> edges;
        private final Map<Integer, Node> byId;
        private final Map<Integer, Map<Integer, Integer>> weights;

        private Graph(final List<Edge> edges, int len) {

            this.edges = edges;

            final Map<Integer, Map<Integer, Integer>> weights = new HashMap<>();

            edges.forEach(edge -> {
                weights.compute(edge.from, (from, adj) -> {
                    if(adj == null) {
                        final Map<Integer, Integer> map = new HashMap<>();
                        map.put(edge.to, edge.weight);
                        return map;
                    } else {
                        adj.put(edge.to, edge.weight);
                        return adj;
                    }
                });
            });

            this.weights = weights;

            final Map<Integer, Node> byId = new HashMap<>(len);
            for(int i = 1; i <= len; i++) {
                final Node node = new Node(i, Integer.MAX_VALUE, new ArrayList<>());
                byId.put(i, node);
            }

            System.out.println("Processing edges " + edges);
            edges.forEach(edge -> {
                Node source = byId.get(edge.from);
                source.getAdjacent().add(byId.get(edge.to));
            });

            this.byId = byId;

        }

        public int getWeight(int from, int to) {
            return weights.get(from).get(to);
        }

        public Node byId(int id) {
            return byId.get(id);
        }

        public Collection<Node> getNodes() {
            return byId.values();
        }

        public static Graph fromFile(Path path, int numNodes) throws IOException {
            final List<Edge> e = Files.lines(path).flatMap(line -> {
                List<Edge> edges = new ArrayList<>();
                String[] args = line.split("\\s+");
                int from = Integer.parseInt(args[0].trim());
                for (int i = 1; i < args.length; i++) {
                    String[] toW = args[i].trim().split(",");
                    edges.add(new Edge(from, Integer.parseInt(toW[0]), Integer.parseInt(toW[1])));
                }
                return edges.stream();
            }).collect(Collectors.toList());

            return new Graph(e, numNodes);
        }
    }

    private static final class Node {

        public int getPath() {
            return path;
        }

        public List<Node> getAdjacent() {
            return adjacent;
        }

        public void setPath(int path) {
            this.path = path;
        }

        private final int id;
        private int path;
        private final List<Node> adjacent;
        public Node(int id, int path, final List<Node> adjacent) {
            this.id = id;
            this.path = path;
            this.adjacent = adjacent;
        }
    }
}
