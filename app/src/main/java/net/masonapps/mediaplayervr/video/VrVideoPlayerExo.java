package net.masonapps.mediaplayervr.video;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.IOException;

/**
 * Created by Bob on 12/21/2016.
 */

public class VrVideoPlayerExo extends VrVideoPlayer implements ExoPlayer.EventListener, ExtractorMediaSource.EventListener {

    public static final String TAG = VrVideoPlayerExo.class.getSimpleName();
    private ExoPlayer exoPlayer;

    public VrVideoPlayerExo(Context context, Uri uri, int width, int height) {
        super(context, uri, width, height);
    }

    public VrVideoPlayerExo(Context context, Uri uri, int width, int height, VideoMode videoMode) {
        super(context, uri, width, height, videoMode);
    }

    @Override
    protected void createMediaPlayer() {
        final TrackSelector trackSelector = new DefaultTrackSelector();
//                    final LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE * 4),DefaultLoadControl.DEFAULT_MIN_BUFFER_MS / 8, DefaultLoadControl.DEFAULT_MAX_BUFFER_MS * 8, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        final LoadControl loadControl = new DefaultLoadControl();
        Renderer[] renderers = new Renderer[2];
        final MediaCodecVideoRendererNoDrop mediaCodecVideoRendererNoDrop = new MediaCodecVideoRendererNoDrop(context, MediaCodecSelector.DEFAULT);
        mediaCodecVideoRendererNoDrop.setDropLateFrames(false);
        mediaCodecVideoRendererNoDrop.setDropLateMs(100);
        renderers[0] = mediaCodecVideoRendererNoDrop;
        renderers[1] = new MediaCodecAudioRenderer(MediaCodecSelector.DEFAULT);
        exoPlayer = ExoPlayerFactory.newInstance(renderers, trackSelector, loadControl);
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

    public void update() {
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
        if (exoPlayer != null && exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
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

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        for (int i = 0; i < trackGroups.length; i++) {
            final TrackGroup trackGroup = trackGroups.get(i);
            for (int j = 0; j < trackGroup.length; j++) {
                final Format format = trackGroup.getFormat(j);
//                width = format.width;
//                height = format.height;
            }
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (!isLoading) {
            prepared = true;
            updateAspectRatio();
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_READY:
                break;
            case ExoPlayer.STATE_IDLE:
                break;
            case ExoPlayer.STATE_BUFFERING:
                break;
            case ExoPlayer.STATE_ENDED:
                if (completionListener != null) {
                    completionListener.onCompletion();
                }
                break;
        }
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
        Log.e(TAG, "onPositionDiscontinuity() called");
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
}
