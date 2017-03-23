package net.masonapps.mediaplayervr.audiovisualization;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.MainActivity;
import net.masonapps.mediaplayervr.MediaPlayerGame;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;

import java.io.IOException;
import java.util.List;

/**
 * Created by Bob on 5/5/2016.
 */
public abstract class MusicVisualizerScreen extends VrWorldScreen implements Visualizer.OnDataCaptureListener, ExtractorMediaSource.EventListener, ExoPlayer.EventListener {

    private static final String TAG = MusicVisualizerScreen.class.getSimpleName();
    private static final float ALPHA = 0.5f;
    protected final float[] intensityValues = new float[3];
    protected final SongDetails currentSongDetails;
    protected final List<SongDetails> songList;
    private final MainActivity context;
    private final Object lock = new Object();
    protected boolean hasAudioPermissions = false;
    protected Visualizer visualizer;
    protected int captureSize;
    protected SpectrumAnalyzer spectrumAnalyzer;
    protected Vector2 leftJoystick = new Vector2();
    protected Vector2 rightJoystick = new Vector2();
    //    protected GvrAudioEngine gvrAudioEngine;
    private boolean isUpdateFftEnabled = true;
    private boolean isUpdateWaveformEnabled = true;
    private boolean isVisualizerReady = false;
    private float[] maxValues = new float[3];
    private Ray ray = new Ray();
    private boolean mLoading;
    private AudioManager audioManager;
    private boolean prepared = false;
    private SimpleExoPlayer exoPlayer;
    private Handler handler;

    public MusicVisualizerScreen(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game);
        this.context = (MainActivity) context;
        this.songList = songList;
        currentSongDetails = songList.get(index);
        initializeMediaPlayer();
        playSong(currentSongDetails.uri);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        gvrAudioEngine = new GvrAudioEngine(context, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
        for (int i = 0; i < maxValues.length; i++) {
            maxValues[i] = 1f;
        }
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                loadGvrAudio(gvrAudioEngine);
//            }
//
//        }).start();
    }

    public static float getCenteredAxis(MotionEvent event,
                                        int axis, int historyPos) {
        final InputDevice.MotionRange range = event.getDevice().getMotionRange(axis, event.getSource());
        float flat;
        if (range != null) {
            flat = range.getFlat();
        } else {
            flat = 0.1f;
        }
        final float value = historyPos < 0 ? event.getAxisValue(axis)
                : event.getHistoricalAxisValue(axis, historyPos);
        if (Math.abs(value) > flat) {
            return value;
        }
        return 0;
    }

