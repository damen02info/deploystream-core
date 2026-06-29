package com.mdz.deploystream.events;

import com.mdz.deploystream.entities.AppConfig;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppConfigUpdateEvent extends ApplicationEvent {
    private final AppConfig payload;

    public AppConfigUpdateEvent(Object source, AppConfig payload) {
        super(source);
        this.payload = payload;
    }
}