package com.mdz.deploystream.events;

import org.springframework.context.ApplicationEvent;

public class CountdownTickEvent extends ApplicationEvent {
    private final int remainingSeconds;

    public CountdownTickEvent(Object source, int remainingSeconds) {
        super(source);
        this.remainingSeconds = remainingSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}
