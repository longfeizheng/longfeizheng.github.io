public class Main {

    private static ThreadPoolExecutor poolTest = new ThreadPoolExecutor(2, 3, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(50));

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            poolTest.execute(new TestRunnable("test-----" + i));
        }
//        poolTest.shutdown();

        for (int i = 10; i < 20; i++) {
            poolTest.execute(new TestRunnable("test-----" + i));
        }
        System.out.println("测试线程池");

        long begin = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - begin > 2000) {
                System.out.println("中断循环");
                break;
            }
        }

        System.out.println("测试线程池和中断循环");
    }
}

public class TestRunnable implements Runnable {


    private String threadName;

    public TestRunnable(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(threadName);
        System.out.println(Thread.currentThread().getName()+"---"+Thread.currentThread().getId());
    }
}
