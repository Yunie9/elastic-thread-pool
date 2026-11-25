package com.etp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedExecutionHandler for {@link EagerTaskQueue}.
 * <p>
 * This handler will try to add the task to the {@code EagerTaskQueue},
 * if it fails, it will delegate to the final handler.
 *
 * @author Yunie
 * @version 1.0
 */
public class ElasticRejectedExecutionHandler implements RejectedExecutionHandler {

    private final RejectedExecutionHandler finalReject;

    /**
     * Constructor with the final rejection handler.
     *
     * @param finalReject the final handler to delegate to if force offer fails
     */
    public ElasticRejectedExecutionHandler(RejectedExecutionHandler finalReject) {
        this.finalReject = finalReject;
    }

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor pool) {
        BlockingQueue<Runnable> queue = pool.getQueue();
        if (queue instanceof EagerTaskQueue) {
            EagerTaskQueue eagerQueue = (EagerTaskQueue) queue;
            if (eagerQueue.forceOffer(task)) {
                return;
            }
        }
        finalReject.rejectedExecution(task, pool);
    }
}
