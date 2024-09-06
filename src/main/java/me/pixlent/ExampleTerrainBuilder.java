package me.pixlent;

import me.pixlent.noise.InterpolatedNoiseRegistry;
import net.minestom.server.coordinate.Point;

public class ExampleTerrainBuilder extends TerrainBuilder {
    ExampleTerrainBuilder(long seed) {
        super(seed);
    }

    @Override
    int getSurfaceHeight(int x, int z) {
        double continentalness = InterpolatedNoiseRegistry.CONTINENTALNESS.evaluateNoise(x, z);
        double erosion = InterpolatedNoiseRegistry.EROSION.evaluateNoise(x, z);

        return (int) ((continentalness + erosion) * 50);
    }

    @Override
    double getDensity(Point pos) {
        return 0;
    }
}
