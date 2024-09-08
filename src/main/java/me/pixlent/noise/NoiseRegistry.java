package me.pixlent.noise;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import me.pixlent.TerrainGenerator;

public enum NoiseRegistry {
    WEIRDNESS(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(0)
                    .build())
            .octavate(5, 0.5, 1.2, FractalFunction.FBM, false)
            .scale(0.005)
            .addModifier(new NoiseUtils.AbsClampNoiseModifier())
            .build()),
    DENSITY(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(0)
                    .build())
            .octavate(5, 0.7, 1.6, FractalFunction.FBM, true)
            .scale(0.006)
            .addModifier(new TerrainGenerator.AbsClampNoiseModifier())
            .build());

    final private JNoise noise;

    NoiseRegistry(JNoise noise) {
        this.noise = noise;
    }

    public double evaluateNoise(int x, int z) {
        return noise.evaluateNoise(x, z);
    }

    public double evaluateNoise(int x, int y, int z) {
        return noise.evaluateNoise(x, y, z);
    }
}