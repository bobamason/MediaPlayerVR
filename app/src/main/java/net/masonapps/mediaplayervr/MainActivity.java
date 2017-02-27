package net.masonapps.mediaplayervr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.vr.sdk.base.AndroidCompat;

import net.masonapps.mediaplayervr.database.VideoOptionsDatabaseHelper;

import org.masonapps.libgdxgooglevr.vr.VrActivity;

public class MainActivity extends VrActivity {

    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final int RC_PERMISSIONS = 2001;
    private StoragePermissionResultListener listener = null;
    private MediaPlayerGame game;
    private VideoOptionsDatabaseHelper videoOptionsDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidCompat.setVrModeEnabled(this, true);
        game = new MediaPlayerGame(this);
        videoOptionsDatabaseHelper = new VideoOptionsDatabaseHelper(this);
        initialize(game);
        getGvrView().setMultisampling(2);
    }

    @Override
    protected void onDestroy() {
        if (videoOptionsDatabaseHelper != null)
            videoOptionsDatabaseHelper.close();
        super.onDestroy();
    }

    public boolean isReadStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestReadStoragePermission(StoragePermissionResultListener listener) {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, RECORD_AUDIO}, RC_PERMISSIONS);
        this.listener = listener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSIONS && grantResults.length >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
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
