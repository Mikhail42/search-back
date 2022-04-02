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
            exec.shutdown();
        }
        try {
            while (!exec.awaitTermination(1, TimeUnit.SECONDS)) ;
        } catch (InterruptedException e) {
            logger.error("can't wait", e);
        }
    }
}
