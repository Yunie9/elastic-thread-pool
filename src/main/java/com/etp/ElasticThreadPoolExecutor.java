package com.etp;

import java.util.concurrent.*;

/**
 * A thread pool executor that supports elastic scaling based on queue size.
 *
 * <p>This executor automatically scales the number of threads based on the queue size threshold.
 * When the queue size reaches the configured scale threshold, new threads are created to handle
 * the workload, providing better responsiveness for burst traffic scenarios.</p>
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic scaling based on queue size threshold</li>
 *   <li>Compatible with any BlockingQueue implementation through decoration</li>
 *   <li>Thread-safe scaling with proper synchronization</li>
 *   <li>Configurable scaling threshold and final rejection handler</li>
 * </ul>
 *
 * @author Yunie
 * @version 1.0
 */
public class ElasticThreadPoolExecutor extends ThreadPoolExecutor {
    private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();
    private final EagerTaskQueue eagerTaskQueue;

    /**
     * Creates an ElasticThreadPoolExecutor with default thread factory and abort policy.
     */
    public ElasticThreadPoolExecutor(int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     int scaleThreshold,
                                     BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, scaleThreshold,
                workQueue, Executors.defaultThreadFactory(), defaultHandler);
    }

    /**
     * Creates an ElasticThreadPoolExecutor with default abort policy.
     */
    public ElasticThreadPoolExecutor(int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     int scaleThreshold,
                                     BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, scaleThreshold,
                workQueue, threadFactory, defaultHandler);
    }

    /**
     * Creates an ElasticThreadPoolExecutor with default thread factory.
     */
    public ElasticThreadPoolExecutor(int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     int scaleThreshold,
                                     BlockingQueue<Runnable> workQueue,
                                     RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, scaleThreshold,
                workQueue, Executors.defaultThreadFactory(), handler);
    }

    /**
     * Creates an ElasticThreadPoolExecutor with all parameters specified.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param scaleThreshold  the queue size threshold that triggers scaling (>= 0)
     * @param workQueue       the queue to use for holding tasks before they are executed
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param handler         the handler to use when execution is blocked because thread bounds
     *                        and queue capacities are reached
     */
    public ElasticThreadPoolExecutor(int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     int scaleThreshold,
                                     BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory,
                                     RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                workQueue = newEagerTaskQueue(workQueue, scaleThreshold),
                threadFactory,
                new ElasticRejectedExecutionHandler(handler));
        this.eagerTaskQueue = (EagerTaskQueue) workQueue;
        // bind EagerTaskQueue
        this.eagerTaskQueue.setExecutor(this);
    }

    private static EagerTaskQueue newEagerTaskQueue(BlockingQueue<Runnable> workQueue, int scaleThreshold) {
        if (workQueue instanceof EagerTaskQueue) {
            EagerTaskQueue eagerTaskQueue = (EagerTaskQueue) workQueue;
            eagerTaskQueue.setScaleThreshold(scaleThreshold);
            return eagerTaskQueue;
        }
        return new EagerTaskQueue(workQueue, scaleThreshold);
    }

    /**
     * Gets the scaling threshold.
     *
     * @return the queue size threshold that triggers scaling
     */
    public int getScaleThreshold() {
        return this.eagerTaskQueue.getScaleThreshold();
    }

    /**
     * Sets the scaling threshold.
     *
     * @param scaleThreshold the queue size threshold that triggers scaling
     */
    public void setScaleThreshold(int scaleThreshold) {
        this.eagerTaskQueue.setScaleThreshold(scaleThreshold);
    }
}
