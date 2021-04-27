public class FPSCounter extends Thread {
    private int counter = 0;
    private double fps = 0.0;

    public double getFPS() {
        return fps;
    }

    public void count_frame() {
        ++counter;
    }

    public void run() {
        while (true) {
            int bef_counter = counter;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("counter: " + bef_counter + " " + counter);
            fps = (counter - bef_counter) / 2.0;
            System.out.println(bef_counter);
        }
    }
}
