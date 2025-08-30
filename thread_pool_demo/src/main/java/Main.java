public class Main {

    public static void main(String[] args) throws InterruptedException {
        int[] count = new int[]{10000};

        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    try {
                        Thread.sleep(10);
                        count[0]--;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }

        Thread.sleep(2000);
        System.out.println("count[0] = " + count[0]);
    }

}
