package me.pixlent;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public class TerrainGenerator implements Generator {
    final long seed = 0;
    final JNoise random = JNoise.newBuilder()
            .white(seed)
            .build();
    final JNoise noise = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(seed).build())
            .scale(.013)
            .octavate(4, .6, 1.6, FractalFunction.FBM, false)
            .build();
    final JNoise grass = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 1).build())
            .octavate(2, .3, 1.4, FractalFunction.FBM, false)
            .scale(.015)
            .build();
    final JNoise flowers = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 2).build())
            .octavate(2, .3, 1.4, FractalFunction.FBM, false)
            .scale(.04)
            .build();

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();

        for (int x = 0; x < unit.size().x(); x++) {
            for (int z = 0; z < unit.size().z(); z++) {
                Point bottom = start.add(x, 0, z);

                synchronized (noise) { // Synchronization is necessary for JNoise
                    double height = noise.evaluateNoise(bottom.x(), bottom.z()) * 16;
                    // * 16 means the height will be between -16 and +16
                    unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(61 + height), Block.STONE);
                    unit.modifier().fill(bottom.withY(64 + height), bottom.add(1, 0, 1).withY(63), Block.WATER);

                    if ((60 + height) > 61) {
                        unit.modifier().fill(bottom.withY(61 + height), bottom.add(1, 0, 1).withY(63 + height), Block.DIRT);
                        unit.modifier().fill(bottom.withY(63 + height), bottom.add(1, 0, 1).withY(64 + height), Block.GRASS_BLOCK);

                        placeDecorations(unit, x, z, height, bottom);
                    } else {
                        // Sand
                        unit.modifier().fill(bottom.withY(61 + height), bottom.add(1, 0, 1).withY(64 + height), Block.SAND);
                    }
                }
            }
        }
    }

    private void placeDecorations(GenerationUnit unit, int x, int z, double height, Point bottom) {
        // Flowers
        if (flowers.evaluateNoise(bottom.x(), bottom.z()) > .7 && random.evaluateNoise(bottom.x(), bottom.z()) > .6) {
            unit.modifier().fill(bottom.withY(64 + height), bottom.add(1, 0, 1).withY(65 + height), Block.POPPY);
            return;
        }
        if (flowers.evaluateNoise(bottom.x(), bottom.z()) < -0.5 && random.evaluateNoise(bottom.x(), bottom.z()) > .6) {
            unit.modifier().fill(bottom.withY(64 + height), bottom.add(1, 0, 1).withY(65 + height), Block.DANDELION);
            return;
        }
        // Grass
        if (grass.evaluateNoise(bottom.x(), bottom.z()) > 0.8 || random.evaluateNoise(bottom.x(), bottom.z()) > 0.1) {
            if (random.evaluateNoise(bottom.x(), bottom.z()) < 0.2) {
                unit.modifier().fill(bottom.withY(64 + height), bottom.add(1, 0, 1).withY(65 + height), Block.TALL_GRASS);
                unit.modifier().fill(bottom.withY(65 + height), bottom.add(1, 0, 1).withY(66 + height), Block.TALL_GRASS.withProperty("half","upper"));
            } else {
                unit.modifier().fill(bottom.withY(64 + height), bottom.add(1, 0, 1).withY(65 + height), Block.SHORT_GRASS);
            }
            return;
        }

        if (random.evaluateNoise(bottom.x(), bottom.z()) > 0.09) {
            placeTree(unit, height, bottom);
        }
    }

    private void placeTree(GenerationUnit unit, double height, Point bottom) {
        height = height + getTreeHeight(bottom);
        GenerationUnit fork = unit.fork(bottom.add(-2, 0, -2).withY(64+height), bottom.add(3, 0, 3).withY(75+height));
        fork.modifier().fill(bottom.add(-2, 0, -2).withY(67 + height), bottom.add(3, 0, 3).withY(69+height), Block.OAK_LEAVES);
        fork.modifier().fill(bottom.add(-1, 0, -1).withY(69 + height), bottom.add(2, 0, 2).withY(71+height), Block.OAK_LEAVES);

        placeLeaf(fork, bottom.add(-2, 0, -2).withY(height + 68));
        placeLeaf(fork, bottom.add(2, 0, -2).withY(height + 68));
        placeLeaf(fork, bottom.add(-2, 0, 2).withY(height + 68));
        placeLeaf(fork, bottom.add(2, 0, 2).withY(height + 68));

        placeLeaf(fork, bottom.add(-1, 0, -1).withY(height + 69));
        placeLeaf(fork, bottom.add(1, 0, -1).withY(height + 69));
        placeLeaf(fork, bottom.add(-1, 0, 1).withY(height + 69));
        placeLeaf(fork, bottom.add(1, 0, 1).withY(height + 69));

        placeLeaf(fork, bottom.add(-1, 0, -1).withY(height + 70));
        placeLeaf(fork, bottom.add(1, 0, -1).withY(height + 70));
        placeLeaf(fork, bottom.add(-1, 0, 1).withY(height + 70));
        placeLeaf(fork, bottom.add(1, 0, 1).withY(height + 70));

        fork.modifier().fill(bottom.withY(64 + height - getTreeHeight(bottom)), bottom.add(1, 0, 1).withY(70+height), Block.OAK_WOOD);
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
