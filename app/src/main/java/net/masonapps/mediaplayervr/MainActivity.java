package net.masonapps.mediaplayervr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrView;

import net.masonapps.mediaplayervr.database.VideoOptionsDatabaseHelper;

import org.masonapps.libgdxgooglevr.vr.VrActivity;
import org.masonapps.libgdxgooglevr.vr.VrActivityGVR;

public class MainActivity extends VrActivityGVR {

    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final int RC_PERMISSIONS = 2001;
    private StoragePermissionResultListener listener = null;
    private MediaPlayerGame game;
    private VideoOptionsDatabaseHelper videoOptionsDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = new MediaPlayerGame(this);
        videoOptionsDatabaseHelper = new VideoOptionsDatabaseHelper(this);
        initialize(game);
    }

    @Override
    protected void initGvrView(GvrView gvrView) {
        super.initGvrView(gvrView);
        if (gvrView.setAsyncReprojectionEnabled(true)) {
//            // Async reprojection decouples the app framerate from the display framerate,
//            // allowing immersive interaction even at the throttled clockrates set by
//            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
            Log.d(VrActivity.class.getSimpleName(), "Async Reprojection Enabled");
        }
        gvrView.setMultisampling(1);
        gvrView.setNeckModelEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (videoOptionsDatabaseHelper != null)
            videoOptionsDatabaseHelper.close();
        super.onDestroy();
    }

    public boolean isReadStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestReadStoragePermission(StoragePermissionResultListener listener) {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, RC_PERMISSIONS);
        this.listener = listener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSIONS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (listener != null) {
                listener.onResult(true);
            }
        } else {
            if (listener != null) {
                listener.onResult(false);
            }
        }
    }

    public VideoOptionsDatabaseHelper getVideoOptionsDatabaseHelper() {
        return videoOptionsDatabaseHelper;
    }

    public interface StoragePermissionResultListener {
        void onResult(boolean granted);
    }
}
