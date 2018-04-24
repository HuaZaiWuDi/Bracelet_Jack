/*
 * Copyright (C) 2015 The Telink Bluetooth Light Project
 *
 */
package com.android.rcc.util;

public interface EventListener<T> {
    void performed(Event<T> event);
}