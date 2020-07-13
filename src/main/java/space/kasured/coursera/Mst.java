package space.kasured.coursera;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

/**
 * Test suite to cover Minimal Spanning Tree problem
 */
public abstract class Mst extends GraphSuite {

    protected final long EXPECTED_MST_COST = -3_612_829L;

    /*private static final Path INPUT_FILE_PATH = Paths.get("src/main/resources/primsMst_02.input");

    static final int SAMPLE_NODES_SIZE = 8;
    private static final int SAMPLE_EDGES_SIZE = 14;
    private static final long EXPECTED_MST_COST = 15L;*/

    /*private static final Path INPUT_FILE_PATH = Paths.get("src/main/resources/primsMst_01.input");

    static final int SAMPLE_NODES_SIZE = 4;
    private static final int SAMPLE_EDGES_SIZE = 5;
    private static final long EXPECTED_MST_COST = 7L;*/

    private final Function<GraphSuite.Graph, Long> mstCalculator;

    protected Mst(Function<GraphSuite.Graph, Long> mstCalculator) throws IOException {
        super(Paths.get("src/main/resources/primsMst.input"), 500, 2184);
        this.mstCalculator = mstCalculator;
    }

    @Test(dependsOnMethods = {"testGraphCreation", "testReadSample"})
    public void testMstCost() throws IOException {
        final GraphSuite.Graph graph = GraphSuite.Graph.fromSource(INPUT_FILE_PATH);
        final Long mstCost = mstCalculator.apply(graph);

        Assert.assertEquals(EXPECTED_MST_COST, (long) mstCost, "Mst cost failed");
    }

}
