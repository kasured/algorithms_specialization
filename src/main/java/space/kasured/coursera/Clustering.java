package space.kasured.coursera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Clustering extends GraphSuite {

    private final int k;
    private final long expectedMaximumSpacing;

    private static final Logger logger = LoggerFactory.getLogger(Clustering.class);

    Clustering() throws IOException {
        super(Paths.get("src/main/resources/clustering1.txt"), 500, 124750);
        this.k = 4;
        this.expectedMaximumSpacing = 106L;
    }

    /*Clustering() throws IOException {
        super(Paths.get("src/main/resources/clustering1_1.txt"), 6, 11);
        this.k = 3;
        this.expectedMaximumSpacing = 50L;
    }*/

    /*Clustering() throws IOException {
        super(Paths.get("src/main/resources/clustering1_2.txt"), 8, 100);
        this.k = 4;
        this.expectedMaximumSpacing = 21L;
    }*/

    /*Clustering() throws IOException {
        super(Paths.get("src/main/resources/clustering1_3.txt"), 16, 100);
        this.k = 4;
        this.expectedMaximumSpacing = 29L;
    }*/

    /*Clustering() throws IOException {
        super(Paths.get("src/main/resources/clustering1_4.txt"), 128, 100);
        this.k = 4;
        this.expectedMaximumSpacing = 106L;
    }*/

    @Test
    public void testClustering() throws IOException {
        final long maximumSpacing = calculateMaximumSpacingFor(k);
        Assert.assertEquals(maximumSpacing, this.expectedMaximumSpacing, "Maximum spacing does not match");
    }

    private long calculateMaximumSpacingFor(int k) {

        final KruskalsMst.Graph graph = KruskalsMst.Graph.from(this.graph);

        graph.edges.sort(edgeComparator);

        logger.debug("Sorted list of edges {}", graph.edges);

        final KruskalsMst.UnionFindKruskal unionFind = new KruskalsMst.UnionFindKruskal(graph);

        logger.debug("Number of groups is {}", unionFind.numOfGroups());

        long maximumClusterSpacing = 0L;

        for (int i = 0; i < graph.edges.size(); i++) {
            final GraphSuite.Edge edge =  graph.edges.get(i);
            logger.debug("number of groups {}", unionFind.numOfGroups());
            logger.debug("processing edge {}", edge);

            final KruskalsMst.Node from = graph.getNodeById(edge.from);
            final KruskalsMst.Node to = graph.getNodeById(edge.to);

            if(from.leaderId != to.leaderId) {
                unionFind.union(from, to);
                if (unionFind.numOfGroups() == k) {
                    // number of groups satisfies the requested k
                    // take the next edge node which will be the max spacing for the cluster
                    final Edge nextNotFused = findNextNotFused(unionFind, graph, graph.edges, i + 1);
                    maximumClusterSpacing = nextNotFused.weight;
                    logger.debug("We have now {} groups and the next edge node is {}", k, nextNotFused);
                    break;
                }
            } else {
                logger.debug("Groups are already fused leader: {} and leader: {}", from.leaderId, to.leaderId);
            }

        }

        return maximumClusterSpacing;
    }

    private Edge findNextNotFused(KruskalsMst.UnionFindKruskal unionFind, KruskalsMst.Graph graph, List<Edge> edges, int i) {
        for(int j = i; j < edges.size(); j++) {
            final Edge edge = edges.get(j);
            final KruskalsMst.Node from = graph.getNodeById(edge.from);
            final KruskalsMst.Node to = graph.getNodeById(edge.to);
            if (from.leaderId != to.leaderId) {
               logger.info("found next connecting edge {}", edge);
               return edge;
            } else {
               logger.info("Edge's from/to is already in the same group {}, {}", edge, from.leaderId);
            }
        }
        throw new IllegalArgumentException("Something is not right");
    }

}
