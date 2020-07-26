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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaximumWeightIndependentSet {

    private static final Logger logger = LoggerFactory.getLogger(MaximumWeightIndependentSet.class);

    @DataProvider(name = "samples")
    public static Object[][] testSampleProvider() {
        return new Object[][]{
                {new Sample("src/main/resources/mwis_1.txt", 4, 8, "0101")},
                {new Sample("src/main/resources/mwis_2.txt", 10, 2616, "0101010101")},
                {new Sample("src/main/resources/mwis_3.txt", 10, 2533, "1010010010")},
                {new Sample("src/main/resources/mwis_4.txt", 10, 2617, "0101001001")},
                {new Sample("src/main/resources/mwis.txt", 1_000, 2_955_353_732L, "1010100101001010010101010101001010010101010101010101010101010101010010010010101010101001010101010101001001010101001010010101010100101001001010101010101010101001010101010100101010101010101001001010101010101010101001010100101001010101010101010010101010010101010010101010101010101010101010101001010101010101010101010101010010101010101010101010101010101010101010101010101010101010101010101010101010101010010101010100101010010100101010101010101010101010101001010101010101010101010101001010100101010101010100101001010101001010101010101010010010101010100101010101010100100100101010101010100101010101010101010101010101001010101010101001010101001010101010101001010101010101010010101001001001010101010100101010010101010100101010101010010101001010101010101001010100101001001001010101010101001001010101001010101010100100100101010010101010101001010010101010010010101010101010010101001010010101010101010101010101010010101010101010101010010010100101010101010101010101010101010101010010101010101010101010101010100101", true)}
        };
    }

    @Test(dataProvider = "samples")
    public void testMaximumWeightIndependentPath(final Sample sample) throws IOException {
        final List<Long> nodes = streamFromResource(sample.inputPath).skip(1).map(Long::parseLong)
                .collect(Collectors.toList());

        Assert.assertEquals(nodes.size(), sample.numNodes, "Number of expected nodes failed");
        logger.info("Nodes are {}", nodes);

        final long[] solution = new long[nodes.size() + 1];

        // base case solution[0] = 0
        // base case solution[1] = nodes.get(0)
        solution[0] = 0L;
        solution[1] = nodes.get(0);
        for (int i = 2; i < nodes.size() + 1; i++) {
            solution[i] = Math.max(solution[i - 1], solution[i - 2] + nodes.get(i - 1));
        }

        logger.info("Solution array is {}", solution);
        Assert.assertEquals(solution[nodes.size()], sample.pathSum, "Maximum Weight Independent set failed");

        final char[] pathString = new char[nodes.size()];

        int i = solution.length - 1;
        while(i >= 1) {
            if (solution[i - 1] >= ((i == 1) ? 0 : solution[i - 2]) + nodes.get(i - 1)) {
                pathString[i - 1] = '0';
                i = i - 1;
            } else {
                pathString[i - 1] = '1';
                if(i != 1) {
                    pathString[i - 2] = '0';
                }
                i = i - 2;
            }
        }

        final String result = String.valueOf(pathString);

        logger.info("Path string is {}", result);
        Assert.assertEquals(result, sample.pathNodes, "Path nodes assertion failed");

        if (sample.isCoursera) {
            final char[] c = new char[8];
            c[0] = result.charAt(0);
            c[1] = result.charAt(1);
            c[2] = result.charAt(2);
            c[3] = result.charAt(3);
            c[4] = result.charAt(16);
            c[5] = result.charAt(116);
            c[6] = result.charAt(516);
            c[7] = result.charAt(996);
            logger.info("Output for coursera is {}", String.valueOf(c));
        }

    }

    private static Stream<String> streamFromResource(final String resource) throws IOException {
        return Files.lines(Paths.get(resource), StandardCharsets.UTF_8);
    }

    private static final class Sample {
        private final String inputPath;
        private final int numNodes;
        private final long pathSum;
        private final String pathNodes;
        private final boolean isCoursera;


        private Sample(String inputPath, int numNodes, long pathSum, String pathNodes) {
            this(inputPath, numNodes, pathSum, pathNodes, false);
        }

        private Sample(String inputPath, int numNodes, long pathSum, String pathNodes, boolean isCoursera) {
           this.isCoursera = isCoursera;
           this.inputPath = inputPath;
           this.numNodes = numNodes;
           this.pathNodes = pathNodes;
           this.pathSum = pathSum;
        }
    }

}
