package me.pixlent;

import de.articdive.jnoise.core.api.modifiers.NoiseModifier;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import me.pixlent.utils.ExecutionTimer;
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
            .add(0.0, 0.0)
            .add(0.25, 0.1)
            .add(0.285, 0.38)
            .add(0.3, 0.444)
            .add(0.39, 0.666)
            .add(0.433, 0.777)
            .add(0.5, 0.888)
            .add(0.7, 0.97)
            .add(1, 1)
            .build();
    final JNoise continentalness = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(seededSeedGenerator.nextLong())
                    .build())
            .octavate(5, 0.5, 2.2, FractalFunction.FBM, false)
            .scale(0.002)
            .addModifier(new AbsClampNoiseModifier())
            .build();
    final SplineInterpolator erosionInterpolator = SplineInterpolator.builder()
            .add(0, 0)
            .add(0.1, 0.3)
            .add(0.5, 0.9)
            .add(0.6, 0.91)
            .add(0.62, 0.7)
            .add(0.7, 0.71)
            .add(0.72, 0.91)
            .add(1, 1)
            .build();
    final JNoise erosion = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(seededSeedGenerator.nextLong())
                    .build())
            .octavate(5, 0.3, 3, FractalFunction.FBM, false)
            .scale(0.0015)
            .invert()
            .addModifier(new AbsClampNoiseModifier())
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
        final ExecutionTimer timer = new ExecutionTimer();
        final Point min = unit.absoluteStart();
        final Point max = unit.absoluteEnd();

        for (int x = min.blockX(); x < max.blockX(); x++) {
            for (int z = min.blockZ(); z < max.blockZ(); z++) {
                double previousDensity = 0.0;
                for (int y = max.blockY(); y > min.blockY(); y--) {
                    double density = getDensity(x, y, z);
                    if (density > 1) {
                        if (previousDensity > 1) {
                            populate(unit, new Vec(x, y, z), density);
                        } else {
                            unit.modifier().setBlock(x, y, z, Block.STONE);
                        }
                    }
                    previousDensity = density;
                }

                //populate(unit, new Vec(x, getHeight(new Vec(x, 0, z)), z));
            }
        }
        System.out.println("Loading chunk took: " + timer.finished() + "ms");
    }

    private void populate(@NotNull GenerationUnit unit, Vec pos, double density) {
        final double slope = calculateSlope(pos.blockX(), pos.y(), pos.blockZ());
        Block blockType = Block.DIRT;
        if (pos.y() >= 68) {
            for (final SlopeBlock slopeBlock : SURFACE_SLOPE_BLOCKS) {
                if (slope <= slopeBlock.slopeDegree()) {
                    blockType = slopeBlock.blockType();
                    break;
                }
            }
        } else if(pos.y() > 61) {
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

        unit.modifier().setBlock(pos, blockType);

        if (pos.y() >= 68) {
            placeDecorations(unit, pos);
        } else if (density <= 0) {
            unit.modifier().fill(pos, pos.add(1, 0, 1).withY(65), Block.WATER);
            if (pos.y() <= 50) {
                placeDecorations(unit, pos);
            }
        }
    }

    private double getDensity(int x, int y, int z) {
        double continentalHeight = continentalness.evaluateNoise(x, y, z);
        double erosionHeight = erosion.evaluateNoise(x, y, z);
        double density = continentalHeight + erosionHeight;
        int surfaceHeight = getHeight(new Vec(x, y, z));

        if (surfaceHeight >= y - 10) {
            return density * 1.5;
        }
        if (surfaceHeight <= y + 15) {
            return density * 0.5;
        }

        return density;
    }

    public int getHeight(Point pos) {
        double continentalHeight = continentalInterpolator.interpolate(continentalness.evaluateNoise(pos.x(), pos.z()));
        double erosionHeight = erosionInterpolator.interpolate(erosion.evaluateNoise(pos.x(), pos.z()));

        double surfaceLevel;

        if (continentalHeight < 4) {
            surfaceLevel = continentalHeight;
        } else {
            surfaceLevel = (continentalHeight + erosionHeight) / 2;
        }

        return (int) Math.round(surfaceLevel * 80);
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

    private void placeDecorations(GenerationUnit unit, Point pos) {
        double slope = calculateSlope(pos.blockX(), pos.blockY(), pos.blockZ());
        if (slope > 45) { // Example threshold for steepness
            return;
        }

        // Flowers
        if (flowers.evaluateNoise(pos.x(), pos.z()) > .6 && random.evaluateNoise(pos.x(), pos.z()) > .6) {
            unit.modifier().fill(pos, pos.add(1, 1, 1), Block.POPPY);
            return;
        }
        if (flowers.evaluateNoise(pos.x(), pos.z()) < -0.6 && random.evaluateNoise(pos.x(), pos.z()) > .6) {
            unit.modifier().fill(pos, pos.add(1, 1, 1), Block.DANDELION);
            return;
        }
        // Grass
        if (grass.evaluateNoise(pos.x(), pos.z()) > 0.8 || random.evaluateNoise(pos.x(), pos.z()) > 0.1) {
            if (random.evaluateNoise(pos.x(), pos.z()) < 0.2) {
                unit.modifier().fill(pos, pos.add(1, 1, 1), Block.TALL_GRASS);
                unit.modifier().fill(pos.add(0, 1, 0), pos.add(1, 2, 1), Block.TALL_GRASS.withProperty("half","upper"));
            } else {
                unit.modifier().fill(pos, pos.add(1, 1, 1), Block.SHORT_GRASS);
            }
            return;
        }

        if (random.evaluateNoise(pos.x(), pos.z()) > 0.09) {
            placeTree(unit, pos);
        }
    }

    private void placeOceanDecorations(GenerationUnit unit, Point pos) {
        if (flowers.evaluateNoise(pos.x(), pos.z()) > -0.3 && random.evaluateNoise(pos.x(), pos.z()) > .5) {
            unit.modifier().fill(pos, pos.add(1, 0, 1).add(0, ((67   - pos.y()) * .8 * (random.evaluateNoise(pos.x(), pos.z()))), 0), Block.KELP_PLANT);
            unit.modifier().setBlock(pos.add(0, (67 - pos.y()) * .8 * (random.evaluateNoise(pos.x(), pos.z())), 0), Block.KELP);
            return;
        }
        if (flowers.evaluateNoise(pos.x(), pos.z()) < -0.2) {
            if (random.evaluateNoise(pos.x(), pos.z()) > .4) {
                unit.modifier().setBlock(pos, Block.SEAGRASS);
            }
            if (random.evaluateNoise(pos.x(), pos.z()) < -0.6) {
                unit.modifier().setBlock(pos, Block.TALL_SEAGRASS);
                unit.modifier().setBlock(pos.add(0, 1, 0), Block.TALL_SEAGRASS.withProperty("half","upper"));
            }
        }
    }

    private void placeTree(GenerationUnit unit, Point pos) {
        pos = pos.withY(pos.y() + getTreeHeight(pos));

        GenerationUnit fork = unit.fork(pos.add(-2, 0, -2), pos.add(3, 11, 3));
        fork.modifier().fill(pos.add(-2, 3, -2), pos.add(3, 5, 3), Block.OAK_LEAVES);
        fork.modifier().fill(pos.add(-1, 5, -1), pos.add(2, 7, 2), Block.OAK_LEAVES);

        placeLeaf(fork, pos.add(-2, 4, -2));
        placeLeaf(fork, pos.add(2, 4, -2));
        placeLeaf(fork, pos.add(-2, 4, 2));
        placeLeaf(fork, pos.add(2, 4, 2));

        placeLeaf(fork, pos.add(-1, 5, -1));
        placeLeaf(fork, pos.add(1, 5, -1));
        placeLeaf(fork, pos.add(-1, 5, 1));
        placeLeaf(fork, pos.add(1, 5, 1));

        placeLeaf(fork, pos.add(-1, 6, -1));
        placeLeaf(fork, pos.add(1, 6, -1));
        placeLeaf(fork, pos.add(-1, 6, 1));
        placeLeaf(fork, pos.add(1, 6, 1));

        fork.modifier().fill(pos.withY(pos.y() - getTreeHeight(pos)), pos.add(1, 6, 1), Block.OAK_WOOD);
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

    private boolean inRange(double min, double max, double num) {
        return num >= min && num <= max;
    }

    public record AbsClampNoiseModifier() implements NoiseModifier {

        @Override
        public double apply(double result) {
            return (result + 1.0) * 0.5;
        }
    }
}
