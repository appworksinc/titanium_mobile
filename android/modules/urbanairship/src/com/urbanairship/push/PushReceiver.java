package com.urbanairship.push;

public interface PushReceiver {
    void onReceive(String message, String payload);
}
