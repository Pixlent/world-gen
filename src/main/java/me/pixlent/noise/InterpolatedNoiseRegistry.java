package me.pixlent.noise;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import me.pixlent.TerrainGenerator;
import me.pixlent.utils.SplineInterpolator;

public enum InterpolatedNoiseRegistry {
    CONTINENTALNESS(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(0)
                    .build())
            .octavate(5, 0.5, 2.2, FractalFunction.FBM, false)
            .scale(0.002)
            .addModifier(new TerrainGenerator.AbsClampNoiseModifier())
            .build(), SplineInterpolator.builder()
            .add(0.0, 0.0)
            .add(0.25, 0.1)
            .add(0.285, 0.38)
            .add(0.3, 0.444)
            .add(0.39, 0.666)
            .add(0.433, 0.777)
            .add(0.5, 0.888)
            .add(0.7, 0.97)
            .add(1, 1)
            .build()),
    EROSION(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(0)
                    .build())
            .octavate(5, 0.3, 3, FractalFunction.FBM, false)
            .scale(0.0015)
            .invert()
            .addModifier(new TerrainGenerator.AbsClampNoiseModifier())
            .build(), SplineInterpolator.builder()
            .add(0, 0)
            .add(0.1, 0.3)
            .add(0.5, 0.9)
            .add(0.6, 0.91)
            .add(0.62, 0.7)
            .add(0.7, 0.71)
            .add(0.72, 0.91)
            .add(1, 1)
            .build());

    final private JNoise noise;
    final private SplineInterpolator interpolator;

    InterpolatedNoiseRegistry(JNoise noise, SplineInterpolator interpolator) {
        this.noise = noise;
        this.interpolator = interpolator;
    }

    public double evaluateNoise(int x, int z) {
        return interpolator.interpolate(noise.evaluateNoise(x, z));
    }

    double evaluateNoise(int x, int y, int z) {
        return interpolator.interpolate(noise.evaluateNoise(x, y, z));
    }
}
