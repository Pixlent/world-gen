package me.pixlent;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import me.pixlent.utils.SplineInterpolator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TerrainGenerator implements Generator {
    final long seed = 0;
    final Random seededSeedGenerator = new Random(seed);
    final SplineInterpolator continentalInterpolator = SplineInterpolator.builder()
            .add(-1, 0)
            .add(-0.3, 30)
            .add(0.2, 61)
            .add(0.5, 66)
            .add(0.7, 90)
            .add(1, 120)
            .build();
    final JNoise continentalness = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(seededSeedGenerator.nextLong())
                    .build())
            .octavate(4, 0.4, 2.2, FractalFunction.FBM, false)
            .scale(0.003)
            .build();
    final SplineInterpolator erosionInterpolator = SplineInterpolator.builder()
            .add(-1, 0)
            .add(-0.2, 50)
            .add(0.55, 25)
            .add(0.6, 40)
            .add(0.8, 45)
            .add(0.85, 70)
            .add(1, 100)
            .build();
    final JNoise erosion = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(seededSeedGenerator.nextLong())
                    .build())
            .octavate(5, 0.3, 3, FractalFunction.FBM, false)
            .scale(0.002)
            .invert()
            .build();
    final JNoise detail = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(seededSeedGenerator.nextLong())
                    .build())
            .octavate(8, 0.5, 1.6, FractalFunction.FBM, false)
            .scale(0.03)
            .build();
    final JNoise random = JNoise.newBuilder()
            .white(seededSeedGenerator.nextLong())
            .build();
    final JNoise grass = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(seededSeedGenerator.nextLong()).build())
            .octavate(2, .3, 1.4, FractalFunction.FBM, false)
            .scale(.015)
            .build();
    final JNoise flowers = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(seededSeedGenerator.nextLong()).build())
            .octavate(2, .3, 1.4, FractalFunction.FBM, false)
            .scale(.04)
            .build();

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();

        for (int x = 0; x < unit.size().x(); x++) {
            for (int z = 0; z < unit.size().z(); z++) {
                Point bottom = start.add(x, 0, z);

                int height = getHeight(bottom);

                unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.STONE);
                unit.modifier().fill(bottom.withY(height), bottom.add(1, 0, 1).withY(64), Block.WATER);

                if ((height) > 65) {
                unit.modifier().fill(bottom.withY( height - 3), bottom.add(1, 0, 1).withY(height - 1), Block.DIRT);
                unit.modifier().fill(bottom.withY(height - 1), bottom.add(1, 0, 1).withY(height), Block.GRASS_BLOCK);

                placeDecorations(unit, height, bottom);

                } else {
                    unit.modifier().fill(bottom.withY(height - 3), bottom.add(1, 0, 1).withY(height), Block.SAND);
                }
            }
        }
    }

    private int getHeight(Point pos) {
        double continentalHeight = continentalInterpolator.interpolate(continentalness.evaluateNoise(pos.x(), pos.z()));
        double erosionHeight = erosionInterpolator.interpolate(erosion.evaluateNoise(pos.x(), pos.z()));

        return (int) Math.round(continentalHeight + erosionHeight - 30);
    }

    private void placeDecorations(GenerationUnit unit, double height, Point bottom) {
        // Flowers
        if (flowers.evaluateNoise(bottom.x(), bottom.z()) > .6 && random.evaluateNoise(bottom.x(), bottom.z()) > .6) {
            unit.modifier().fill(bottom.withY(height), bottom.add(1, 0, 1).withY(height + 1), Block.POPPY);
            return;
        }
        if (flowers.evaluateNoise(bottom.x(), bottom.z()) < -0.6 && random.evaluateNoise(bottom.x(), bottom.z()) > .6) {
            unit.modifier().fill(bottom.withY(height), bottom.add(1, 0, 1).withY(height + 1), Block.DANDELION);
            return;
        }
        // Grass
        if (grass.evaluateNoise(bottom.x(), bottom.z()) > 0.8 || random.evaluateNoise(bottom.x(), bottom.z()) > 0.1) {
            if (random.evaluateNoise(bottom.x(), bottom.z()) < 0.2) {
                unit.modifier().fill(bottom.withY(height), bottom.add(1, 0, 1).withY(height + 1), Block.TALL_GRASS);
                unit.modifier().fill(bottom.withY(height + 1), bottom.add(1, 0, 1).withY(height + 2), Block.TALL_GRASS.withProperty("half","upper"));
            } else {
                unit.modifier().fill(bottom.withY(height), bottom.add(1, 0, 1).withY(height + 1), Block.SHORT_GRASS);
            }
            return;
        }

        if (random.evaluateNoise(bottom.x(), bottom.z()) > 0.09) {
            placeTree(unit, height, bottom);
        }
    }

    private void placeTree(GenerationUnit unit, double height, Point bottom) {
        height = height + getTreeHeight(bottom);
        GenerationUnit fork = unit.fork(bottom.add(-2, 0, -2).withY(height), bottom.add(3, 0, 3).withY(height + 11));
        fork.modifier().fill(bottom.add(-2, 0, -2).withY(height + 3), bottom.add(3, 0, 3).withY(height + 5), Block.OAK_LEAVES);
        fork.modifier().fill(bottom.add(-1, 0, -1).withY(height + 5), bottom.add(2, 0, 2).withY(height + 7), Block.OAK_LEAVES);

        placeLeaf(fork, bottom.add(-2, 0, -2).withY(height + 4));
        placeLeaf(fork, bottom.add(2, 0, -2).withY(height + 4));
        placeLeaf(fork, bottom.add(-2, 0, 2).withY(height + 4));
        placeLeaf(fork, bottom.add(2, 0, 2).withY(height + 4));

        placeLeaf(fork, bottom.add(-1, 0, -1).withY(height + 5));
        placeLeaf(fork, bottom.add(1, 0, -1).withY(height + 5));
        placeLeaf(fork, bottom.add(-1, 0, 1).withY(height + 5));
        placeLeaf(fork, bottom.add(1, 0, 1).withY(height + 5));

        placeLeaf(fork, bottom.add(-1, 0, -1).withY(height + 6));
        placeLeaf(fork, bottom.add(1, 0, -1).withY(height + 6));
        placeLeaf(fork, bottom.add(-1, 0, 1).withY(height + 6));
        placeLeaf(fork, bottom.add(1, 0, 1).withY(height + 6));

        fork.modifier().fill(bottom.withY(height - getTreeHeight(bottom)), bottom.add(1, 0, 1).withY(height + 6), Block.OAK_WOOD);
    }

    private void placeLeaf(GenerationUnit fork, Point pos) {
        if (random.evaluateNoise(pos.x(), pos.z()) > 0.6) return;
        fork.modifier().setBlock(pos, Block.AIR);
    }

    private int getTreeHeight(Point pos) {
        double randomness = random.evaluateNoise(pos.x(), pos.z());
        if (randomness < .093) {
            return 0;
        }
        if (randomness > .096) {
            return -1;
        }
        return -2;
    }
}
