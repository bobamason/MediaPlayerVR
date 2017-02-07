package org.masonapps.libgdxgooglevr.vr;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.google.vr.sdk.base.GvrView;

import javax.microedition.khronos.egl.EGL10;


/**
 * Created by Bob on 10/9/2016.
 */

public class VrSurfaceView extends GvrView {

    private static final boolean DEBUG = false;
    static String TAG = VrSurfaceView.class.getSimpleName();

    public VrSurfaceView(Context context) {
        super(context);
    }

    public VrSurfaceView(Context context, boolean translucent, int depth, int stencil) {
        super(context);
    }

    static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {

        // add this line, the IME can show the selectable words when use chinese input method editor.
        if (outAttrs != null) {
            outAttrs.imeOptions = outAttrs.imeOptions | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        }

        return new BaseInputConnection(VrSurfaceView.this, false) {
            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                int sdkVersion = android.os.Build.VERSION.SDK_INT;
                if (sdkVersion >= 16) {
                    /*
                     * In Jelly Bean, they don't send key events for delete. Instead, they send beforeLength = 1, afterLength = 0. So,
					 * we'll just simulate what it used to do.
					 */
                    if (beforeLength == 1 && afterLength == 0) {
                        sendDownUpKeyEventForBackwardCompatibility(KeyEvent.KEYCODE_DEL);
                        return true;
                    }
                }
                return super.deleteSurroundingText(beforeLength, afterLength);
            }

            @TargetApi(16)
            private void sendDownUpKeyEventForBackwardCompatibility(final int code) {
                final long eventTime = SystemClock.uptimeMillis();
                super.sendKeyEvent(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, code, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
                super.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime, KeyEvent.ACTION_UP, code, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
            }
        };
    }
}
