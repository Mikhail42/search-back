package org.ionkin.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelFor {

    private static final Logger logger = LoggerFactory.getLogger(ParallelFor.class);

    public static void par(SupplierExc fun, int from, int until) {
        ExecutorService exec = Executors.newFixedThreadPool(Util.threadPoolSize);
        try {
            for (int i = from; i < until; i++) {
                final int i0 = i;
                exec.submit(() -> {
                    try {
                        fun.run(i0);
                    } catch (Exception e) {
                        logger.error("" + i0, e);
                    }
                });
            }
        } finally {
            shutdown(exec);
        }
    }

    // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
    private static void shutdown(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    logger.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
