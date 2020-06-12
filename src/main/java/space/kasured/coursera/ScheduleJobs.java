package space.kasured.coursera;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;


public class ScheduleJobs {

    private static final Path INPUT_FILE_PATH = Paths.get("src/main/resources/scheduleJobs.input");

    private static final int SAMPLE_INPUT_SIZE = 10000;

    private static Stream<String> getTestCaseStream() throws IOException {
        return Files.lines(INPUT_FILE_PATH, StandardCharsets.UTF_8);
    }

    @Test(dependsOnMethods = {"testReadSample"})
    public void testJobsSchedule() throws IOException {
        final Stream<String> inputStream = getTestCaseStream();
        final List<Job> jobsToSchedule = inputStream.skip(1).map(ScheduleJobs::mapStringToJob).collect(Collectors.toList());
        Assert.assertEquals(jobsToSchedule.size(), SAMPLE_INPUT_SIZE);

        final Result resultByDifference = minimalWeightedCompletionTime(jobsToSchedule, diffJobsComparator);
        System.out.println("Result for scheduling by difference is " + resultByDifference);
        Assert.assertEquals(resultByDifference.sumOfLengths, 510_289L);
        Assert.assertEquals(resultByDifference.weightedCompletionTime, 69_119_377_652L);

        final Result resultByRatio = minimalWeightedCompletionTime(jobsToSchedule, ratioJobsComparator);
        System.out.println("Result for scheduling by ratio is " + resultByRatio);
        Assert.assertEquals(resultByRatio.sumOfLengths, 510_289L);
        Assert.assertEquals(resultByRatio.weightedCompletionTime, 67_311_454_237L);

    }

    private static Job mapStringToJob(final String weightWithLength) {
        final String[] tokens = weightWithLength.split(" ");
        return Job.from(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
    }

    @Test
    public void testReadSample() throws IOException {
        final String header = Files.lines(INPUT_FILE_PATH, StandardCharsets.UTF_8)
                .limit(1).findFirst().orElseThrow(IllegalArgumentException::new);
        Assert.assertEquals(Integer.parseInt(header), SAMPLE_INPUT_SIZE);
    }


    private static final class Result {
        public long sumOfLengths;
        public long weightedCompletionTime;

        @Override
        public String toString() {
            return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                    .add("sumOfLengths=" + sumOfLengths)
                    .add("weightedCompletionTime=" + weightedCompletionTime)
                    .toString();
        }
    }

    public static Result minimalWeightedCompletionTime(final List<Job> jobsToSchedule, final Comparator<Job> scheduler) {
        final Result completionTime = jobsToSchedule.stream().sorted(scheduler)
                .reduce(new Result(), (timeR, nextJob) -> {
                    timeR.sumOfLengths = timeR.sumOfLengths + nextJob.length;
                    timeR.weightedCompletionTime = timeR.weightedCompletionTime + nextJob.weight * timeR.sumOfLengths;
                    //System.out.println("agg " + timeR);
                    return timeR;
                }, (r1, r2) -> {
                    //System.out.println("combining " + r1 + "and" + r2);
                    r1.sumOfLengths += r2.sumOfLengths;
                    r1.weightedCompletionTime = r1.weightedCompletionTime + r2.weightedCompletionTime;
                    //System.out.println("into " + r1);
                    return r1;
                });
        return completionTime;
    }

    @Test
    public void testDiff() {
        final Job job1 = Job.from(1, 7);    // -6 fourth
        final Job job2 = Job.from(10, 3);   // 7 third
        final Job job3 = Job.from(11, 3);   // 8 first
        final Job job4 = Job.from(14, 7);   // 7 second higher weight

        final List<Job> expected = Arrays.asList(job3, job4, job2, job1);

        final List<Job> scheduled = Arrays.asList(job1, job2, job3, job4);
        scheduled.sort(diffJobsComparator);

        Assert.assertEquals(scheduled, expected);

        System.out.println("Minimal weighted completion time is " + minimalWeightedCompletionTime(Arrays.asList(job1, job2, job3, job4), diffJobsComparator));

    }

    @Test
    public void testRatio() {
        final Job job1 = Job.from(1, 7);    // 1/7 fourth
        final Job job2 = Job.from(13, 7);   // 2 third
        final Job job3 = Job.from(11, 3);   // 11/3 first
        final Job job4 = Job.from(28, 14);   // 2 second higher weight

        final List<Job> expected = Arrays.asList(job3, job4, job2, job1);

        final List<Job> scheduled = Arrays.asList(job1, job2, job3, job4);
        scheduled.sort(ratioJobsComparator);

        Assert.assertEquals(scheduled, expected);

        System.out.println("Minimal weighted completion time is " + minimalWeightedCompletionTime(Arrays.asList(job1, job2, job3, job4), ratioJobsComparator));

    }

    @Test
    public void testScenario1() {
        final Job job1 = Job.from(8, 50);
        final Job job2 = Job.from(74, 59);
        final Job job3 = Job.from(31, 73);
        final Job job4 = Job.from(45, 79);
        final Job job5 = Job.from(24, 10);
        final Job job6 = Job.from(41, 66);
        final Job job7 = Job.from(93, 43);
        final Job job8 = Job.from(88, 4);
        final Job job9 = Job.from(28, 30);
        final Job job10 = Job.from(41, 13);
        final Job job11 = Job.from(4, 70);
        final Job job12 = Job.from(10, 58);

        final List<Job> inputList = Arrays.asList(
                job1, job2, job3, job4, job5, job6, job7, job8, job9, job10, job11, job12
        );

        final Result diffResult = minimalWeightedCompletionTime(inputList, diffJobsComparator);

        Assert.assertEquals(diffResult.weightedCompletionTime, 68615L);

        final Result ratioResult = minimalWeightedCompletionTime(inputList, ratioJobsComparator);

        Assert.assertEquals(ratioResult.weightedCompletionTime, 67247L);

    }

    private static final Comparator<Job> diffJobsComparator =
            Comparator.comparing((Job job) -> job.getWeight() - job.getLength()).thenComparing(Job::getWeight).reversed();

    private static final Comparator<Job> ratioJobsComparator =
            Comparator.comparing((Job job) -> valueOf(job.getWeight()).divide(valueOf(job.getLength()), 10, HALF_UP)).reversed();

    private static final class Job {
        private final int length;
        private final int weight;

        private Job(int weight, int length) {
            this.length = length;
            this.weight = weight;
        }

        public static Job from(int weight, int length) {
            return new Job(weight, length);
        }

        public int getLength() {
            return length;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Job.class.getSimpleName() + "[", "]")
                    .add("weight=" + weight)
                    .add("length=" + length)
                    .toString();
        }
    }
}
