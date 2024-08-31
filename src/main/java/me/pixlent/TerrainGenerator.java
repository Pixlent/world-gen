package me.pixlent;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import me.pixlent.utils.SplineInterpolator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class TerrainGenerator implements Generator {
    private static final List<SlopeBlock> SURFACE_SLOPE_BLOCKS = List.of(
            new SlopeBlock(20, Block.GRASS_BLOCK),
            new SlopeBlock(45, Block.MOSS_BLOCK),
            new SlopeBlock(75, Block.MOSSY_COBBLESTONE),
            new SlopeBlock(80, Block.COBBLESTONE),
            new SlopeBlock(85, Block.STONE)
    );

    private static final List<SlopeBlock> WATER_SLOPE_BLOCKS = List.of(
            new SlopeBlock(45, Block.GRAVEL),
            new SlopeBlock(75, Block.STONE),
            new SlopeBlock(Double.MAX_VALUE, Block.STONE)
    );

    private static final List<SlopeBlock> BEACH_SLOPE_BLOCKS = List.of(
            new SlopeBlock(60, Block.SAND),
            new SlopeBlock(90, Block.MOSS_BLOCK),
            new SlopeBlock(Double.MAX_VALUE, Block.SAND)
    );

    final long seed = 0;
    final Random seededSeedGenerator = new Random(seed);
    final SplineInterpolator continentalInterpolator = SplineInterpolator.builder()
            .add(-1, 0)
            .add(-0.6, 30)
            .add(0.35, 62)
            .add(0.5, 64)
            .add(0.7, 70)
            .add(0.855, 100)
            .add(1, 120)
            .build();
    final JNoise continentalness = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(seededSeedGenerator.nextLong())
                    .build())
            .octavate(5, 0.5, 2.2, FractalFunction.FBM, false)
            .scale(0.002)
            .build();
    final SplineInterpolator erosionInterpolator = SplineInterpolator.builder()
            .add(-1, 20)
            .add(-0.2, 50)
            .add(0.55, 25)
            .add(0.6, 40)
            .add(0.8, 45)
            .add(0.85, 70)
            .add(1, 90)
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
        final Point min = unit.absoluteStart();
        final Point max = unit.absoluteEnd();

        for (int x = min.blockX(); x < max.blockX(); x++) {
            for (int z = min.blockZ(); z < max.blockZ(); z++) {
                final Point bottom = new Pos(x, 0, z);

                int height = getHeight(bottom);

                final double slope = calculateSlope(x, height, z);
                Block blockType = Block.DIRT;
                if (height >= 68) {
                    for (final SlopeBlock slopeBlock : SURFACE_SLOPE_BLOCKS) {
                        if (slope <= slopeBlock.slopeDegree()) {
                            blockType = slopeBlock.blockType();
                            break;
                        }
                    }
                } else if(height > 61) {
                    for (final SlopeBlock slopeBlock : BEACH_SLOPE_BLOCKS) {
                        if (slope <= slopeBlock.slopeDegree()) {
                            blockType = slopeBlock.blockType();
                            break;
                        }
                    }
                } else {
                    for (final SlopeBlock slopeBlock : WATER_SLOPE_BLOCKS) {
                        if (slope <= slopeBlock.slopeDegree()) {
                            blockType = slopeBlock.blockType();
                            break;
                        }
                    }
                }

                unit.modifier().fill(new Vec(x, 0, z), new Vec(x + 1, height, z + 1), blockType);

                if (height >= 68) {
                    placeDecorations(unit, height, bottom);
                } else {
                    unit.modifier().fill(new Vec(x, height, z), new Vec(x + 1, 65, z + 1), Block.WATER);
                }
                if (height <= 50) {
                    placeOceanDecorations(unit, height, bottom);
                }
            }
        }
    }

    public int getHeight(Point pos) {
        double continentalHeight = continentalInterpolator.interpolate(continentalness.evaluateNoise(pos.x(), pos.z()));
        double erosionHeight = erosionInterpolator.interpolate(erosion.evaluateNoise(pos.x(), pos.z()));

        return (int) Math.round(continentalHeight + erosionHeight - 30);
    }

    private double calculateSlope(final int x, final double y, final int z) {
        final int radius = 1;

        double maxDiff = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;

                final double neighborY = getHeight(new Pos(x + dx, 0, z + dz));
                final double diff = Math.abs(y - neighborY);

                maxDiff = Math.max(maxDiff, diff);
            }
        }

        return Math.toDegrees(Math.atan(maxDiff / radius));
    }

    private void placeDecorations(GenerationUnit unit, double height, Point bottom) {
        double slope = calculateSlope(bottom.blockX(), height, bottom.blockZ());
        if (slope > 45) { // Example threshold for steepness
            return;
        }

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

    private void placeOceanDecorations(GenerationUnit unit, double height, Point bottom) {
        if (flowers.evaluateNoise(bottom.x(), bottom.z()) > -0.3 && random.evaluateNoise(bottom.x(), bottom.z()) > .5) {
            unit.modifier().fill(bottom.withY(height), bottom.add(1, 0, 1).withY(height + ((64 - height) * .8 * (random.evaluateNoise(bottom.x(), bottom.z())))), Block.KELP_PLANT);
            unit.modifier().setBlock(bottom.withY(height + ((64 - height) * .8 * (random.evaluateNoise(bottom.x(), bottom.z())))), Block.KELP);
            return;
        }
        if (flowers.evaluateNoise(bottom.x(), bottom.z()) < -0.2) {
            if (random.evaluateNoise(bottom.x(), bottom.z()) > .4) {
                unit.modifier().setBlock(bottom.withY(height), Block.SEAGRASS);
            }
            if (random.evaluateNoise(bottom.x(), bottom.z()) < -0.6) {
                unit.modifier().setBlock(bottom.withY(height), Block.TALL_SEAGRASS);
                unit.modifier().setBlock(bottom.withY(height + 1), Block.TALL_SEAGRASS.withProperty("half","upper"));
            }
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
