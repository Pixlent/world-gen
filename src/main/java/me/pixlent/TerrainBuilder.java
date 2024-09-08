package me.pixlent;

public abstract class TerrainBuilder {
    long seed;

    TerrainBuilder(long seed) {
        this.seed = seed;
    }

    abstract int getSurfaceHeight(int x, int z);

    abstract double getDensity(int x, int y, int z);
}
