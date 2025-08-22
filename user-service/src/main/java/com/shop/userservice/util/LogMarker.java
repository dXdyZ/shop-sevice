package com.shop.userservice.util;

import lombok.Getter;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Getter
public enum LogMarker {

    AUDIT,
    APP_CALL,
    ERROR,
    INFRA_ERROR;

    private final Marker marker;

    LogMarker() {
        this.marker = MarkerFactory.getMarker(this.name());
    }

}
