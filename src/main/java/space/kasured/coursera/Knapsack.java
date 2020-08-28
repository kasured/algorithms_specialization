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

public class Knapsack {

    private static final Logger logger = LoggerFactory.getLogger(Knapsack.class);

    @DataProvider(name = "samples")
    public static Object[][] testSampleProvider() {
        return new Object[][]{
                { new TestCase("src/main/resources/knapsack0.txt", 6L, 4, 8) },
                { new TestCase("src/main/resources/knapsack00.txt", 190L, 6, 150) },
                { new TestCase("src/main/resources/knapsack000.txt", 76L, 95, 202) },
                { new TestCase("src/main/resources/knapsack1.txt", 10_000L, 100, 2493893) }
        };
    }

    @Test(dataProvider = "samples")
    public void testKnapsack(final TestCase testCase) throws IOException {
        final List<Item> items = streamFromResource(testCase.path).skip(1).map(Knapsack::itemFromString)
                .collect(Collectors.toList());
        Assert.assertEquals(items.size(), testCase.numOfItems);

        // array for solution
        final int n = items.size();
        final int capacity = (int) testCase.capacity;
        final long[][] A = new long[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 1; w <= capacity; w++) {
                if (w - items.get(i - 1).weight < 0) {
                    A[i][w] = A[i - 1][w];
                } else {
                    A[i][w] = Math.max(A[i - 1][w], A[i - 1][w - (int) items.get(i - 1).weight] + items.get(i - 1).value);
                }
            }
        }

        Assert.assertEquals(A[n][capacity], testCase.value);

    }

    private static final Item itemFromString(final String valueWeight) {
        final String[] valueWeightArr = valueWeight.split(" ");
        return new Item(Long.parseLong(valueWeightArr[0]), Long.parseLong(valueWeightArr[1]));
    }

    private static final Stream<String> streamFromResource(final String resource) throws IOException {
        return Files.lines(Paths.get(resource), StandardCharsets.UTF_8);
    }

    private static final class Item {
        private final long value;
        private final long weight;

        private Item(long value, long weight) {
            this.value = value;
            this.weight = weight;
        }
    }

    private static final class TestCase {
        private final String path;
        private final long capacity;
        private final long numOfItems;
        private final long value;

        private TestCase(String path, long capacity, long numOfItems, long value) {
            this.path = path;
            this.capacity = capacity;
            this.numOfItems = numOfItems;
            this.value = value;
        }
    }

}
