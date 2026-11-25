# Elastic Thread Pool

[![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)](https://java.oracle.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-orange.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

[中文版](README_zh.md)

A high-performance elastic thread pool implementation that supports queue size-based scaling, providing better responsiveness for burst traffic scenarios.

## 🚀 Core Features

- **Balanced Elastic Scaling and Task Buffering**: Creates non-core threads when queue size reaches `scaleThreshold`, rather than waiting for queue to be full
- **API Compatibility**:
  - Compatible with any `BlockingQueue` implementation
  - Compatible with any `RejectedExecutionHandler` implementation
  - Can directly replace standard `ThreadPoolExecutor`

## 📋 Table of Contents

- [Usage Examples](#-usage-examples)
- [Applicable Scenarios](#-applicable-scenarios)
- [License](#-license)

## 📖 Usage Examples

### Different Queue Types

```java
// LinkedBlockingQueue (unbounded queue)
BlockingQueue<Runnable> unboundedQueue = new LinkedBlockingQueue<>();
// Scale when capacity reaches 100
ElasticThreadPoolExecutor unboundedExecutor = new ElasticThreadPoolExecutor(
    2, 20, 60L, TimeUnit.SECONDS, 100, unboundedQueue, factory, handler);

int capacity = 50;
// ArrayBlockingQueue (bounded queue)
BlockingQueue<Runnable> boundedQueue = new ArrayBlockingQueue<>(capacity);
// Scale when capacity reaches half
ElasticThreadPoolExecutor boundedExecutor = new ElasticThreadPoolExecutor(
    2, 20, 60L, TimeUnit.SECONDS, capacity / 2, boundedQueue, factory, handler);
```

### Dynamic Threshold Adjustment

```java
ElasticThreadPoolExecutor executor = new ElasticThreadPoolExecutor(
    2, 20, 60L, TimeUnit.SECONDS, 10, queue, factory, handler);

// Adjust threshold at runtime
executor.setScaleThreshold(5);  // More aggressive scaling
// ... run workload ...
executor.setScaleThreshold(20); // More conservative scaling
```


## 📚 Applicable Scenarios

### Typical Application Scenarios

#### Scenario 1: Web Service Request Processing

```
Characteristics:
- Request arrival is bursty
- Strict response time requirements (P99 < 100ms)
- Relatively stable task execution time

Advantages:
- Proactively scales during burst traffic, reduces request queuing time
- Automatically scales down during low periods, saves resources
```

#### Scenario 2: Message Queue Consumer

```
Characteristics:
- Messages arrive in batches
- Need to consume quickly to avoid backlog
- Consumption speed affects overall system performance

Advantages:
- Proactively scales when messages back up, accelerates consumption
- Avoids messages waiting too long in queue
```

#### Scenario 3: Asynchronous Task Processing

```
Characteristics:
- Task submission frequency fluctuates greatly
- Task execution time varies significantly
- Need to balance latency and resource usage

Advantages:
- Dynamically adjusts processing capacity based on queue status
- Finds balance between latency and resources
```

### Inapplicable Scenarios

1. **Strict Resource Limitation Scenarios**

    - If the system has fixed thread count requirements
    - Standard thread pool's "conservative" strategy is more appropriate
2. **Very Short Task Execution Time**

    - If task execution time < 1ms
    - Overhead of dynamic thread creation/destruction may exceed benefits
3. **CPU Intensive with Optimized Thread Count**

    - If thread count is already precisely matched to CPU cores
    - Extra threads may bring context switching overhead


### Selection Guidelines

**Use Standard Thread Pool, if:**

- Task arrival pattern is relatively stable
- Strict limits on resource usage
- Need maximum stability and predictability
- Thread pool configuration has been fully tuned

**Use Elastic Thread Pool, if:**

- Burst traffic scenarios exist
- Sensitive to task processing latency
- Task execution time varies significantly
- Need more aggressive resource utilization strategy
- Willing to trade additional complexity for better responsiveness


## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

**Elastic Thread Pool** - Injecting intelligence into thread pool scaling! 🚀
