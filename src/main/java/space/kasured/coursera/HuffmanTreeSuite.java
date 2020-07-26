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
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HuffmanTreeSuite {
    private static final Logger logger = LoggerFactory.getLogger(HuffmanTreeSuite.class);

    @DataProvider(name = "samples")
    public static Object[][] testSampleProvider() {
        return new Object[][]{
                {new Sample("src/main/resources/huffman_1.txt", 5, 3, 2)},
                {new Sample("src/main/resources/huffman.txt", 1_000, 19, 9)}
        };
    }

    @Test(dataProvider = "samples")
    public void testHuffmanTree(final Sample sample) throws IOException {
        final List<Long> weights = streamFromResource(sample.inputPath).skip(1).map(Long::parseLong)
                .collect(Collectors.toList());

        Assert.assertEquals(weights.size(), sample.numberOfSymbols, "Expected length of alphabet fails");

        final Queue<TreeNode> minHeap = new PriorityQueue<>(weights.size(), Comparator.comparing(tn -> tn.weight));
        weights.forEach(weight -> minHeap.add(new TreeNode(weight, null, null)));

        while (minHeap.size() > 1) {
            // extract two symbols or meta-symbols from the heap with minimal weights
            final TreeNode first = minHeap.poll();
            final TreeNode second = !minHeap.isEmpty() ? minHeap.poll() : null;
            if (second == null) {
                // we exhausted the input and tree is ready
                break;
            } else {
                minHeap.add(mergeSymbols(first, second));
            }
        }

        final TreeNode hTree = minHeap.poll();

        logger.info("Head of the Huffman Tree is {}", hTree.weight);

        /*final StringBuilder sb = new StringBuilder();
        preOrder(hTree, sb);*/

        final int height = treeDepth(hTree);
        logger.info("Height of the tree is {}", height);
        Assert.assertEquals(height - 1, sample.maximumLength, "Maximum length of the codeword failed");

        final int minLeafHeight = minLeafDepth(hTree);
        logger.info("Min Leaf Height of the tree is {}", minLeafHeight);
        Assert.assertEquals(minLeafHeight - 1, sample.minimumLength, "Minimum length of the codeword failed");

    }

    private TreeNode mergeSymbols(TreeNode firstSymbolNode, TreeNode secondSymbolNode) {
        logger.info("Merging symbols nodes with freq {} and {}", firstSymbolNode.weight, secondSymbolNode.weight);
        return new TreeNode(firstSymbolNode.weight + secondSymbolNode.weight, firstSymbolNode, secondSymbolNode);
    }

    private static final class TreeNode {
        private final long weight;
        private final TreeNode left;
        private final TreeNode right;

        private TreeNode(long weight, TreeNode left, TreeNode right) {
            this.weight = weight;
            this.left = left;
            this.right = right;
        }
    }

    private static final void preOrder(final TreeNode tNode, final StringBuilder sb) {
        if (tNode == null) {
            sb.append("X");
            return;
        }
        sb.append("#").append(tNode.weight);
        preOrder(tNode.left, sb);
        preOrder(tNode.right, sb);
    }

    private static final int treeDepth(final TreeNode tNode) {
        if (tNode == null) {
            return 0;
        }
        return 1 + Math.max(treeDepth(tNode.left), treeDepth(tNode.right));
    }

    private static final int minLeafDepth(final TreeNode tNode) {
        if (tNode == null) {
            return 0;
        }
        return 1 + Math.min(minLeafDepth(tNode.left), minLeafDepth(tNode.right));
    }

    private static final Stream<String> streamFromResource(final String resource) throws IOException {
        return Files.lines(Paths.get(resource), StandardCharsets.UTF_8);
    }

    private static final class Sample {
        private final String inputPath;
        private final int numberOfSymbols;
        private final int maximumLength;
        private final int minimumLength;

        private Sample(String inputPath, int numberOfSymbols, int maximumLength, int minimumLength) {
            this.inputPath = inputPath;
            this.numberOfSymbols = numberOfSymbols;
            this.maximumLength = maximumLength;
            this.minimumLength = minimumLength;
        }
    }
}
