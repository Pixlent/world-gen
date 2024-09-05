package me.pixlent;


import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;

public enum NoiseRegistry {
    CONTINENTALNESS(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().build())
            .scale(0.005)
            .octavate(5, 0.5, 1.2, FractalFunction.FBM, false)
            .build()),
    EROSION(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().build())
            .scale(0.06)
            .build());

    final private JNoise noise;

    NoiseRegistry(JNoise noise) {
        this.noise = noise;
    }

    double evaluateNoise(int x, int z) {
        return noise.evaluateNoise(x, z);
    }

    double evaluateNoise(int x, int y, int z) {
        return noise.evaluateNoise(x, y, z);
    }
}
