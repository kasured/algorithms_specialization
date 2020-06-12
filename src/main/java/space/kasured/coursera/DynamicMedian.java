package space.kasured.coursera;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class DynamicMedian {
    public static void main(String[] args) throws URISyntaxException, IOException {

        final int MODULO = 10_000;
        final int[] moduloSum = new int[1];
        final MedianTracker tracker = new MedianTracker();

        final URI uri = DynamicMedian.class.getResource(args[0]).toURI();
        FileSystem fs = initFileSystem(uri);

        final Stream<Integer> integerStream = Files.lines(Paths.get(uri)).map(Integer::parseInt);

        integerStream.forEach(val -> {
            System.out.println("Adding val " + val);
            tracker.add(val);
            System.out.println("Median is " + tracker.getMedian());
            moduloSum[0] = moduloSum[0] + (tracker.getMedian() % MODULO);
        });

        System.out.println("Modulo sum is " + (moduloSum[0] % MODULO));

        fs.close();
    }

    private static FileSystem initFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.newFileSystem(uri, Collections.emptyMap());
        }catch(IllegalArgumentException e) {
            return FileSystems.getDefault();
        }
    }

    private static class MedianTracker {

        private final PriorityQueue<Integer> lower = new PriorityQueue<>(Comparator.reverseOrder());

        private final PriorityQueue<Integer> higher = new PriorityQueue<>();

        public void add(Integer value) {
            if(lower.isEmpty() || value <= lower.peek()) {
                lower.add(value);
            } else {
                higher.add(value);
            }
            rebalance();
        }
        public Integer getMedian() {
            return lower.peek();
        }

        private void rebalance() {

            while(lower.size() > 0 && lower.size() > higher.size() + 1) {
                higher.add(lower.poll());
            }

            while(higher.size() > 0 && lower.size() < higher.size()) {
                lower.add(higher.poll());
            }
            System.out.println("lower size is " + lower.size() + ", higher size is " + higher.size());
        }
    }
}
