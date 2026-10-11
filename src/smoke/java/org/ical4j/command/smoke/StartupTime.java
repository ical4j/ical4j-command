package org.ical4j.command.smoke;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Measures {@code ict --help} wall-clock time over several runs and fails if the median exceeds
 * a limit. Usage: {@code StartupTime <ict executable> [runs=10] [limit-ms=100]}. One unmeasured
 * warm-up run comes first so the file cache does not count against the binary.
 */
public final class StartupTime {

    private StartupTime() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            System.err.println("Usage: StartupTime <ict executable> [runs] [limit-ms]");
            System.exit(2);
        }
        String binary = args[0];
        int runs = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        long limit = args.length > 2 ? Long.parseLong(args[2]) : 100;

        time(binary);
        long[] millis = new long[runs];
        for (int i = 0; i < runs; i++) {
            millis[i] = time(binary);
        }
        long[] sorted = millis.clone();
        Arrays.sort(sorted);
        double median = runs % 2 == 1 ? sorted[runs / 2] : (sorted[runs / 2 - 1] + sorted[runs / 2]) / 2.0;

        System.out.printf("ict --help over %d runs: median %.1f ms, min %d ms, max %d ms (limit %d ms)%n",
                runs, median, sorted[0], sorted[runs - 1], limit);
        if (median > limit) {
            System.out.println("Startup time FAILED: median exceeds " + limit + " ms");
            System.exit(1);
        }
    }

    private static long time(String binary) throws IOException, InterruptedException {
        long start = System.nanoTime();
        Process process = new ProcessBuilder(binary, "--help")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException(binary + " --help failed");
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }
}
