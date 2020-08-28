package space.kasured.coursera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.StringJoiner;

public class OptimalBstSuite {

    private static final Logger logger = LoggerFactory.getLogger(OptimalBstSuite.class);

    @DataProvider(name = "samples")
    public static Object[][] testSamplesProvider() {
        return new Object[][] {
                { new TestCase(new double[] {0.05d, 0.4d}, 0.5d) },
                { new TestCase(new double[] {0.4d, 0.08d}, 0.56d) },
                { new TestCase(new double[] {0.05d, 0.08d}, 0.18d) },
                { new TestCase(new double[] {0.05d, 0.4d, 0.08d}, 0.66d) },
                { new TestCase(new double[] {0.05d, 0.4d, 0.08d, 0.04d, 0.1d, 0.1d, 0.23d}, 2.18d) }
        };
    }

    @Test(dataProvider = "samples")
    public void testOptimalBstSuite(final TestCase testCase) {
        logger.info("Input test case is {}", testCase);
        Assert.assertEquals(testCase.cost, computeOptimalBstCost(testCase.input));
    }

    private static double computeOptimalBstCost(final double[] weights) {
        // initialize the array

        final int len = weights.length;

        final double[][] array = new double[len][len];

        logger.info("length of array is {}", len);

        for (int s = 0; s < len; s++) {
            for (int i = 0; i < len; i++) {
                logger.info("{s={}, i={}}", s, i);
                if (i + s < len) {
                    double sum = sumBetween(weights, i, i + s);
                    double min = Double.MAX_VALUE;
                    for (int r = i; r <= i + s; r++) {
                        double arrLeft = r - 1 < 0 || r - 1 >= len ? 0 : array[i][r - 1];
                        double arrRight = r + 1 > i + s ? 0 : array[r + 1][i + s];
                        double candidate = arrLeft + arrRight;
                        min = Math.min(min, candidate);
                    }
                    array[i][i + s] = sum + min;
                }
            }
        }

        return array[0][len - 1];

    }

    //inclusive
    private static final double sumBetween(final double[] array, int start, int end) {
        double result = 0.0d;

        for (int i = start; i <= end; i++) {
            result += array[i];
        }

        return result;
    }

    private static final class TestCase {
        private final double[] input;
        private final double cost;

        private TestCase(double[] input, double cost) {
            this.input = input;
            this.cost = cost;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TestCase.class.getSimpleName() + "[", "]")
                    .add("input=" + Arrays.toString(input))
                    .add("cost=" + cost)
                    .toString();
        }
    }
}
