package me.pixlent;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public class TestGenerator implements Generator {
    TerrainBuilder terrainBuilder;
    double densityThreshold = 0.5;
    int upperRelativeDensityLimit = 50;
    int lowerRelativeDensityLimit = 50;

    public TestGenerator(TerrainBuilder terrainBuilder) {
        this.terrainBuilder = terrainBuilder;
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        final Point min = unit.absoluteStart();
        final Point max = unit.absoluteEnd();

        for (int x = min.blockX(); x < max.blockX(); x++) {
            for (int z = min.blockZ(); z < max.blockZ(); z++) {
                if (x > 0) {
                    return;
                }

                // Column & Rows
                generate3D(min, max, x, z, unit);
                //generate2D(min, max, x, z, unit);
            }
        }
    }

    private void generate2D(Point min, Point max, int x, int z, GenerationUnit unit) {
        int height = terrainBuilder.getSurfaceHeight(x, z);
        unit.modifier().fill(min.withX(x).withZ(z), new Vec(x + 1, height, z + 1), Block.STONE);
    }

    private void generate3D(Point min, Point max, int x, int z, GenerationUnit unit) {
        int height = terrainBuilder.getSurfaceHeight(x, z);
        int upperDensityLimit = height + upperRelativeDensityLimit;
        int lowerDensityLimit = height - lowerRelativeDensityLimit;

        for (int y = upperDensityLimit; y >= lowerDensityLimit; y--) {
            if (y == lowerDensityLimit) {
                unit.modifier().fill(min.withX(x).withZ(z), new Vec(x + 1, lowerDensityLimit + 1, z + 1), Block.STONE);
                break;
            }

            Vec pos = new Vec(x, y, z);
            if (terrainBuilder.getDensity(x, y, z) > densityThreshold) {
                unit.modifier().setBlock(pos, Block.STONE);
            }
        }
    }

    private void decorate() {

    }
}
