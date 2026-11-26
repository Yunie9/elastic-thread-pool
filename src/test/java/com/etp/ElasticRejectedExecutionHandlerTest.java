package com.etp;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ElasticRejectedExecutionHandlerTest {
    @Test
    public void rejectsSubmissionAfterTermination() throws Exception {
        ElasticThreadPoolExecutor pool = pool(new ArrayBlockingQueue<>(2));
        try {
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

            assertThrows(RejectedExecutionException.class, () -> pool.submit(() -> 42));
            assertTrue(pool.getQueue().isEmpty());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void removesTaskWhenShutdownRacesWithFallbackOffer() throws Exception {
        CountDownLatch offering = new CountDownLatch(1);
        CountDownLatch resumeOffer = new CountDownLatch(1);
        BlockingQueue<Runnable> delegate = new ArrayBlockingQueue<Runnable>(2) {
            @Override
            public boolean offer(Runnable task) {
                offering.countDown();
                try {
                    assertTrue(resumeOffer.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
                return super.offer(task);
            }
        };
        ElasticThreadPoolExecutor pool = pool(delegate);
        ExecutorService submitter = Executors.newSingleThreadExecutor();
        try {
            // Invoke the fallback directly so shutdown always lands inside the queue offer.
            Future<?> submission = submitter.submit(() ->
                    pool.getRejectedExecutionHandler().rejectedExecution(() -> {}, pool));
            assertTrue(offering.await(5, TimeUnit.SECONDS));
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
            resumeOffer.countDown();

            ExecutionException rejection = assertThrows(ExecutionException.class,
                    () -> submission.get(5, TimeUnit.SECONDS));
            assertTrue(rejection.getCause() instanceof RejectedExecutionException);
            assertTrue(delegate.isEmpty());
        } finally {
            resumeOffer.countDown();
            submitter.shutdownNow();
            pool.shutdownNow();
        }
    }

    private static ElasticThreadPoolExecutor pool(BlockingQueue<Runnable> queue) {
        return new ElasticThreadPoolExecutor(1, 2, 30, TimeUnit.SECONDS, 1, queue);
    }
}
