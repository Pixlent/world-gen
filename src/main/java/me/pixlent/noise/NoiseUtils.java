package me.pixlent.noise;

import de.articdive.jnoise.core.api.modifiers.NoiseModifier;

public class NoiseUtils {
    public record AbsClampNoiseModifier() implements NoiseModifier {
        @Override
        public double apply(double result) {
            return (result + 1.0) * 0.5;
        }
    }
}
