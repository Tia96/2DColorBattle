public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private boolean gameLoop;
    private double targetFpsTime = 1000 / 30.0;
    private long startTime;

    private GameManager() {}

    public static GameManager getInstance() {
        INSTANCE.gameLoop = true;
        return INSTANCE;
    }

    public void init() {

    }
//->ready "ID" ready "ID"? OK. OK.
    public void start() throws InterruptedException {
        int frames = 0;
        startTime = System.currentTimeMillis();
        while (INSTANCE.gameLoop) {
            long sT = System.currentTimeMillis();
            ++frames;

            long elapsedTime = System.currentTimeMillis() - sT;
            if(elapsedTime < targetFpsTime) Thread.sleep((long) (targetFpsTime - elapsedTime));

            System.out.println("fps: " + (double)frames / (System.currentTimeMillis() - startTime) * 1000);
        }
    }
}
