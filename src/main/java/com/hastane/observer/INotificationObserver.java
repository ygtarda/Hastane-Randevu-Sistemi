package com.hastane.observer;

public interface INotificationObserver {
    // Bildirim geldiğinde tetiklenecek metod
    void update(String mesaj);
}