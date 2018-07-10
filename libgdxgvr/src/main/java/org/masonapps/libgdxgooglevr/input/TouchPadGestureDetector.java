package org.masonapps.libgdxgooglevr.input;

import com.badlogic.gdx.input.GestureDetector;

/**
 * Created by Bob Mason on 6/1/2018.
 */
public class TouchPadGestureDetector {

    private static final float touchScale = 500f;
    private final GestureDetector detector;

    /**
     * create TouchPadGestureDetector with a default deadzone 20 percent of the touch pad size
     *
     * @param listener handle gesture events scaled from 0.0 to 1.0
     */
    public TouchPadGestureDetector(TouchPadGestureDetector.TouchPadGestureListener listener) {
        this(0.2f, listener);
    }

    /**
     * @param deadzone fraction of touch pad to move before pan starts from 0.0 to 1.0 covering whole touch pad
     * @param listener handle gesture events scaled from 0.0 to 1.0
     */
    public TouchPadGestureDetector(float deadzone, TouchPadGestureDetector.TouchPadGestureListener listener) {
        detector = new GestureDetector(deadzone * touchScale / 2f, 0.4f, 1.1f, 0.15f, new GestureDetector.GestureAdapter() {
            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                listener.touchDown(x / touchScale, y / touchScale);
                return true;
            }

            @Override
            public boolean tap(float x, float y, int count, int button) {
                listener.tap(x / touchScale, y / touchScale);
                return true;
            }

            @Override
            public boolean longPress(float x, float y) {
                listener.longPress(x / touchScale, y / touchScale);
                return true;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                listener.fling(velocityX / touchScale, velocityY / touchScale);
                return true;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                listener.pan(x / touchScale, y / touchScale, deltaX / touchScale, deltaY / touchScale);
                return true;
            }

            @Override
            public boolean panStop(float x, float y, int pointer, int button) {
                listener.panStop(x / touchScale, y / touchScale);
                return true;
            }
        });
    }

    public void onControllerTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                detector.touchDown(event.x * touchScale, event.y * touchScale, 0, 0);
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                detector.touchDragged(event.x * touchScale, event.y * touchScale, 0);
                break;
            case DaydreamTouchEvent.ACTION_UP:
                detector.touchUp(event.x * touchScale, event.y * touchScale, 0, 0);
                break;
        }
    }

    public interface TouchPadGestureListener {

        void touchDown(float x, float y);

        void tap(float x, float y);

        void longPress(float x, float y);

        void fling(float velocityX, float velocityY);

        void pan(float x, float y, float deltaX, float deltaY);

        void panStop(float x, float y);
    }

    public static class TouchPadGestureAdapter implements TouchPadGestureListener {
        @Override
        public void touchDown(float x, float y) {
        }

        @Override
        public void tap(float x, float y) {
        }

        @Override
        public void longPress(float x, float y) {
        }

        @Override
        public void fling(float velocityX, float velocityY) {
        }

        @Override
        public void pan(float x, float y, float deltaX, float deltaY) {
        }

        @Override
        public void panStop(float x, float y) {
        }
    }
}
