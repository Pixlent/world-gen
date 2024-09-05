package me.pixlent;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;

import java.util.Random;

public class TerrainBuilder {
    final Random seededRandom;

    public TerrainBuilder(long seed) {
        seededRandom = new Random(seed);
    }

    final JNoise noise = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    //.setSeed(seededRandom.nextLong())
                    .build())
            .octavate(5, 0.5, 2.2, FractalFunction.FBM, false)
            .scale(0.002)
            .addModifier(new TerrainGenerator.AbsClampNoiseModifier())
            .build();

    public int getSurfaceHeight(Point pos) {
        return 0;
    }
}
