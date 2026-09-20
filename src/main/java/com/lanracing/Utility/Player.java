package com.lanracing.Utility;

public class Player {
    private final String id;
    private final String name;
    private volatile boolean ready;
    private volatile long finishTimeMillis;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public long getFinishTimeMillis() {
        return finishTimeMillis;
    }

    public void setFinishTimeMillis(long finishTimeMillis) {
        this.finishTimeMillis = finishTimeMillis;
    }
}
