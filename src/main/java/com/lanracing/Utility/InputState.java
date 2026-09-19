package com.lanracing.Utility;

public class InputState {
    public volatile boolean accelerate;
    public volatile boolean brake;
    public volatile boolean turnLeft;
    public volatile boolean turnRight;
    public volatile boolean nitro;
    public volatile boolean reset;

    public void clear() {
        accelerate = false;
        brake = false;
        turnLeft = false;
        turnRight = false;
        nitro = false;
        reset = false;
    }
}