    private void initializeMediaPlayer() {
        if (handler == null)
            handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    exoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector(), new DefaultLoadControl());
                    lock.notify();
                }
            }
        });
    }

    @Override
    public void show() {
        GdxVr.app.getGvrView().setNeckModelEnabled(true);
        GdxVr.app.getGvrView().setNeckModelFactor(1f);
    }

    @Override
    public void hide() {

    }

    private void playSong(Uri uri) {
        if (exoPlayer == null) {
            synchronized (lock) {
                while (exoPlayer == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        exoPlayer.prepare(new ExtractorMediaSource(uri, new DefaultDataSourceFactory(context, context.getPackageName()), new DefaultExtractorsFactory(), null, this));
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
        prepared = false;
    }

    @Override
    public void resume() {
        if (prepared)
            exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        if (prepared)
            exoPlayer.setPlayWhenReady(false);
    }

    protected void loadGvrAudio(GvrAudioEngine audioEngine) {
    }

    public void initVisualizer(int audioSession) {
        visualizer = new Visualizer(audioSession);
        visualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);
        Log.d(TAG, "Visualizer range: " + Visualizer.getCaptureSizeRange()[0] + " - " + Visualizer.getCaptureSizeRange()[1]);
        if (!(512 > Visualizer.getCaptureSizeRange()[1]))
            captureSize = 512;
        else
            captureSize = Visualizer.getCaptureSizeRange()[1];
        if (visualizer.setCaptureSize(captureSize) != Visualizer.SUCCESS) {
            captureSize = visualizer.getCaptureSize();
        }
        Log.d(TAG, "visualizer capture size: " + captureSize);

        spectrumAnalyzer = new SpectrumAnalyzer(visualizer);
        Log.d(TAG, "SpectrumAnalyzer octaves: " + spectrumAnalyzer.getNumOctaves());
        Log.d(TAG, "SpectrumAnalyzer bandwidth: " + spectrumAnalyzer.getBandwidth());
        Log.d(TAG, "SpectrumAnalyzer sampling rate: " + spectrumAnalyzer.getSamplingRate());
        visualizer.setDataCaptureListener(this, Math.min(Visualizer.getMaxCaptureRate(), visualizer.getSamplingRate() / captureSize), false, true);
        visualizer.setEnabled(true);
        hasAudioPermissions = true;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                isVisualizerReady = true;
            }
        });
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        if (this.visualizer != null && this.visualizer.equals(visualizer)) {
            float volume = (float) getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC) / getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (isUpdateWaveformEnabled) {
                spectrumAnalyzer.updateWaveform(waveform, volume);
//                onCaptureUpdated(spectrumAnalyzer);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onCaptureUpdated(spectrumAnalyzer);
                    }
                });
            }
        }
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        if (this.visualizer != null && this.visualizer.equals(visualizer)) {
            float volume = (float) getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC) / getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        Log.d(TAG, "volume: " + volume);
            if (isUpdateFftEnabled) {
                spectrumAnalyzer.update(fft, volume);
//                onCaptureUpdated(spectrumAnalyzer);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onCaptureUpdated(spectrumAnalyzer);
                    }
                });
            }
        }
    }

    protected void onCaptureUpdated(SpectrumAnalyzer spectrumAnalyzer) {
        synchronized (intensityValues) {
            float amplitudeLow = spectrumAnalyzer.getAmplitude(0);
            if (amplitudeLow > maxValues[0]) maxValues[0] = amplitudeLow;
            float amplitudeMid = (spectrumAnalyzer.getAmplitude(1) + spectrumAnalyzer.getAmplitude(2)) / 2f;
            if (amplitudeMid > maxValues[1]) maxValues[1] = amplitudeMid;
            float amplitudeHigh = (spectrumAnalyzer.getAmplitude(3) + spectrumAnalyzer.getAmplitude(4) + spectrumAnalyzer.getAmplitude(5)) / 3f;
            if (amplitudeHigh > maxValues[2]) maxValues[2] = amplitudeHigh;

//        Log.d(getClass().getSimpleName(), "amplitude 1: " + amplitudeMid);

            intensityValues[0] = (amplitudeLow / maxValues[0]) * ALPHA + (1f - ALPHA) * intensityValues[0];
            intensityValues[1] = (amplitudeMid / maxValues[1]) * ALPHA + (1f - ALPHA) * intensityValues[1];
            intensityValues[2] = (amplitudeHigh / maxValues[2]) * ALPHA + (1f - ALPHA) * intensityValues[2];
        }

        for (int i = 0; i < maxValues.length; i++) {
            if (maxValues[i] > 64f)
                maxValues[i] *= 0.75f;
        }
    }

    protected boolean isVisualizerReady() {
        return isVisualizerReady;
    }

    public void dispose() {
        super.dispose();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop();
            exoPlayer.release();
        }
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
            visualizer = null;
        }
        if (spectrumAnalyzer != null) spectrumAnalyzer.dispose();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void update() {
        if (isLoading() || !isVisualizerReady) {
            if (getAssets().update()) {
                doneLoading(getAssets());
                setLoading(false);
            }
        }
        getWorld().update();
    }

    @Override
    public void onDrawEye(Eye eye) {
        super.onDrawEye(eye);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
//        musicVisualizerControls.renderAxes(camera);
    }

    public void onMediaPlayerCompleted(MediaPlayer mp) {
        visualizer.setEnabled(false);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK) | event.isFromSource(InputDevice.SOURCE_JOYSTICK)) {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_A || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BUTTON_START)
                context.onCardboardTrigger();
            else if (keyCode == KeyEvent.KEYCODE_BACK) return false;
            return true;
        }
        return false;
    }

    public boolean isUpdateFftEnabled() {
        return isUpdateFftEnabled;
    }

    public void setUpdateFftEnabled(boolean updateFftEnabled) {
        isUpdateFftEnabled = updateFftEnabled;
    }

    public boolean isUpdateWaveformEnabled() {
        return isUpdateWaveformEnabled;
    }

    public void setUpdateWaveformEnabled(boolean updateWaveformEnabled) {
        isUpdateWaveformEnabled = updateWaveformEnabled;
    }

    @Override
    public void onCardboardTrigger() {
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
        if (GdxVr.input.isControllerConnected()) {
            if (controller.appButtonState) {
                pause();
                ((MediaPlayerGame) game).goToSelectionScreen();
            }
        }
    }

    public void processMotionEvent(MotionEvent event, int historyPos) {
        leftJoystick.x = (getCenteredAxis(event, MotionEvent.AXIS_X, historyPos));
        leftJoystick.y = (getCenteredAxis(event, MotionEvent.AXIS_Y, historyPos));
        rightJoystick.x = (getCenteredAxis(event, MotionEvent.AXIS_Z, historyPos));
        rightJoystick.y = (getCenteredAxis(event, MotionEvent.AXIS_RZ, historyPos));
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK) || event.isFromSource(InputDevice.SOURCE_JOYSTICK) || event.isFromSource(InputDevice.SOURCE_DPAD)) {
            final int historySize = event.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                processMotionEvent(event, i);
            }
            processMotionEvent(event, -1);
            return true;
        }
        return false;
    }

    @Override
    public boolean isLoading() {
        return super.isLoading() || !prepared;
    }

    public SpectrumAnalyzer getSpectrumAnalyzer() {
        return spectrumAnalyzer;
    }

    public float getIntensityValue(int i) {
        return intensityValues[i];
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public MainActivity getContext() {
        return context;
    }

    @Override
    public void onLoadError(IOException error) {
        Log.e(TAG, error.getMessage());
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (visualizer == null && exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
            initVisualizer(exoPlayer.getAudioSessionId());
            prepared = true;
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

//    public GvrAudioEngine getGvrAudioEngine() {
//        return gvrAudioEngine;
//    }
}

