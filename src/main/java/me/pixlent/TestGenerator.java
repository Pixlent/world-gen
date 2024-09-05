package me.pixlent;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public class TestGenerator implements Generator {
    @Override
    public void generate(@NotNull GenerationUnit unit) {
        final Point min = unit.absoluteStart();
        final Point max = unit.absoluteEnd();

        for (int x = min.blockX(); x < max.blockX(); x++) {
            for (int z = min.blockZ(); z < max.blockZ(); z++) {
                // Column and rows
                unit.modifier().fill(new Vec(x, 0, z), new Vec(x+1, 60 + NoiseRegistry.CONTINENTALNESS.evaluateNoise(x, z) * 16, z+1), Block.STONE);
            }
        }
    }
}
