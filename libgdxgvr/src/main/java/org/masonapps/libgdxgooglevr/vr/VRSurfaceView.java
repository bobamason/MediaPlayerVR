package org.masonapps.libgdxgooglevr.vr;

import android.content.Context;
import android.util.AttributeSet;

import com.google.vr.ndk.base.GvrSurfaceView;

/**
 * Created by Bob on 6/6/2017.
 */

public class VRSurfaceView extends GvrSurfaceView {
    public VRSurfaceView(Context context) {
        super(context);
    }

    public VRSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
