package com.hastane.observer;

public class ConsoleLoggerObserver implements INotificationObserver {
    @Override
    public void update(String mesaj) {
        System.out.println("🔔 [BİLDİRİM SİSTEMİ]: " + mesaj);
    }
}