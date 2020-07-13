package space.kasured.coursera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class LeaderUnionFindStructureSuite {

    private final Logger logger = LoggerFactory.getLogger(LeaderUnionFindStructureSuite.class);

    @Test
    public void testUnionFindStructure() {

        final List<DataElement> testData = new ArrayList<>();
        DataElement one = new DataElement(1, "one");
        testData.add(one);
        DataElement two = new DataElement(2, "two");
        testData.add(two);
        DataElement three = new DataElement(3, "three");
        testData.add(three);
        DataElement four = new DataElement(4, "four");
        testData.add(four);
        DataElement five = new DataElement(5, "five");
        testData.add(five);

        final LeaderUnionFindStructure<DataElement> unionFind = new LeaderUnionFindStructure<>(testData);
        logger.debug(unionFind.getGroups());

        Assert.assertEquals((int) unionFind.findGroupFor(one), 1);
        Assert.assertEquals((int) unionFind.findGroupFor(two), 2);
        Assert.assertEquals((int) unionFind.findGroupFor(three), 3);
        Assert.assertEquals((int) unionFind.findGroupFor(four), 4);
        Assert.assertEquals((int) unionFind.findGroupFor(five), 5);

        unionFind.union(four, five);
        logger.debug(unionFind.getGroups());
        Assert.assertEquals(unionFind.findGroupFor(four), unionFind.findGroupFor(five));

        unionFind.union(one, four);
        Assert.assertEquals(unionFind.findGroupFor(one), unionFind.findGroupFor(five));
        Assert.assertEquals(unionFind.findGroupFor(one), unionFind.findGroupFor(four));

        unionFind.union(one, two);
        final String afterOneAndTwoFused = unionFind.getGroups();
        unionFind.union(one, two);
        Assert.assertEquals(afterOneAndTwoFused, unionFind.getGroups());

        Assert.assertEquals(unionFind.numOfGroups(), 2);

    }

    private static final class DataElement {
        private final int id;
        private final String label;

        private DataElement(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataElement that = (DataElement) o;
            return id == that.id &&
                    Objects.equals(label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, label);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", DataElement.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .add("label='" + label + "'")
                    .toString();
        }
    }
}
