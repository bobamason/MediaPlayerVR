package net.masonapps.mediaplayervr.audiovisualization;

import android.media.audiofx.Visualizer;

/**
 * Created by Bob on 5/6/2016.
 */
public class SpectrumAnalyzer {

    private static final String TAG = SpectrumAnalyzer.class.getSimpleName();
    private final float bandwidth;
    private final int samplingRate;
    private final int numOctaves;
    private final float captureSize;
    private final int numSamples;
    private final float[] currentAmplitudes;
    private final float[] averages;
    private final float[] waveform;
    private float dc = 0f;
    private boolean useSquaredAmplitude = false;

    public SpectrumAnalyzer(Visualizer visualizer) {
        samplingRate = visualizer.getSamplingRate() / 1000;
        captureSize = visualizer.getCaptureSize();
        waveform = new float[visualizer.getCaptureSize()];
        numSamples = visualizer.getCaptureSize() / 2 - 1;
        currentAmplitudes = new float[numSamples];
        bandwidth = (samplingRate / 2f) / (captureSize / 2f);
        numOctaves = numOctaves(Math.round(bandwidth), samplingRate);
        averages = new float[numOctaves];
    }

    public static float getFrequency(Visualizer visualizer, int k) {
        if (k > visualizer.getCaptureSize() / 2 - 1)
            throw new IllegalArgumentException("k must be less then number of frequency bands");
        return (float) (k * (visualizer.getSamplingRate() / 1000)) / (visualizer.getCaptureSize() / 2);
    }

    private static int numOctaves(int minBandwidth, int sampleRate) {
        int octaves = 1;
        float nqy = sampleRate / 2f;
        while ((nqy /= 2f) > minBandwidth) {
            octaves++;
        }
        return octaves;
    }

    private static void normalize(float[] array) {
        float sum = 0;
        int i;
        for (i = 0; i < array.length; i++) {
            sum += array[i] * array[i];
        }
        float mag = (float) Math.sqrt(sum);
        if (mag != 0) {
            for (i = 0; i < array.length; i++) {
                array[i] /= mag;
            }
        }
    }

    private int frequencyToIndex(float freq) {
        if (freq < bandwidth / 2f) return 0;
        if (freq > samplingRate / 2f - bandwidth / 2) return currentAmplitudes.length - 1;
        return Math.round(captureSize * (freq / samplingRate));
    }

    public float getBandwidth() {
        return bandwidth;
    }

    public int getNumOctaves() {
        return numOctaves;
    }

    public void updateWaveform(byte[] waveform, float volume) {
        synchronized (this.waveform) {
            for (int i = 0; i < waveform.length; i++) {
//                final float s = volume == 0f ? 1f : 1f / volume;
//                this.waveform[i] = ((float) waveform[i]) / 127f * s;
                this.waveform[i] = ((float) waveform[i]) / 255f;
            }
        }
    }

    public void update(byte[] fft, float volume) {
//        final float s = volume == 0 ? 1f : 1f / volume;
        int ampIndex = 0;
        dc = fft[0];
        synchronized (currentAmplitudes) {
            for (int i = 2; i < fft.length; i += 2) {
                final float real = fft[i];
                final float imag = fft[i + 1];
                if (ampIndex > currentAmplitudes.length) break;
                if (useSquaredAmplitude)
//                    currentAmplitudes[ampIndex] = (real * real + imag * imag) * s;
                    currentAmplitudes[ampIndex] = (real * real + imag * imag);
                else
//                    currentAmplitudes[ampIndex] = (float) Math.sqrt(real * real + imag * imag) * s;
                    currentAmplitudes[ampIndex] = (float) Math.sqrt(real * real + imag * imag);
                ampIndex++;
            }
        }
//        normalize(currentAmplitudes);
        updateAverages();
    }

    private void updateAverages() {
//        String str = "";
        for (int i = 0; i < numOctaves; i++) {
            float avg = 0;
            int lowFreq;
            if (i == 0)
                lowFreq = 0;
            else
                lowFreq = (int) (samplingRate / 2 / (float) (Math.pow(2, numOctaves - i)));
            final int highFreq = (int) (samplingRate / 2 / (float) (Math.pow(2, numOctaves - 1 - i)));
            final int lowBound = frequencyToIndex(lowFreq);
            final int highBound = frequencyToIndex(highFreq);
            float num = 0;
            for (int j = lowBound; j < highBound; j++) {
                avg += currentAmplitudes[j];
                num += 1f;
            }
            averages[i] = avg / num;
//            str += "octave #" + (i +1) + ": " + num + " elements \n";
        }
//        Log.d(TAG, str);
    }

    public void dispose() {
    }

    public float getAmplitude(int octave) {
        synchronized (averages) {
            if (octave < 0) octave = 0;
            if (octave >= averages.length) octave = averages.length - 1;
            return averages[octave];
        }
    }

    public float getCaptureSize() {
        return captureSize;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public float getDC() {
        return dc;
    }

    public float[] getCurrentAmplitudes() {
        return currentAmplitudes;
    }

    public float[] getWaveform() {
        return waveform;
    }

    public boolean getUseSquaredAmplitude() {
        return useSquaredAmplitude;
    }

    public void setUseSquaredAmplitude(boolean useSquaredAmplitude) {
        this.useSquaredAmplitude = useSquaredAmplitude;
    }
}
