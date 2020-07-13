package space.kasured.coursera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ClusteringHammingSuite {

    private static final Set<Integer> HammingMasksForLt3 = generateHammingMasksForLt3();

    private final Logger logger = LoggerFactory.getLogger(ClusteringHammingSuite.class);

    @DataProvider(name = "samples")
    public static Object[][] testSampleProvider() {
        return new Object[][] {
                { new Sample("src/main/resources/clustering_big_1.txt", 11, 11, 6) },
                { new Sample("src/main/resources/clustering_big.txt", 200_000, 198_788, 6118) }
        };
    }

    @Test(dataProvider = "samples")
    public void testClusteringHamming(final Sample sample) throws IOException {

        //1. Read each line representing the node and convert its 24-bits representation to an Integer
        final List<Integer> nodesWithBits = streamFromResource(sample.inputSamplePath).skip(1)
                .map(line -> Integer.parseInt(line.replace(" ", ""), 2))
                .collect(Collectors.toList());

        Assert.assertEquals(nodesWithBits.size(), sample.expectedNumNodes, "Num of nodes expected failed");

        //2. Create a list of nodes
        final List<Node> nodes = IntStream.range(0, nodesWithBits.size()).mapToObj(idx -> {
            final Node node  = new Node(idx, nodesWithBits.get(idx));
            return node;
        }).collect(Collectors.toList());

        //3. Create a grouping by the 'bits' representation for a node
        final Map<Integer, List<Node>> bitsToNodes = nodes.stream().collect(Collectors.groupingBy(node -> node.bits));
        Assert.assertEquals(bitsToNodes.keySet().size(), sample.expectedNumGroupsByBits, "Num of init groups by bits expected failed");

        //4. Create a union find data structure for Nodes
        final LeaderUnionFindStructure<Node> unionFind = new LeaderUnionFindStructure<>(nodes);
        Assert.assertEquals(unionFind.numOfGroups(), sample.expectedNumNodes);

        //5. Generate Hamming 24 bit masks for Hamming distance [0,3)
        final Set<Integer> distanceMasks = HammingMasksForLt3;
        logger.debug("HammingMasksForLt3 {}", distanceMasks);

        //6. Now
        //  iterate through the set's keys
        //  xor with distances array
        // if the xor result is present in the map then fuse each node with the node from another group
        bitsToNodes.keySet().forEach(bits -> {
            distanceMasks.forEach(distance -> {
                final int xor = bits ^ distance;
                if (bitsToNodes.get(xor) != null) {
                   //fuse all nodes in that groups between each other
                    for (Node node1: bitsToNodes.get(bits)) {
                        for (Node node2: bitsToNodes.get(xor)) {
                            unionFind.union(node1, node2);
                        }
                    }
                }
            });
        });

        final int resultingNumOfGroups = unionFind.numOfGroups();
        logger.debug("Maximum number of clusters {}", resultingNumOfGroups);

        Assert.assertEquals(resultingNumOfGroups, sample.expectedNumOfFinalClusters);

    }

    private static Set<Integer> generateHammingMasksForLt3() {
        final Set<Integer> result = new LinkedHashSet<>();
        // distance 0
        result.add(0);

        //distance 1
        for (int i = 0; i < 24; i++) {
            result.add(1 << i);
        }

        //distance 2
        for (int i = 0; i < 23; i++) {
            for (int j = i + 1; j < 24; j++) {
                result.add((1 << i) | (1 << j));
            }
        }

        return result;
    }

    private static final class Node {
        private final int id;
        private final int bits;

        private Node(int id, int bits) {
            this.id = id;
            this.bits = bits;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return id == node.id &&
                    bits == node.bits;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, bits);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .add("bits=" + bits)
                    .toString();
        }
    }

    private static final Stream<String> streamFromResource(final String resource) throws IOException {
        return Files.lines(Paths.get(resource), StandardCharsets.UTF_8);
    }


    private static final class Sample {
        private final String inputSamplePath;
        private final int expectedNumNodes;
        private final int expectedNumGroupsByBits;
        private final int expectedNumOfFinalClusters;

        private Sample(String inputSamplePath, int expectedNumNodes, int expectedNumGroupsByBits, int expectedNumOfFinalClusters) {
            this.inputSamplePath = inputSamplePath;
            this.expectedNumNodes = expectedNumNodes;
            this.expectedNumGroupsByBits = expectedNumGroupsByBits;
            this.expectedNumOfFinalClusters = expectedNumOfFinalClusters;
        }
    }

}
