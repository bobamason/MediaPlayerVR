package net.masonapps.mediaplayervr.video;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;

import java.io.IOException;

/**
 * Created by Bob on 12/21/2016.
 */

public class VrVideoPlayerExo extends VrVideoPlayer implements Player.EventListener, ExtractorMediaSource.EventListener {

    public static final String TAG = VrVideoPlayerExo.class.getSimpleName();
    //    @Nullable
//    private GvrAudioProcessor gvrAudioProcessor;
    private ExoPlayer exoPlayer;

    public VrVideoPlayerExo(Context context, Uri uri, int width, int height, Model rectModel, Model sphereModel, Model cylinderModel) {
        super(context, uri, width, height, rectModel, sphereModel, cylinderModel);
    }

    public VrVideoPlayerExo(Context context, Uri uri, int width, int height, DisplayMode displayMode, Model rectModel, Model sphereModel, Model cylinderModel) {
        super(context, uri, width, height, displayMode, rectModel, sphereModel, cylinderModel);
    }

    @Override
    protected void createMediaPlayer() {
        final TrackSelector trackSelector = new DefaultTrackSelector();
        final LoadControl loadControl = new DefaultLoadControl();
        Renderer[] renderers = new Renderer[2];
//        final MediaCodecVideoRendererNoDrop mediaCodecVideoRendererNoDrop = new MediaCodecVideoRendererNoDrop(context, MediaCodecSelector.DEFAULT);
//        mediaCodecVideoRendererNoDrop.setDropLateFrames(false);
//        mediaCodecVideoRendererNoDrop.setDropLateFrames(true);
//        mediaCodecVideoRendererNoDrop.setDropLateMs(30);
//        renderers[0] = mediaCodecVideoRendererNoDrop;
        renderers[0] = new MediaCodecVideoRenderer(context, MediaCodecSelector.DEFAULT);
        renderers[1] = new MediaCodecAudioRenderer(MediaCodecSelector.DEFAULT);

//        gvrAudioProcessor = new GvrAudioProcessor();
//        exoPlayer = ExoPlayerFactory.newSimpleInstance(new GvrRenderersFactory(context, gvrAudioProcessor), trackSelector, loadControl);
//        exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(context), trackSelector, loadControl);
        exoPlayer = ExoPlayerFactory.newInstance(renderers, trackSelector, loadControl);
//        exoPlayer.setVideoSurface(new Surface(videoTexture));
        exoPlayer.sendMessages(new ExoPlayer.ExoPlayerMessage(renderers[0], C.MSG_SET_SURFACE, new Surface(videoTexture)));
        exoPlayer.addListener(VrVideoPlayerExo.this);
    }

    @Override
    public boolean play(final Uri uri) {

        //Wait for the player to be created. (If the Looper thread is busy,
        if (exoPlayer == null) {
            synchronized (lock) {
                while (exoPlayer == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
            }
        }

        prepared = false;
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.prepare(new ExtractorMediaSource(uri, new DefaultDataSourceFactory(context, context.getPackageName()), new DefaultExtractorsFactory(), null, this));

        return true;
    }

    public void update(Matrix4 transform) {
        if (!prepared) {
            return;
        }
        synchronized (this) {
            if (frameAvailable) {
                videoTexture.updateTexImage();
                frameAvailable = false;
            }
        }
    }

    @Override
    public void stop() {
        if (exoPlayer != null && exoPlayer.getPlaybackState() == Player.STATE_READY) {
            exoPlayer.stop();
        }
        prepared = false;
    }

    @Override
    public void pause() {
        // If it is running
        if (prepared) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void resume() {
        // If it is running
        if (prepared) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void dispose() {
        stop();
        if (exoPlayer != null) {
            exoPlayer.removeListener(this);
            exoPlayer.release();
            exoPlayer = null;
//            gvrAudioProcessor = null;
        }
        super.dispose();
    }

    @Override
    public boolean isPlaying() {
        return prepared && exoPlayer.getPlayWhenReady();
    }

    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

//    @Nullable
//    public GvrAudioProcessor getGvrAudioProcessor() {
//        return gvrAudioProcessor;
//    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//        for (int i = 0; i < trackGroups.length; i++) {
//            final TrackGroup trackGroup = trackGroups.get(i);
//            for (int j = 0; j < trackGroup.length; j++) {
//                final Format format = trackGroup.getFormat(j);
////                width = format.width;
////                height = format.height;
//            }
//        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (!isLoading && !prepared) {
            prepared = true;
            updateAspectRatio();
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_READY:
                prepared = true;
                break;
            case Player.STATE_IDLE:
                break;
            case Player.STATE_BUFFERING:
                break;
            case Player.STATE_ENDED:
                if (completionListener != null) {
                    completionListener.onCompletion();
                }
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(TAG, error.getMessage());
        if (errorListener != null) {
            errorListener.onError(error.getMessage());
        }
    }

    @Override
    public void onPositionDiscontinuity() {
//        Log.e(TAG, "onPositionDiscontinuity() called");
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onLoadError(IOException error) {
        Log.e(TAG, error.getMessage());
        if (errorListener != null) {
            errorListener.onError(error.getMessage());
        }
    }

    @Override
    public void seekTo(long position) {
        exoPlayer.seekTo(position);
    }

    @Override
    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return exoPlayer.getDuration();
    }

//    private static final class GvrRenderersFactory extends DefaultRenderersFactory {
//
//        private final GvrAudioProcessor gvrAudioProcessor;
//
//        private GvrRenderersFactory(Context context, GvrAudioProcessor gvrAudioProcessor) {
//            super(context);
//            this.gvrAudioProcessor = gvrAudioProcessor;
//        }
//
//        @Override
//        public AudioProcessor[] buildAudioProcessors() {
//            return new AudioProcessor[]{gvrAudioProcessor};
//        }
//    }
}
