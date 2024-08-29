package me.pixlent.utils;

public record Range(double min, double max) {

    public double size() {
        return Math.abs(max - min);
    }
}
