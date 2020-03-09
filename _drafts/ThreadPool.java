public class Main {

    private static ThreadPoolExecutor poolTest = new ThreadPoolExecutor(3, 3, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(50));

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            poolTest.execute(new TestRunnable("test-----" + i));
        }
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
