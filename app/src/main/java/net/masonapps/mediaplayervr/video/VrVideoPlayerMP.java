package net.masonapps.mediaplayervr.video;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.badlogic.gdx.graphics.g3d.Model;

import java.io.IOException;

/**
 * Created by Bob on 12/21/2016.
 */

public class VrVideoPlayerMP extends VrVideoPlayer implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener {

    public static final String TAG = VrVideoPlayerMP.class.getSimpleName();
    private MediaPlayer player;

    public VrVideoPlayerMP(Context context, Uri uri, int width, int height, Model rectModel, Model sphereModel, Model cylinderModel) {
        super(context, uri, width, height, rectModel, sphereModel, cylinderModel);
    }

    public VrVideoPlayerMP(Context context, Uri uri, int width, int height, DisplayMode displayMode, Model rectModel, Model sphereModel, Model cylinderModel) {
        super(context, uri, width, height, displayMode, rectModel, sphereModel, cylinderModel);
    }

    @Override
    protected void createMediaPlayer() {
        player = new MediaPlayer();
    }

    @Override
    public boolean play(final Uri uri) {

        //Wait for the player to be created. (If the Looper thread is busy,
        if (player == null) {
            synchronized (lock) {
                while (player == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
            }
        }

        prepared = false;
        player.setSurface(new Surface(videoTexture));
        try {
            player.setDataSource(context, uri);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            if (errorListener != null) {
                errorListener.onError(e.getMessage());
            }
            return false;
        }
        player.setOnPreparedListener(mp -> {
            prepared = true;
            mp.start();
        });
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        player.setOnInfoListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnBufferingUpdateListener(this);
        player.prepareAsync();

        return true;
    }

    @Override
    public void stop() {
        if (player != null && prepared) {
            player.stop();
        }
        prepared = false;
    }

    @Override
    public void pause() {
        // If it is running
        if (prepared) {
            player.pause();
        }
    }

    @Override
    public void resume() {
        // If it is running
        if (prepared) {
            player.start();
        }
    }

    @Override
    public void dispose() {
        stop();
        if (player != null) {
            player.release();
        }
        super.dispose();
    }

    @Override
    public boolean isPlaying() {
        return prepared && player.isPlaying();
    }

    @Override
    public void seekTo(long position) {
        player.seekTo((int) position);
    }

    @Override
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        final String msg = "Error occured (" + i + ", " + i1 + ")";
        Log.e(TAG, msg);
        if (errorListener != null) {
            errorListener.onError(msg);
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (completionListener != null) {
            completionListener.onCompletion();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }
}
