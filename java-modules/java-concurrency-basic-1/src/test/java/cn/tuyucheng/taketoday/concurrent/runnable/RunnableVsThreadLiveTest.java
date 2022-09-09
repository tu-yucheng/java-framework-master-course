package cn.tuyucheng.taketoday.concurrent.runnable;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RunnableVsThreadLiveTest {
    private static final Logger log = LoggerFactory.getLogger(RunnableVsThreadLiveTest.class);

    private static ExecutorService executorService;

    @BeforeAll
    public static void setup() {
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void givenARunnable_whenRunIt_thenResult() throws Exception {
        Thread thread = new Thread(new SimpleRunnable("SimpleRunnable executed using Thread"));
        thread.start();
        thread.join();
    }

    @Test
    public void givenARunnable_whenSubmitToES_thenResult() throws Exception {
        executorService.submit(new SimpleRunnable("SimpleRunnable executed using ExecutorService")).get();
    }

    @Test
    public void givenARunnableLambda_whenSubmitToES_thenResult() throws Exception {
        executorService.submit(() -> log.info("Lambda runnable executed!!!")).get();
    }

    @Test
    public void givenAThread_whenRunIt_thenResult() throws Exception {
        Thread thread = new SimpleThread("SimpleThread executed using Thread");
        thread.start();
        thread.join();
    }

    @Test
    public void givenAThread_whenSubmitToES_thenResult() throws Exception {
        executorService.submit(new SimpleThread("SimpleThread executed using ExecutorService")).get();
    }

    @Test
    public void givenACallable_whenSubmitToES_thenResult() throws Exception {
        Future<Integer> future = executorService.submit(new SimpleCallable());

        log.info("Result from callable: {}", future.get());
    }

    @Test
    public void givenACallableAsLambda_whenSubmitToES_thenResult() throws Exception {
        Future<Integer> future = executorService.submit(() -> RandomUtils.nextInt(0, 100));

        log.info("Result from callable: {}", future.get());
    }

    @AfterAll
    public static void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}