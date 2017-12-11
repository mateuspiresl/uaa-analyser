package br.com.assistatecnologia.uaa_analyser;

/**
 * Time counter.
 */
public class Timer
{
    private long time = 0;
    private long elapsed = 0;
    private boolean running = false;

    /**
     * Starts the counter.
     * @return this instance.
     */
    public synchronized Timer start()
    {
        if (!this.running)
        {
            this.time = System.currentTimeMillis();

            if (this.elapsed > 0)
                this.time -= this.elapsed;

            this.running = true;
        }

        return this;
    }

    /**
     * Pause the counter.
     * @return this instance.
     */
    public synchronized Timer pause()
    {
        this.elapsed = System.currentTimeMillis() - this.time;
        this.running = false;

        return this;
    }

    /**
     * Reset the counter. This method do not pause.
     * To stop the counter and reset it, call {@link #pause()} before this method.
     * @return this instance.
     */
    public synchronized Timer reset()
    {
        this.elapsed = 0;

        if (this.running)
            this.time = System.currentTimeMillis();

        return this;
    }

    /**
     * Update the elapsed time.
     * @param elapsed the elapsed time.
     * @return this instance.
     */
    public synchronized Timer update(long elapsed)
    {
        if (this.running)
            this.time = System.currentTimeMillis() - elapsed;
        else
            this.elapsed = elapsed;

        return this;
    }

    /**
     * Updates the time by a factor.
     * The factor must be a value between 0.0 and 1.0, which will determine the percentage of update.
     * If the timer current time is 1000, the elapsed given is 500 and the factor is 0.2 (20%), the
     * resultant time will be 900, which is 20% of 500 plus 80% of 1000.
     * @param elapsed the new elapsed time.
     * @param factor the smooth factor.
     * @return self.
     */
    public synchronized Timer softUpdate(long elapsed, float factor)
    {
        if (this.running)
            this.time = System.currentTimeMillis()
                    - (long) (elapsed * factor + getElapsed() * (1 - factor));
        else
            this.elapsed = elapsed;

        return this;
    }

    /**
     * Returns the running state.
     * @return true if the counter is running; false, otherwise.
     */
    public synchronized boolean isRunning() {
        return this.running;
    }

    /**
     * Returns the elapsed time.
     * @return the elapsed time.
     */
    public synchronized long getElapsed() {
        return this.running ? System.currentTimeMillis() - this.time : this.elapsed;
    }

    /**
     * Checks if the counter has elapsed a given time.
     * @param elapsed the time to check.
     * @return true if the counter has elapsed; false, otherwise.
     */
    public synchronized boolean hasElapsed(long elapsed) {
        return getElapsed() >= elapsed;
    }

    /**
     * Time left to reach a given time.
     * @param time the time to check.
     * @return true if the counter reached the time; false, otherwise.
     */
    public synchronized long timeLeftTo(long time) {
        return time - getElapsed();
    }
}
