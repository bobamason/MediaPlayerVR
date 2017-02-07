package net.masonapps.mediaplayervr.utils;

/**
 * Created by Bob on 5/3/2016.
 */
public class WaveformUtils {

    public static float rootMeanSquare(byte[] waveform, float currentVolume) {
        float s = currentVolume == 0.0f ? 1.0f : 1.0f / currentVolume;
        float sum = 0;
        for (int i = 0; i < waveform.length; i++) {
            final float scaled = waveform[i] * s;
            sum += scaled * scaled;
        }
        return (float) Math.sqrt(sum / waveform.length);
    }
}
