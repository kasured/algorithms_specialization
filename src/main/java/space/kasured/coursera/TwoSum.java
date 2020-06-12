package space.kasured.coursera;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class TwoSum {
    public static void main(String[] args) throws IOException {

        final int left = -10_000;
        final int right = 10_000;

        final long count = solution(Files.lines(Paths.get("src", "main", "resources", "2sum.input"))
                .map(Long::parseLong).toArray(Long[]::new), left, right);

        /*long count = solution(new Long[]{-2L, 0L, 0L, 4L }, 0, 4);*/

        System.out.println("count is " + count);
    }

    private static long solution(Long[] input, int left, int right) {
        final Set<Long> index = new HashSet<>();
        final Set<Long> unique = new HashSet<>();

        long count = 0;

        for (int i = 0; i < input.length; i++) {
            long val = input[i];
            long leftAdj = left - val;
            long rightAdj = right - val;
            for (long j = leftAdj; j <= rightAdj; j++) {
                if (index.contains(j) && j != val && !unique.contains(val + j)) {
                    count = count + 1;
                    System.out.println(String.format("For val %s at %s found complement %s for range [%s,%s], set size %s, sum %s",
                            val, i, j, leftAdj, rightAdj, index.size(), val + j)
                    );
                    unique.add(val + j);
                }
            }
            index.add(val);
        }

        return count;

    }
}
