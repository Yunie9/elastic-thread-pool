# 弹性线程池 (Elastic Thread Pool)

[![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)](https://java.oracle.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-orange.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

一个高性能的弹性线程池实现，支持基于队列大小的弹性扩容，为突发流量场景提供更好的响应性能。

## 🚀 核心特性

- **兼顾弹性扩展与任务缓冲**：队列大小达到阈值 `scaleThreshold` 即创建非核心线程，而不是队列满才进行扩容
- **API 兼容**：
  - 兼容任何 `BlockingQueue`实现
  - 兼容任何 `RejectedExecutionHandler` 实现
  - 可直接替换标准 `ThreadPoolExecutor`

## 📋 目录

- [使用示例](#-使用示例)
- [适用场景](#-适用场景)
- [许可证](#-许可证)

## 📖 使用示例

### 不同队列类型

```java
// LinkedBlockingQueue（无界队列）
BlockingQueue<Runnable> unboundedQueue = new LinkedBlockingQueue<>();
// 容量达到100时扩容
ElasticThreadPoolExecutor unboundedExecutor = new ElasticThreadPoolExecutor(
    2, 20, 60L, TimeUnit.SECONDS, 100, unboundedQueue, factory, handler);

int capacity = 50; 
// ArrayBlockingQueue（有界队列）
BlockingQueue<Runnable> boundedQueue = new ArrayBlockingQueue<>(capacity);
// 容量达到一半时扩容
ElasticThreadPoolExecutor boundedExecutor = new ElasticThreadPoolExecutor(
    2, 20, 60L, TimeUnit.SECONDS, capacity / 2, boundedQueue, factory, handler);
```

### 动态阈值调整

```java
ElasticThreadPoolExecutor executor = new ElasticThreadPoolExecutor(
    2, 20, 60L, TimeUnit.SECONDS, 10, queue, factory, handler);

// 运行时调整阈值
executor.setScaleThreshold(5);  // 更激进的扩容
// ... 运行负载 ...
executor.setScaleThreshold(20); // 更保守的扩容
```


## 📚 适用场景

### 典型应用场景

#### 场景一：Web 服务请求处理

```
特点：
- 请求到达具有突发性
- 响应时间要求严格（P99 < 100ms）
- 任务执行时间相对稳定

优势：
- 突发流量时主动扩容，减少请求排队时间
- 低峰期自动缩容，节省资源
```

#### 场景二：消息队列消费者

```
特点：
- 消息批量到达
- 需要快速消费避免积压
- 消费速度影响系统整体性能

优势：
- 消息积压时主动扩容，加速消费
- 避免消息在队列中长时间等待
```

#### 场景三：异步任务处理

```
特点：
- 任务提交频率波动大
- 任务执行时间差异较大
- 需要平衡延迟和资源使用

优势：
- 根据队列状态动态调整处理能力
- 在延迟和资源之间找到平衡点
```

### 不适用场景

1. **严格资源限制场景**

    - 如果系统对线程数有固定数量要求
    - 标准线程池的"保守"策略更合适
2. **任务执行时间极短**

    - 如果任务执行时间 < 1ms
    - 线程动态创建与销毁开销可能超过收益
3. **CPU 密集型且线程数已优化**

    - 如果线程数已精确匹配 CPU 核心数
    - 额外线程可能带来上下文切换开销
    

### 选择建议

**使用标准线程池，如果：**

- 任务到达模式相对稳定
- 对资源使用有严格限制
- 需要最大程度的稳定性和可预测性
- 线程池配置已经过充分调优

**使用弹性线程池，如果：**

- 存在突发流量场景
- 对任务处理延迟敏感
- 任务执行时间差异较大
- 需要更积极的资源利用策略
- 愿意用额外的复杂度换取更好的响应性


## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

---

**弹性线程池** - 为线程池扩容注入智能！🚀
