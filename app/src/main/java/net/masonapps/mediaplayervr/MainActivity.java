package net.masonapps.mediaplayervr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.vr.sdk.base.AndroidCompat;

import org.masonapps.libgdxgooglevr.vr.VrActivity;

public class MainActivity extends VrActivity {

    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_READ_EXTERNAL_STORAGE = 2001;
    private StoragePermissionResultListener listener = null;
    private MediaPlayerGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidCompat.setVrModeEnabled(this, true);
        game = new MediaPlayerGame(this);
        initialize(game);
    }

    public boolean isReadStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestReadStoragePermission(StoragePermissionResultListener listener) {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, RC_READ_EXTERNAL_STORAGE);
        this.listener = listener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_READ_EXTERNAL_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (listener != null) {
                listener.onResult(true);
            }
        } else {
            if (listener != null) {
                listener.onResult(false);
            }
        }
    }

    public interface StoragePermissionResultListener {
        void onResult(boolean granted);
    }
}
