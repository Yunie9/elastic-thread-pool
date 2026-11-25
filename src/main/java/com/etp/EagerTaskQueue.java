package com.etp;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * A blocking queue decorator that actively monitors queue capacity and triggers scaling.
 *
 * <p>Uses decorator pattern to enhance any BlockingQueue implementation. Monitors queue size
 * during task submission and triggers thread pool scaling when the configured threshold is reached.</p>
 *
 * @author Yunie
 * @version 1.0
 */
public class EagerTaskQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {

    private final BlockingQueue<Runnable> delegate;
    private volatile int scaleThreshold;
    private volatile ElasticThreadPoolExecutor executor;

    /**
     * Constructor that decorates any BlockingQueue.
     *
     * @param delegate       the queue to be decorated
     * @param scaleThreshold the scaling threshold, triggers scaling when queue size reaches this value (>= 0)
     */
    public EagerTaskQueue(BlockingQueue<Runnable> delegate, int scaleThreshold) {
        if (delegate == null) {
            throw new NullPointerException("delegate queue cannot be null");
        }
        if (scaleThreshold < 0) {
            throw new IllegalArgumentException("scaleThreshold must be >= 0");
        }
        this.delegate = delegate;
        this.scaleThreshold = scaleThreshold;
    }

    /**
     * Constructor that decorates a LinkedBlockingQueue.
     *
     * @param capacity the capacity of the queue (> 0)
     * @param scaleThreshold the scaling threshold, triggers scaling when queue size reaches this value (>= 0)
     */
    public EagerTaskQueue(int capacity, int scaleThreshold) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        if (scaleThreshold < 0) {
            throw new IllegalArgumentException("scaleThreshold must be >= 0");
        }
        if (scaleThreshold > capacity) {
            throw new IllegalArgumentException("scaleThreshold must be <= capacity");
        }
        this.delegate = new LinkedBlockingQueue<>(capacity);
        this.scaleThreshold = scaleThreshold;
    }

    /**
     * Sets the associated elastic thread pool executor.
     *
     * @param executor the elastic thread pool executor to associate with
     */
    protected void setExecutor(ElasticThreadPoolExecutor executor) {
        if (this.executor != null) {
            throw new IllegalStateException("Executor already set");
        }
        this.executor = executor;
    }

    @Override
    public boolean offer(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }

        if (executor == null) {
            // if executor is not set, degrade to normal offer
            return delegate.offer(task);
        }

        // check if it should scale
        if (shouldScale()) {
            // return false to make thread pool scale
            return false;
        }

        // normal offer
        return delegate.offer(task);
    }

    /**
     * Forces offer without scaling check, used internally when scaling is triggered.
     *
     * @param task the task to offer
     * @return true if the task was added to the queue
     */
    protected boolean forceOffer(Runnable task) {
        return delegate.offer(task);
    }

    @Override
    public boolean offer(Runnable task, long timeout, TimeUnit unit) throws InterruptedException {
        if (task == null) {
            throw new NullPointerException();
        }

        if (executor == null) {
            // if executor is not set, degrade to normal offer
            return delegate.offer(task, timeout, unit);
        }

        // check if it should scale
        if (shouldScale()) {
            return false;
        }

        return delegate.offer(task, timeout, unit);
    }

    /**
     * Forces offer with timeout without scaling check, used internally when scaling is triggered.
     *
     * @param task    the task to offer
     * @param timeout how long to wait before giving up
     * @param unit    a TimeUnit determining how to interpret the timeout parameter
     * @return true if the task was added to the queue
     * @throws InterruptedException if interrupted while waiting
     */
    protected boolean forceOffer(Runnable task, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.offer(task, timeout, unit);
    }

    @Override
    public Runnable poll() {
        return delegate.poll();
    }

    @Override
    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.poll(timeout, unit);
    }

    @Override
    public Runnable take() throws InterruptedException {
        return delegate.take();
    }

    @Override
    public Runnable peek() {
        return delegate.peek();
    }

    @Override
    public int drainTo(java.util.Collection<? super Runnable> c) {
        return delegate.drainTo(c);
    }

    @Override
    public int drainTo(java.util.Collection<? super Runnable> c, int maxElements) {
        return delegate.drainTo(c, maxElements);
    }

    @Override
    public void put(Runnable task) throws InterruptedException {
        delegate.put(task);
    }

    @Override
    public int remainingCapacity() {
        return delegate.remainingCapacity();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<Runnable> iterator() {
        return delegate.iterator();
    }

    /**
     * Checks if the thread pool should scale based on queue size and pool capacity.
     *
     * @return true if scaling should occur, false otherwise
     */
    private boolean shouldScale() {
        return executor.getPoolSize() < executor.getMaximumPoolSize()
                && delegate.size() >= scaleThreshold;
    }

    /**
     * Gets the scaling threshold.
     *
     * @return the queue size threshold that triggers scaling
     */
    public int getScaleThreshold() {
        return scaleThreshold;
    }

    /**
     * Sets the scaling threshold.
     *
     * @param scaleThreshold the queue size threshold that triggers scaling
     */
    public void setScaleThreshold(int scaleThreshold) {
        this.scaleThreshold = scaleThreshold;
    }
}
