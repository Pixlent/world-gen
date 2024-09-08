package me.pixlent;

import me.pixlent.noise.InterpolatedNoiseRegistry;
import me.pixlent.noise.NoiseRegistry;

public class ExampleTerrainBuilder extends TerrainBuilder {
    // 0: no squashing the terrain, 1: basically the surface height
    private final double squashingFactor = 1.5;

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
    double getDensity(int x, int y, int z) {
        double density = NoiseRegistry.DENSITY.evaluateNoise(x, y, z);
        int surfaceHeight = getSurfaceHeight(x, z);
        // Clamps the height from -1 to 1
        //double relativeHeight = (double) (y - surfaceHeight) / surfaceHeight;

        density *= (double) surfaceHeight / y;

        //density -= relativeHeight;

        return density;
    }
}
