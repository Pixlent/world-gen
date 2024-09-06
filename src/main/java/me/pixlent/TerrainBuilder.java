package me.pixlent;

import net.minestom.server.coordinate.Point;

public abstract class TerrainBuilder {
    long seed;

    TerrainBuilder(long seed) {
        this.seed = seed;
    }

    abstract int getSurfaceHeight(int x, int z);

    abstract double getDensity(Point pos);
}
