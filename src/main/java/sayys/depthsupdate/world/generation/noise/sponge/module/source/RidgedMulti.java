package sayys.depthsupdate.world.generation.noise.sponge.module.source;

import sayys.depthsupdate.world.generation.noise.sponge.Noise;
import sayys.depthsupdate.world.generation.noise.sponge.NoiseQuality;
import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class RidgedMulti extends Module {
   public static final double DEFAULT_RIDGED_FREQUENCY = 1.0D;
   public static final double DEFAULT_RIDGED_LACUNARITY = 2.0D;
   public static final int DEFAULT_RIDGED_OCTAVE_COUNT = 6;
   public static final NoiseQuality DEFAULT_RIDGED_QUALITY;
   public static final int DEFAULT_RIDGED_SEED = 0;
   public static final int RIDGED_MAX_OCTAVE = 30;
   private double frequency = 1.0D;
   private double lacunarity = 2.0D;
   private NoiseQuality noiseQuality;
   private int octaveCount;
   private double[] spectralWeights;
   private int seed;

   public RidgedMulti() {
      super(0);
      this.noiseQuality = DEFAULT_RIDGED_QUALITY;
      this.octaveCount = 6;
      this.seed = 0;
      this.calcSpectralWeights();
   }

   public double getFrequency() {
      return this.frequency;
   }

   public void setFrequency(double frequency) {
      this.frequency = frequency;
   }

   public double getLacunarity() {
      return this.lacunarity;
   }

   public void setLacunarity(double lacunarity) {
      this.lacunarity = lacunarity;
   }

   public NoiseQuality getNoiseQuality() {
      return this.noiseQuality;
   }

   public void setNoiseQuality(NoiseQuality noiseQuality) {
      this.noiseQuality = noiseQuality;
   }

   public int getOctaveCount() {
      return this.octaveCount;
   }

   public void setOctaveCount(int octaveCount) {
      this.octaveCount = Math.min(octaveCount, 30);
   }

   public int getSeed() {
      return this.seed;
   }

   public void setSeed(int seed) {
      this.seed = seed;
   }

   private void calcSpectralWeights() {
      double h = 1.0D;
      double frequency = 1.0D;
      this.spectralWeights = new double[30];

      for(int i = 0; i < 30; ++i) {
         this.spectralWeights[i] = Math.pow(frequency, -h);
         frequency *= this.lacunarity;
      }

   }

   public int getSourceModuleCount() {
      return 0;
   }

   public double getValue(double x, double y, double z) {
      double x1 = x * this.frequency;
      double y1 = y * this.frequency;
      double z1 = z * this.frequency;
      double value = 0.0D;
      double weight = 1.0D;
      double offset = 1.0D;
      double gain = 2.0D;

      for(int curOctave = 0; curOctave < this.octaveCount; ++curOctave) {
         double nx = Utils.makeInt32Range(x1);
         double ny = Utils.makeInt32Range(y1);
         double nz = Utils.makeInt32Range(z1);
         int seed = this.seed + curOctave & Integer.MAX_VALUE;
         double signal = Noise.gradientCoherentNoise3D(nx, ny, nz, seed, this.noiseQuality) * 2.0D - 1.0D;
         signal = Math.abs(signal);
         signal = offset - signal;
         signal *= signal;
         signal *= weight;
         weight = signal * gain;
         if (weight > 1.0D) {
            weight = 1.0D;
         }

         if (weight < 0.0D) {
            weight = 0.0D;
         }

         value += signal * this.spectralWeights[curOctave];
         x1 *= this.lacunarity;
         y1 *= this.lacunarity;
         z1 *= this.lacunarity;
      }

      return value / 1.6D;
   }

   static {
      DEFAULT_RIDGED_QUALITY = NoiseQuality.STANDARD;
   }
}
