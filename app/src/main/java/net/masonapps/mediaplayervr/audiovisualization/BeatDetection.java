package net.masonapps.mediaplayervr.audiovisualization;

/**
 * Created by Bob on 5/2/2016.
 */
public class BeatDetection {

    private static final String TAG = BeatDetection.class.getSimpleName();
    private final int mNumFrequencys;
    private final int mHistorySize;
    public float[] mCurrentAmplitudes;
    private float[][] mBuffer;
    private float[] mAvgs;
    private float[] mVariances;
    private boolean[] mHasBeat;

    //  fft capture size with real and imaginary components. buffer will be half the size since it only holds amplitudes
    public BeatDetection(int fftCaptureSize, int historySize) {
        if (fftCaptureSize <= 0 || historySize <= 0)
            throw new IllegalArgumentException("fftCaptureSize and historySize must be greater than 0");
        mHistorySize = historySize;
        mNumFrequencys = fftCaptureSize / 2;
        mBuffer = new float[historySize][mNumFrequencys];
        mCurrentAmplitudes = new float[mNumFrequencys];
        mAvgs = new float[mNumFrequencys];
        mVariances = new float[mNumFrequencys];
        mHasBeat = new boolean[mNumFrequencys];
    }

    public void update(byte[] fft) {
        if (fft.length != mNumFrequencys * 2)
            throw new IllegalArgumentException("FFT array length must be the same as the fftCaptureSize passed into the constructor");

        int histIndex, freqIndex, fftIndex;
        //update averages
        for (freqIndex = 0; freqIndex < mNumFrequencys; freqIndex++) {
            for (histIndex = 0; histIndex < mHistorySize; histIndex++) {
                mAvgs[freqIndex] += mBuffer[histIndex][freqIndex];
            }
            mAvgs[freqIndex] /= mHistorySize;
            for (histIndex = 0; histIndex < mHistorySize; histIndex++) {
                final float diff = mBuffer[histIndex][freqIndex] - mAvgs[freqIndex];
                mVariances[freqIndex] += diff * diff;
            }
            mVariances[freqIndex] /= mHistorySize;
        }
        //shift amplitude values in buffer
        for (histIndex = 0; histIndex < mHistorySize - 1; histIndex++) {
            for (freqIndex = 0; freqIndex < mNumFrequencys; freqIndex++) {
                mBuffer[histIndex][freqIndex] = mBuffer[histIndex + 1][freqIndex];
            }
        }
        //save new amplitude values
        for (freqIndex = 0; freqIndex < mNumFrequencys; freqIndex++) {
            fftIndex = freqIndex * 2;
            mCurrentAmplitudes[freqIndex] = (float) Math.sqrt(fft[fftIndex] * fft[fftIndex] + fft[fftIndex + 1] * fft[fftIndex + 1]);
            mBuffer[mHistorySize - 1][freqIndex] = mCurrentAmplitudes[freqIndex];
        }
        //check new values for beat
        for (freqIndex = 0; freqIndex < mNumFrequencys; freqIndex++) {
//            float C = -0.0025714f * mVariances[freqIndex] + 1.5142857f;
            mHasBeat[freqIndex] = mCurrentAmplitudes[freqIndex] > mAvgs[freqIndex] + 2f * Math.sqrt(mVariances[freqIndex]);
        }

    }

    public void dispose() {
        mBuffer = null;
        mCurrentAmplitudes = null;
        mAvgs = null;
        mVariances = null;
        mHasBeat = null;
    }

    public boolean hasBeat(int i) {
        if (i < 0 || i >= mHasBeat.length) return false;
        return mHasBeat[i];
    }
}
