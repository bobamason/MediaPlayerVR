package org.masonapps.libgdxgooglevr.input;

import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.controller.Controller;

import java.util.ArrayList;

/**
 * Created by Bob on 1/9/2017.
 */

public class DaydreamControllerHandler {

    private ArrayList<DaydreamControllerInputListener> listeners;
    private int currentConnectionState = Controller.ConnectionStates.DISCONNECTED;
    private boolean clickButtonState = false;
    private boolean appButtonState = false;
    private boolean volumeUpButtonState = false;
    private boolean volumeDownButtonState = false;
    private boolean isTouching = false;

    public DaydreamControllerHandler() {
        listeners = new ArrayList<>();
    }

    public void process(Controller controller, int connectionState) {
        if (currentConnectionState != connectionState) {
            currentConnectionState = connectionState;
            for (DaydreamControllerInputListener listener : listeners) {
                listener.onConnectionStateChange(connectionState);
            }
        }

        if (connectionState == Controller.ConnectionStates.CONNECTED) {
            if (controller.clickButtonState) {
                if (!clickButtonState) {
                    postButtonEvent(controller, DaydreamButtonEvent.ACTION_DOWN, DaydreamButtonEvent.BUTTON_TOUCHPAD);
                }
                clickButtonState = true;
            } else {
                if (clickButtonState) {
                    postButtonEvent(controller, DaydreamButtonEvent.ACTION_UP, DaydreamButtonEvent.BUTTON_TOUCHPAD);
                }
                clickButtonState = false;
            }
            if (controller.appButtonState) {
                if (!appButtonState) {
                    postButtonEvent(controller, DaydreamButtonEvent.ACTION_DOWN, DaydreamButtonEvent.BUTTON_APP);
                }
                appButtonState = true;
            } else {
                if (appButtonState) {
                    postButtonEvent(controller, DaydreamButtonEvent.ACTION_UP, DaydreamButtonEvent.BUTTON_APP);
                }
                appButtonState = false;
            }
            if (controller.volumeUpButtonState) {
                if (!volumeUpButtonState) {
                    postButtonEvent(controller, DaydreamButtonEvent.ACTION_DOWN, DaydreamButtonEvent.BUTTON_VOLUME_UP);
                }
                volumeUpButtonState = true;
            } else {
                if (volumeUpButtonState) {
                    postButtonEvent(controller, DaydreamButtonEvent.ACTION_UP, DaydreamButtonEvent.BUTTON_VOLUME_UP);
                }
                volumeUpButtonState = false;
            }
            if (controller.volumeDownButtonState) {
                if (!volumeDownButtonState) {
                    postButtonEvent(controller, DaydreamButtonEvent.ACTION_DOWN, DaydreamButtonEvent.BUTTON_VOLUME_DOWN);
                }
                volumeDownButtonState = true;
            } else {
                if (volumeDownButtonState) {
                    postButtonEvent(controller, DaydreamButtonEvent.ACTION_UP, DaydreamButtonEvent.BUTTON_VOLUME_DOWN);
                }
                volumeDownButtonState = false;
            }
            if (controller.isTouching) {
                if (!isTouching) {
                    postTouchpadEvent(controller, DaydreamTouchEvent.ACTION_DOWN, controller.touch.x, controller.touch.y);
                }
                isTouching = true;
                postTouchpadEvent(controller, DaydreamTouchEvent.ACTION_MOVE, controller.touch.x, controller.touch.y);
            } else {
                if (isTouching) {
                    postTouchpadEvent(controller, DaydreamTouchEvent.ACTION_UP, controller.touch.x, controller.touch.y);
                }
                isTouching = false;
            }
        }
    }

    private void postButtonEvent(Controller controller, int action, int button) {
        DaydreamButtonEvent event = Pools.obtain(DaydreamButtonEvent.class);
        event.action = action;
        event.button = button;
        for (DaydreamControllerInputListener listener : listeners) {
            listener.onButtonEvent(controller, event);
        }
        Pools.free(event);

    }

    private void postTouchpadEvent(Controller controller, int action, float x, float y) {
        DaydreamTouchEvent event = Pools.obtain(DaydreamTouchEvent.class);
        event.action = action;
        event.x = x;
        event.y = y;
        for (DaydreamControllerInputListener listener : listeners) {
            listener.onTouchPadEvent(controller, event);
        }
        Pools.free(event);
    }

    public void addListener(DaydreamControllerInputListener listener) {
        listeners.add(listener);
        if (currentConnectionState == Controller.ConnectionStates.CONNECTED)
            listener.onConnectionStateChange(currentConnectionState);
    }

    public void removeListener(DaydreamControllerInputListener listener) {
        listeners.remove(listener);
    }
}
