package velrondevs.masterfulbotany.datagen.providers.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.sandbox.NoiseProviderBase;
import org.moddingx.libx.sandbox.SandBox;
import velrondevs.botania.registry.BotaniaBlocks;

public class AlfheimNoise extends NoiseProviderBase {

    private static final double HEIGHT_FACTOR = 224;
    
    private static final double WEIRDNESS_FACTOR = 0.03;
    
    private static final double WEIRDNESS_SHIFT = 7;

    public final Holder<DensityFunction> alfheimContinentalness = this.density(DensityFunctions.cache2d(this.shifted(Noises.BADLANDS_SURFACE, 0.088)));
    
    public final Holder<DensityFunction> alfheimErosion = this.density(DensityFunctions.cache2d(this.shifted(Noises.CONTINENTALNESS, 2)));

    public final Holder<DensityFunction> alfheimLow = this.density(DensityFunctions.spline(
            CubicSpline.builder(new DensityFunctions.Spline.Coordinate(this.alfheimContinentalness))
                    .addPoint(-1, -0.8f)
                    .addPoint(-0.4f, -0.2f, 1)
                    .addPoint(0, 0)
                    .addPoint(0.2f, 0.02f)
                    .addPoint(0.25f, 0.095f)
                    .addPoint(0.37f, 0.1f)
                    .addPoint(0.38f, 0.18f)
                    .addPoint(0.42f, 0.18f)
                    .addPoint(0.58f, 0.38f)
                    .addPoint(0.65f, 0.38f)
                    .addPoint(0.75f, 0.7f)
                    .addPoint(1, 0.7f)
                    .build()
    ));
    
    public final Holder<DensityFunction> alfheimHigh = this.density(DensityFunctions.spline(
            CubicSpline.builder(new DensityFunctions.Spline.Coordinate(this.alfheimContinentalness))
                    .addPoint(-1, -1)
                    .addPoint(-0.7f, -0.9f)
                    .addPoint(-0.25f, -0.5f, 2)
                    .addPoint(-0.1f, -0.1f)
                    .addPoint(0, 0)
                    .addPoint(0.3f, 0.25f, 1)
                    .addPoint(0.8f, 1, 0.5f)
                    .addPoint(0.9f, 0.9f)
                    .addPoint(1, 1)
                    .build()
    ));
    
    public final Holder<DensityFunction> alfheimHeight = this.density(SandBox.Density.smashY(
            DensityFunctions.interpolated(SandBox.Density.lerp(
                    new DensityFunctions.HolderHolder(alfheimLow),
                    new DensityFunctions.HolderHolder(alfheimHigh),
                    new DensityFunctions.HolderHolder(alfheimErosion),
                    0.15, 0.4
            ))
    ));

    public final Holder<DensityFunction> alfheimWeirdness = this.density(this.clampNormal(DensityFunctions.mul(
            this.shifted(Noises.RIDGE, 0.25),
            DensityFunctions.constant(2)
    )));

    public final Holder<DensityFunction> alfheimVariation = this.density(DensityFunctions.noise(this.holder(Noises.SURFACE)));

    public final Holder<DensityFunction> alfheimInitial = this.density(DensityFunctions.add(
            new DensityFunctions.HolderHolder(alfheimVariation),
            DensityFunctions.mul(DensityFunctions.constant(-1 / WEIRDNESS_FACTOR), DensityFunctions.mul(
                    DensityFunctions.mul(
                            DensityFunctions.add(
                                    DensityFunctions.add(
                                            DensityFunctions.yClampedGradient(-64, 320, 64 / HEIGHT_FACTOR, -320 / HEIGHT_FACTOR),
                                            this.clampNormal(new DensityFunctions.HolderHolder(alfheimHeight).quarterNegative())
                                    ),
                                    DensityFunctions.constant(64 / HEIGHT_FACTOR)
                            ),
                            DensityFunctions.constant(-HEIGHT_FACTOR / 256d)
                    ),
                    DensityFunctions.add(
                            DensityFunctions.mul(
                                    DensityFunctions.add(
                                            this.clampNormal(new DensityFunctions.HolderHolder(alfheimWeirdness)),
                                            DensityFunctions.constant(-1)
                                    ),
                                    DensityFunctions.constant(-0.5)
                            ),
                            DensityFunctions.constant(WEIRDNESS_SHIFT * WEIRDNESS_FACTOR)
                    )
            ))
    ));
    
    public final Holder<DensityFunction> alfheimDepth = this.density(DensityFunctions.add(
            DensityFunctions.yClampedGradient(-64, 320, 0.5, -1),
            this.clampNormal(new DensityFunctions.HolderHolder(alfheimHeight))
    ));
    
    public final Holder<DensityFunction> alfheimTemperature = this.density(this.clampNormal(DensityFunctions.zero()));
    public final Holder<DensityFunction> alfheimHumidity = this.density(this.clampNormal(DensityFunctions.zero()));

    public final Holder<DensityFunction> alfheimCaves = this.density(DensityFunctions.interpolated(NoiseRouterData.underground(
            this.registries.registry(Registries.DENSITY_FUNCTION),
            this.registries.registry(Registries.NOISE),
            NoiseRouterData.entrances(
                    this.registries.registry(Registries.DENSITY_FUNCTION),
                    this.registries.registry(Registries.NOISE)
            )
    )));
    
    public final Holder<DensityFunction> alfheimFinal = this.density(DensityFunctions.min(
            new DensityFunctions.HolderHolder(this.alfheimInitial),
            new DensityFunctions.HolderHolder(this.alfheimCaves)
    ));

    public final Holder<NoiseGeneratorSettings> alfheim = this.generator()
            .defaultBlock(BotaniaBlocks.livingrock)
            .disableOreVeins()
            .router().initialDensityWithoutJaggedness(this.alfheimInitial)
            .router().finalDensity(this.alfheimFinal)
            .router().continents(this.alfheimContinentalness)
            .router().erosion(this.alfheimErosion)
            .router().ridges(this.alfheimWeirdness)
            .router().depth(this.alfheimDepth)
            .router().temperature(this.alfheimTemperature)
            .router().vegetation(this.alfheimHumidity)
            .build();

    public AlfheimNoise(DatagenContext ctx) {
        super(ctx);
    }

    private DensityFunction shifted(ResourceKey<NormalNoise.NoiseParameters> noise, double scale) {
        return shifted(this.holder(noise), scale);
    }

    private DensityFunction shifted(Holder<NormalNoise.NoiseParameters> noise, double scale) {
        return DensityFunctions.shiftedNoise2d(
                DensityFunctions.cacheOnce(DensityFunctions.shiftA(this.holder(Noises.SHIFT))),
                DensityFunctions.cacheOnce(DensityFunctions.shiftB(this.holder(Noises.SHIFT))),
                scale, noise
        );
    }

    private DensityFunction clampNormal(DensityFunction density) {
        return SandBox.Density.clamp(density, -1, 1);
    }
}
