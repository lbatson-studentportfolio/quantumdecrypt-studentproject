package cmis202honors;

// Comparing computation times between classical and quantum implementations
// only really becomes influential when dealing with large moduli.

public class Timer {

    // in milliseconds
    private float lastRecorded;
    private long startTime;
    private long stopTime;
    
    public Timer() {
        lastRecorded = 0;
        startTime = 0;
        stopTime = 0;
    }

    // Resets recorded times
    public void start() {
        lastRecorded = 0;
        startTime = System.nanoTime();
        stopTime = startTime;
    }

    public void stop() {
        stopTime = System.nanoTime();
        lastRecorded = ((float) (stopTime - startTime) / 1000000);
    }

    public float getRecordedTime() {
        return lastRecorded;
    }

}
