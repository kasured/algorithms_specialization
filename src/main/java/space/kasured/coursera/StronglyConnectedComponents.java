package space.kasured.coursera;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StronglyConnectedComponents {

    //private static final int NUM_NODES = 875714;
    //private static final int NUM_NODES = 11;

    public static void main(String[] args) throws IOException, URISyntaxException {
        //URI uri = StronglyConnectedComponents.class.getResource("/SCC1.txt").toURI();
        URI uri = StronglyConnectedComponents.class.getResource(args[0]).toURI();
        int numNodes = Integer.parseInt(args[1]);
        System.out.println(uri);
        FileSystem fs = initFileSystem(uri);
        Path path = Paths.get(uri);
        System.out.println(path);
        final Graph graph = Graph.fromFile(path, numNodes);
        System.out.println("Graph constructed with number of nodes " + graph.nodes.size());

        for(int i = 0; i < 5; i++) {
            System.out.println(graph.nodes.get(i));
        }

        final Stack<Node> kosaraju = phase1Rec(graph, numNodes);

        final List<List<Node>> scc = phase2Rec(kosaraju, numNodes);

        /*Stack<Node> kosaraju = phase1(graph.reversed, graph, numNodes);

        List<List<Node>> scc = phase2(kosaraju, numNodes);*/

        String sccSizes = scc.stream().map(List::size)
                .sorted(Comparator.reverseOrder()).limit(5).map(Object::toString).collect(Collectors.joining(","));

        System.out.println(scc);

        System.out.println(sccSizes);

        fs.close();
    }

    private static void dfs(final Node node, final BitSet visited, final Stack<Node> kosaraju, final Graph graph) {
        System.out.println("visiting node " + node);
        visited.set(node.data);
        for(Node adj: node.adjacent) {
            if(visited.get(adj.data) == false) {
                dfs(adj, visited, kosaraju, graph);
            }
        }
        // when adding to stack we need to add node from original graph
        System.out.println("Adding node to stack " + graph.getNode(node.data));
        kosaraju.push(graph.getNode(node.data));
    }

    private static Stack<Node> phase1Rec(final Graph graph, final int numNodes) {
        final BitSet visited = new BitSet(numNodes + 1);
        final Stack<Node> kosaraju = new Stack<>();

        // traverse on reverse graph
        for(Node graphNode: graph.reversed.nodes) {
            if(visited.get(graphNode.data) == false) {
                dfs(graphNode, visited, kosaraju, graph);
            }
        }

        return kosaraju;
    }

    private static void dfsAcc(final Node node, final BitSet visited, List<Node> scc) {
        scc.add(node);
        visited.set(node.data);
        for(Node adj: node.adjacent) {
            if(visited.get(adj.data) == false) {
                dfsAcc(adj, visited, scc);
            }
        }
    }

    private static List<List<Node>> phase2Rec(final Stack<Node> kosaraju, final int numNodes) {
        final BitSet visited = new BitSet(numNodes + 1);
        final List<List<Node>> sccComponents = new ArrayList<>();

        while(kosaraju.isEmpty() == false) {
            final Node node = kosaraju.pop();
            if(visited.get(node.data) == false) {
                final List<Node> scc = new ArrayList<>();
                System.out.println("Collecting components for " + node);
                dfsAcc(node, visited, scc);
                sccComponents.add(scc);
            }
        }

        return sccComponents;

    }

    // traverse reversed graph and put sinks to the queue
    private static Stack<Node> phase1(final Graph graphReversed, final Graph graph, int numNodes) {
        final BitSet visited = new BitSet(numNodes + 1);
        final Stack<Node> stack = new Stack<>();

        final Stack<Node> kosaraju = new Stack<>();

        for(Node node: graphReversed.nodes) {
            if(visited.get(node.data) == false) {
                stack.push(node);
                while(!stack.isEmpty()) {
                    Node fromStack = stack.pop();
                    if(visited.get(fromStack.data) == false) {
                        System.out.println("Visiting " + fromStack);
                        visited.set(fromStack.data);
                        boolean allVisited = true;
                        for(Node adj: fromStack.adjacent) {
                            stack.push(adj);
                            if(visited.get(adj.data) == false) {
                                allVisited = false;
                            }
                        }
                        if(allVisited == true || fromStack.adjacent.isEmpty()) {
                            // get original node
                            System.out.println("Adding node to queue " + graph.getNode(fromStack.data) + " " + fromStack);
                            kosaraju.push(graph.getNode(fromStack.data));
                        }
                    }
                }
            }
        }

        return kosaraju;

    }

    //traverse queue and for each element make dfs and find scc components
    private static List<List<Node>> phase2(final Stack<Node> kosaraju, int numNodes) {
        final BitSet visited = new BitSet(numNodes + 1);
        final List<List<Node>> scc = new ArrayList<>();

        final Stack<Node> stack = new Stack<>();

        while(kosaraju.isEmpty() == false) {
            Node node = kosaraju.pop();
            if(visited.get(node.data) == false) {
                System.out.println("From queue " + node);
                stack.push(node);
                List<Node> newScc = new ArrayList<>();
                while(stack.isEmpty() == false) {
                    Node fromStack = stack.pop();
                    if(visited.get(fromStack.data) == false) {
                        System.out.println("Visiting " + fromStack);
                        visited.set(fromStack.data);
                        newScc.add(fromStack);
                        for(Node adj: fromStack.adjacent) {
                            stack.push(adj);
                        }
                    }
                }
                scc.add(newScc);
            }
        }

        return scc;
    }

    private static FileSystem initFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.newFileSystem(uri, Collections.emptyMap());
        }catch(IllegalArgumentException e) {
            return FileSystems.getDefault();
        }
    }

    private static final class Graph {
        List<Node> nodes;
        Graph reversed;
        Map<Integer, Node> fastLookup;
        Node getNode(Integer data) {
            return fastLookup.get(data);
        }
        static Graph fromFile(Path path, int numNodes) throws IOException {

            final Map<Integer, List<Integer>> inputMap = Files.lines(path).map(line -> {
                String[] fromTo = line.split(" ");
                final FromTo ft = new FromTo();
                ft.from = Integer.parseInt(fromTo[0]);
                ft.to = Integer.parseInt(fromTo[1]);
                return ft;
            }).collect(Collectors.groupingBy(ft -> ft.from, Collectors.mapping(ft -> ft.to, Collectors.toList())));

            final Map<Integer, Node> fastLookup = IntStream.rangeClosed(1, numNodes).boxed().collect(Collectors.toMap(data -> data, data -> {
                Node node = new Node();
                node.data = data;
                return node;
            }));

            final Map<Integer, Node> fastLookupReversed = IntStream.rangeClosed(1, numNodes).boxed().collect(Collectors.toMap(data -> data, data -> {
                Node node = new Node();
                node.data = data;
                return node;
            }));

            inputMap.forEach((data, adjacent) -> {
                Node node = fastLookup.get(data);
                Node nodeR = fastLookupReversed.get(data);

                for (Integer adj : adjacent) {
                    node.adjacent.add(fastLookup.get(adj));
                    fastLookupReversed.get(adj).adjacent.add(nodeR);
                }
            });

            final Graph graph = new Graph();
            graph.nodes = new ArrayList<>(fastLookup.values());

            graph.fastLookup = fastLookup;

            final Graph graphR = new Graph();
            graphR.nodes = new ArrayList<>(fastLookupReversed.values());

            graph.reversed = graphR;

            return graph;
        }
    }

    private static final class FromTo {
        int from;
        int to;
    }

    private static final class Node {
        int data;
        List<Node> adjacent = new ArrayList<>();

        @Override
        public String toString() {
            return "Node{" + data + ", c=" + adjacent.size() + "}";
        }
    }
}
