## 1. 概述

“我应该实现Runnable还是继承Thread类”？这是创建线程时一个很常见的问题。

在本文中，我们将看到哪种方法在实践中更有意义，以及为什么。

## 2. 使用Thread类

让我们首先定义一个继承Thread的SimpleThread类：

```java
class SimpleThread extends Thread {
    private static final Logger log = LoggerFactory.getLogger(SimpleThread.class);

    private final String message;

    SimpleThread(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        log.info(message);
    }
}
```

让我们看看如何运行这种类型的线程：

```java
public class RunnableVsThreadLiveTest {

    @Test
    public void givenAThread_whenRunIt_thenResult() throws Exception {
        Thread thread = new SimpleThread("SimpleThread executed using Thread");
        thread.start();
        thread.join();
    }
}
```

我们还可以使用ExecutorService来执行线程：

```java
public class RunnableVsThreadLiveTest {
    private static ExecutorService executorService;

    @BeforeAll
    public static void setup() {
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void givenAThread_whenSubmitToES_thenResult() throws Exception {
        executorService.submit(new SimpleThread("SimpleThread executed using ExecutorService")).get();
    }

    @AfterAll
    public static void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
```

在单独的线程中运行单个日志操作需要大量代码。

另外，**请注意SimpleThread现在不能继承任何其他类**，因为Java不支持多重继承。

## 3. 实现Runnable接口

现在，让我们创建一个实现java.lang.Runnable接口的简单任务：

```java
class SimpleRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SimpleRunnable.class);

    private final String message;

    SimpleRunnable(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        log.info(message);
    }
}
```

上面的SimpleRunnable只是一个任务，我们希望在单独的线程中运行。

我们可以使用多种方法来运行它；其中之一是使用Thread类：

```java
public class RunnableVsThreadLiveTest {
    @Test
    public void givenARunnable_whenRunIt_thenResult() throws Exception {
        Thread thread = new Thread(new SimpleRunnable("SimpleRunnable executed using Thread"));
        thread.start();
        thread.join();
    }
}
```

我们也可以使用ExecutorService：

```java
public class RunnableVsThreadLiveTest {
    @Test
    public void givenARunnable_whenSubmitToES_thenResult() throws Exception {
        executorService.submit(new SimpleRunnable("SimpleRunnable executed using ExecutorService")).get();
    }
}
```

可以在[这里]()阅读更多关于ExecutorService的内容。

因为我们现在是实现的一个接口，如果需要，我们可以自由地继承另一个父类。

从Java 8开始，任何只有单个抽象方法的接口都被视为函数接口，这使其成为有效的lambda表达式目标。

**我们可以使用lambda表达式重写上面的Runnable代码**：

```java
public class RunnableVsThreadLiveTest {
    @Test
    public void givenARunnableLambda_whenSubmitToES_thenResult() throws Exception {
        executorService.submit(() -> log.info("Lambda runnable executed!!!")).get();
    }
}
```

## 4. Runnable还是Thread？

简单地说，我们通常鼓励使用Runnable而不是Thread：

+ 在继承Thread类时，我们没有重写它的任何方法。相反，我们重写了Runnable的方法(Thread恰好实现了Runnable)。
  这显然违反了IS-A Thread原则。
+ 创建Runnable的实现并将其传递给Thread类利用了组合而不是继承，这更灵活。
+ 在继承Thread类之后，我们不能继承任何其他类。
+ 从Java 8开始，Runnable可以表示为lambda表达式。

## 5. 总结

在本文中，我们举例说明了实现Runnable通常是比继承Thread类更好的方法。