package org.masonapps.libgdxgooglevr.input;

import com.google.vr.sdk.controller.Controller;

/**
 * Created by Bob on 1/9/2017.
 */

public interface DaydreamControllerInputListener {

    void onConnectionStateChange(int connectionState);

    void onButtonEvent(Controller controller, DaydreamButtonEvent event);

    void onTouchPadEvent(Controller controller, DaydreamTouchEvent event);
}
