package com.hastane.state;

public class StateFactory {
    public static IAppointmentState getStateByString(String durum) {
        if (durum == null) return new PendingState();

        switch (durum.toUpperCase()) {
            case "ONAYLANDI":
                return new ConfirmedState();
            case "TAMAMLANDI":
                return new CompletedState();
            case "İPTAL EDİLDİ":
                return new CancelledState();
            case "GELMEDİ":
                return new MissedState();
            case "BEKLEMEDE":
            default:
                return new PendingState();
        }
    }
}