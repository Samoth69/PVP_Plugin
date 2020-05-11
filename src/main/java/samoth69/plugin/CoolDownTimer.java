package samoth69.plugin;

public class CoolDownTimer {
    private final int[] revealTime;
    private int currentPos = 0;
    private boolean done = false;

    public CoolDownTimer(int[] revealTimeLong) {
        this.revealTime = revealTimeLong;
    }

    public boolean doReveal(long dif) {
        if (!done && revealTime[currentPos] >= dif) {
            currentPos++;
            if (currentPos >= revealTime.length)
                done = true;
            return true;
        } else {
            return false;
        }
    }

}
