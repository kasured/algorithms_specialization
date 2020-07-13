package space.kasured.coursera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class LeaderUnionFindStructure<T> implements UnionFindStructure<Integer, T> {

    private static final Logger logger = LoggerFactory.getLogger(LeaderUnionFindStructure.class);

    private final Map<Integer, List<Element<T>>> groups = new HashMap<>();

    private final Map<T, Element<T>> fastLookup = new HashMap<>();

    public LeaderUnionFindStructure(final List<T> collection) {
        IntStream.rangeClosed(1, collection.size()).forEach(idx -> {
            final Element<T> elem = new Element<>(idx, collection.get(idx - 1));
            fastLookup.put(collection.get(idx - 1), elem);

            final List<Element<T>> initGroupList = new ArrayList<>();
            initGroupList.add(elem);
            groups.put(idx, initGroupList);
        });
    }

    public String getGroups() {
        return groups.toString();
    }

    @Override
    public Integer findGroupFor(T tElement) {
        final Element<T> mapping = fastLookup.get(tElement);
        logger.debug("found group {} for element {}", mapping.leaderId, tElement);
        return mapping.leaderId;
    }

    @Override
    public void union(T tElement, T otherElement) {
        // check what is the bigger group and fuse smaller into the bigger
        final Element<T> wrapper = fastLookup.get(tElement);
        final Element<T> wrapperOther = fastLookup.get(otherElement);

        if(wrapper.leaderId.equals(wrapperOther.leaderId)) {
            logger.debug("Element {} and {} already in the same group", wrapper, wrapperOther);
            return;
        }

        final int elementGroupSize = groups.get(wrapper.leaderId).size();
        final int otherElementGroupSize = groups.get(wrapperOther.leaderId).size();

        // start fusing
        if(elementGroupSize <= otherElementGroupSize) {
            logger.debug("fusing group {} with size {} into group {} with size {}",
                    wrapper.leaderId, elementGroupSize, wrapperOther.leaderId, otherElementGroupSize
            );
            fuseInto(wrapper.leaderId, wrapperOther.leaderId);
        } else {
            logger.debug("fusing group {} with size {} into group {} with size {}",
                    wrapperOther.leaderId, otherElementGroupSize, wrapper.leaderId, elementGroupSize
            );
            fuseInto(wrapperOther.leaderId, wrapper.leaderId);
        }
    }

    @Override
    public int numOfGroups() {
        return groups.keySet().size();
    }

    private void fuseInto(Integer sourceGroup, Integer targetGroup) {
        final List<Element<T>> sourceElements = groups.remove(sourceGroup);
        // update the leaders to correspond to the target's group leader id
        sourceElements.forEach(node -> node.leaderId = targetGroup);
        // finally add those elements to the target group
        groups.get(targetGroup).addAll(sourceElements);
    }

    private static class Element<T> {
        private Integer leaderId;
        private final T data;

        public Element(Integer leaderId, T data) {
            this.leaderId = leaderId;
            this.data = data;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Element.class.getSimpleName() + "[", "]")
                    .add("leaderId=" + leaderId)
                    .add("data=" + data)
                    .toString();
        }
    }
}
