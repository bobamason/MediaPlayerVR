package org.masonapps.libgdxgooglevr.vr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pool;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerHandler;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;
import org.masonapps.libgdxgooglevr.utils.Vecs;

import java.util.ArrayList;

/**
 * Created by Bob on 12/29/2016.
 * based on AndroidInput originally written by mzechner and jshapcot
 */

public class VrAndroidInput extends AndroidInput implements DaydreamControllerInputListener {

    private final Application app;
    private final Context context;
    private final ArmModel armModel;
    protected Quaternion controllerOrientation = new Quaternion();
    protected boolean isControllerConnected = false;
    private Vector3 controllerPosition = new Vector3();
    private Ray inputRay = new Ray();
    private DaydreamControllerHandler daydreamControllerHandler;
    private Pool<KeyEvent> usedKeyEvents = new Pool<KeyEvent>(16, 1000) {
        protected KeyEvent newObject() {
            return new KeyEvent();
        }
    };
    private Pool<TouchEvent> usedTouchEvents = new Pool<TouchEvent>(16, 1000) {
        protected TouchEvent newObject() {
            return new TouchEvent();
        }
    };
    private ArrayList<View.OnKeyListener> keyListeners = new ArrayList();
    private ArrayList<KeyEvent> keyEvents = new ArrayList();
    private ArrayList<TouchEvent> touchEvents = new ArrayList();
    private boolean keyboardAvailable;
    private boolean requestFocus = true;
    private int keyCount = 0;
    private boolean[] keys = new boolean[SUPPORTED_KEYS];
    private boolean keyJustPressed = false;
    private boolean[] justPressedKeys = new boolean[SUPPORTED_KEYS];
    private String text = null;
    private TextInputListener textListener = null;
    private Handler handle;
    //        private final AndroidMultiTouchHandler touchHandler;
    private int sleepTime = 0;
    private boolean catchBack = false;
    private boolean catchMenu = false;
    private boolean justTouched = false;
    private InputProcessor processor;
    //    private final AndroidOnscreenKeyboard onscreenKeyboard;
    private long currentEventTimeStamp = System.nanoTime();
    private Controller controller;
    private boolean isInputProcessorTouched = false;
    private GridPoint2 touch = new GridPoint2(-1, -1);
    private GridPoint2 lastTouch = new GridPoint2(-1, -1);

    private VrAndroidInput(Application application, Context context, AndroidApplicationConfiguration configuration) {
        super(application, context, null, configuration);
//        this.onscreenKeyboard = new AndroidOnscreenKeyboard(context, new Handler(), this);
        handle = new Handler();
        daydreamControllerHandler = new DaydreamControllerHandler();
        this.app = application;
        this.context = context;
        armModel = ArmModel.getInstance();
    }

    public static VrAndroidInput newInstance(VrActivity vrActivity) {
        final AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        configuration.useGyroscope = false;
        return new VrAndroidInput(vrActivity, vrActivity.getContext(), configuration);
    }

    @Override
    public float getAccelerometerX() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public float getAccelerometerY() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public float getAccelerometerZ() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public float getGyroscopeX() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public float getGyroscopeY() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public float getGyroscopeZ() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public void getTextInput(final TextInputListener listener, final String title, final String text, final String hint) {
        handle.post(new Runnable() {
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(title);
                final EditText input = new EditText(context);
                input.setHint(hint);
                input.setText(text);
                input.setSingleLine();
                alert.setView(input);
                alert.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                listener.input(input.getText().toString());
                            }
                        });
                    }
                });
                alert.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                listener.canceled();
                            }
                        });
                    }
                });
                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                listener.canceled();
                            }
                        });
                    }
                });
                alert.show();
            }
        });
    }

    @Override
    public int getX() {
        return getX(0);
    }

    @Override
    public int getY() {
        return getY(0);
    }

    @Override
    public int getX(int pointer) {
        return pointer == 0 ? touch.x : -1;
    }

    @Override
    public int getY(int pointer) {
        return pointer == 0 ? touch.y : -1;
    }

    public boolean isTouched(int pointer) {
        return isInputProcessorTouched && pointer == 0;
    }

    @Override
    public synchronized boolean isKeyPressed(int key) {
        if (key == Input.Keys.ANY_KEY) {
            return keyCount > 0;
        }
        if (key < 0 || key >= SUPPORTED_KEYS) {
            return false;
        }
        return keys[key];
    }

    @Override
    public synchronized boolean isKeyJustPressed(int key) {
        if (key == Input.Keys.ANY_KEY) {
            return keyJustPressed;
        }
        if (key < 0 || key >= SUPPORTED_KEYS) {
            return false;
        }
        return justPressedKeys[key];
    }

    @Override
    public boolean isTouched() {
        return isTouched(0);
    }

    public void processEvents() {
        synchronized (this) {
            updateInputRay();
            if (processor instanceof VrInputProcessor) {
                ((VrInputProcessor) processor).performRayTest(inputRay);
                if (((VrInputProcessor) processor).isCursorOver()) {
//                    Log.d(VrAndroidInput.class.getSimpleName(), event.toString());
                    final Vector2 hitPoint2D = ((VrInputProcessor) processor).getHitPoint2D();
                    final int x = (int) hitPoint2D.x;
                    final int y = (int) hitPoint2D.y;
                    lastTouch.set(touch);
                    touch.set(x, y);
                    if (controller.clickButtonState && !isInputProcessorTouched) {
                        postTouchEvent(TouchEvent.TOUCH_DOWN, x, y);
                        isInputProcessorTouched = true;
                    } else if (!controller.clickButtonState && isInputProcessorTouched) {
                        postTouchEvent(TouchEvent.TOUCH_UP, x, y);
                        isInputProcessorTouched = false;
                    } else {
                        postTouchEvent(isInputProcessorTouched ? TouchEvent.TOUCH_DRAGGED : TouchEvent.TOUCH_MOVED, x, y);
                    }
                } else {
                    if (isInputProcessorTouched) {
                        postTouchEvent(TouchEvent.TOUCH_UP, 0, 0);
                    }
                    isInputProcessorTouched = false;
                }
            }

            justTouched = false;
            if (keyJustPressed) {
                keyJustPressed = false;
                for (int i = 0; i < justPressedKeys.length; i++) {
                    justPressedKeys[i] = false;
                }
            }

            if (processor != null) {
                final InputProcessor processor = this.processor;

                int len = keyEvents.size();
                for (int i = 0; i < len; i++) {
                    KeyEvent e = keyEvents.get(i);
                    currentEventTimeStamp = e.timeStamp;
                    switch (e.type) {
                        case KeyEvent.KEY_DOWN:
                            processor.keyDown(e.keyCode);
                            keyJustPressed = true;
                            justPressedKeys[e.keyCode] = true;
                            break;
                        case KeyEvent.KEY_UP:
                            processor.keyUp(e.keyCode);
                            break;
                        case KeyEvent.KEY_TYPED:
                            processor.keyTyped(e.keyChar);
                    }
                    usedKeyEvents.free(e);
                }

                len = touchEvents.size();
                for (int i = 0; i < len; i++) {
                    TouchEvent e = touchEvents.get(i);
                    currentEventTimeStamp = e.timeStamp;
                    switch (e.type) {
                        case TouchEvent.TOUCH_DOWN:
                            processor.touchDown(e.x, e.y, e.pointer, e.button);
                            justTouched = true;
                            break;
                        case TouchEvent.TOUCH_UP:
                            processor.touchUp(e.x, e.y, e.pointer, e.button);
                            break;
                        case TouchEvent.TOUCH_DRAGGED:
                            processor.touchDragged(e.x, e.y, e.pointer);
                            break;
                        case TouchEvent.TOUCH_MOVED:
                            processor.mouseMoved(e.x, e.y);
                            break;
                        case TouchEvent.TOUCH_SCROLLED:
                            processor.scrolled(e.scrollAmount);
                    }
                    usedTouchEvents.free(e);
                }
            } else {
                int len = touchEvents.size();
                for (int i = 0; i < len; i++) {
                    TouchEvent e = touchEvents.get(i);
                    if (e.type == TouchEvent.TOUCH_DOWN) justTouched = true;
                    usedTouchEvents.free(e);
                }

                len = keyEvents.size();
                for (int i = 0; i < len; i++) {
                    usedKeyEvents.free(keyEvents.get(i));
                }
            }

            if (touchEvents.size() == 0) {
//                for (int i = 0; i < deltaX.length; i++) {
//                    deltaX[0] = 0;
//                    deltaY[0] = 0;
//                }
            }

            keyEvents.clear();
            touchEvents.clear();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        return false;
    }

    //    /** Called in {@link AndroidLiveWallpaperService} on tap
//     * @param x
//     * @param y */
    public void onTap(int x, int y) {
        postTap(x, y);
    }

    //    /** Called in {@link AndroidLiveWallpaperService} on drop
//     * @param x
//     * @param y */
    public void onDrop(int x, int y) {
        postTap(x, y);
    }

    protected void postTap(int x, int y) {
        synchronized (this) {
            TouchEvent event = usedTouchEvents.obtain();
            event.timeStamp = System.nanoTime();
            event.pointer = 0;
            event.x = x;
            event.y = y;
            event.type = TouchEvent.TOUCH_DOWN;
            touchEvents.add(event);

            event = usedTouchEvents.obtain();
            event.timeStamp = System.nanoTime();
            event.pointer = 0;
            event.x = x;
            event.y = y;
            event.type = TouchEvent.TOUCH_UP;
            touchEvents.add(event);
        }
//        Gdx.app.getGraphics().requestRendering();
    }

    @Override
    public boolean onKey(View v, int keyCode, android.view.KeyEvent e) {
        for (int i = 0, n = keyListeners.size(); i < n; i++)
            if (keyListeners.get(i).onKey(v, keyCode, e)) return true;

        synchronized (this) {
            KeyEvent event = null;

            if (e.getKeyCode() == android.view.KeyEvent.KEYCODE_UNKNOWN && e.getAction() == android.view.KeyEvent.ACTION_MULTIPLE) {
                String chars = e.getCharacters();
                for (int i = 0; i < chars.length(); i++) {
                    event = usedKeyEvents.obtain();
                    event.timeStamp = System.nanoTime();
                    event.keyCode = 0;
                    event.keyChar = chars.charAt(i);
                    event.type = KeyEvent.KEY_TYPED;
                    keyEvents.add(event);
                }
                return false;
            }

            char character = (char) e.getUnicodeChar();
            // Android doesn't report a unicode char for back space. hrm...
            if (keyCode == 67) character = '\b';
            if (e.getKeyCode() < 0 || e.getKeyCode() >= SUPPORTED_KEYS) {
                return false;
            }

            switch (e.getAction()) {
                case android.view.KeyEvent.ACTION_DOWN:
                    event = usedKeyEvents.obtain();
                    event.timeStamp = System.nanoTime();
                    event.keyChar = 0;
                    event.keyCode = e.getKeyCode();
                    event.type = KeyEvent.KEY_DOWN;

                    // Xperia hack for circle key. gah...
                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
                        keyCode = Keys.BUTTON_CIRCLE;
                        event.keyCode = keyCode;
                    }

                    keyEvents.add(event);
                    if (!keys[event.keyCode]) {
                        keyCount++;
                        keys[event.keyCode] = true;
                    }
                    break;
                case android.view.KeyEvent.ACTION_UP:
                    long timeStamp = System.nanoTime();
                    event = usedKeyEvents.obtain();
                    event.timeStamp = timeStamp;
                    event.keyChar = 0;
                    event.keyCode = e.getKeyCode();
                    event.type = KeyEvent.KEY_UP;
                    // Xperia hack for circle key. gah...
                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
                        keyCode = Keys.BUTTON_CIRCLE;
                        event.keyCode = keyCode;
                    }
                    keyEvents.add(event);

                    event = usedKeyEvents.obtain();
                    event.timeStamp = timeStamp;
                    event.keyChar = character;
                    event.keyCode = 0;
                    event.type = KeyEvent.KEY_TYPED;
                    keyEvents.add(event);

                    if (keyCode == Keys.BUTTON_CIRCLE) {
                        if (keys[Keys.BUTTON_CIRCLE]) {
                            keyCount--;
                            keys[Keys.BUTTON_CIRCLE] = false;
                        }
                    } else {
                        if (keys[e.getKeyCode()]) {
                            keyCount--;
                            keys[e.getKeyCode()] = false;
                        }
                    }
            }
            app.getGraphics().requestRendering();
        }

        // circle button on Xperia Play shouldn't need catchBack == true
        if (keyCode == Keys.BUTTON_CIRCLE) return true;
        if (catchBack && keyCode == android.view.KeyEvent.KEYCODE_BACK) return true;
        if (catchMenu && keyCode == android.view.KeyEvent.KEYCODE_MENU) return true;
        return false;
    }

    @Override
    public void setOnscreenKeyboardVisible(final boolean visible) {
        handle.post(new Runnable() {
            public void run() {
                InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (visible) {
                    View view = ((AndroidGraphics) app.getGraphics()).getView();
                    view.setFocusable(true);
                    view.setFocusableInTouchMode(true);
                    manager.showSoftInput(((AndroidGraphics) app.getGraphics()).getView(), 0);
                } else {
                    manager.hideSoftInputFromWindow(((AndroidGraphics) app.getGraphics()).getView().getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    public boolean isCatchBackKey() {
        return catchBack;
    }

    @Override
    public void setCatchBackKey(boolean catchBack) {
        this.catchBack = catchBack;
    }

    @Override
    public boolean isCatchMenuKey() {
        return catchMenu;
    }

    @Override
    public void setCatchMenuKey(boolean catchMenu) {
        this.catchMenu = catchMenu;
    }

    @Override
    public void vibrate(int milliseconds) {
        vibrator.vibrate(milliseconds);
    }

    @Override
    public void vibrate(long[] pattern, int repeat) {
        vibrator.vibrate(pattern, repeat);
    }

    @Override
    public void cancelVibrate() {
        vibrator.cancel();
    }

    @Override
    public boolean justTouched() {
        return justTouched;
    }

    @Override
    public boolean isButtonPressed(int button) {
        return super.isButtonPressed(button);
    }

    @Override
    public float getAzimuth() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public float getPitch() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public float getRoll() {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public void getRotationMatrix(float[] matrix) {
        throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
    }

    @Override
    public InputProcessor getInputProcessor() {
        return this.processor;
    }

    public void setInputProcessor(InputProcessor processor) {
        synchronized (this) {
            this.processor = processor;
        }
    }

    public VrInputProcessor getVrInputProcessor() {
        return (VrInputProcessor) this.processor;
    }

    @Override
    public boolean isPeripheralAvailable(Peripheral peripheral) {
        if (peripheral == Peripheral.Accelerometer) return false;
        if (peripheral == Peripheral.Gyroscope) return false;
        if (peripheral == Peripheral.Compass) return false;
        if (peripheral == Peripheral.HardwareKeyboard) return keyboardAvailable;
        if (peripheral == Peripheral.OnscreenKeyboard) return true;
        if (peripheral == Peripheral.Vibrator)
            return (Build.VERSION.SDK_INT >= 11 && vibrator != null) ? vibrator.hasVibrator() : vibrator != null;
        return false;
    }

    @Override
    public int getRotation() {
        return 90;
    }

    @Override
    public Orientation getNativeOrientation() {
        return Orientation.Landscape;
    }

    @Override
    public boolean isCursorCatched() {
        return false;
    }

    @Override
    public void setCursorCatched(boolean catched) {
    }

    @Override
    public int getDeltaX() {
        return getDeltaX(0);
    }

    @Override
    public int getDeltaX(int pointer) {
        return pointer == 0 ? touch.x - lastTouch.x : 0;
    }

    @Override
    public int getDeltaY() {
        return getDeltaY(0);
    }

    @Override
    public int getDeltaY(int pointer) {
        return pointer == 0 ? touch.y - lastTouch.y : 0;
    }

    @Override
    public void setCursorPosition(int x, int y) {
    }

    @Override
    public long getCurrentEventTime() {
        return currentEventTimeStamp;
    }

    public void addKeyListener(View.OnKeyListener listener) {
        keyListeners.add(listener);
    }

    public void onPause() {
        daydreamControllerHandler.removeListener(this);
    }

    public void onResume() {
        daydreamControllerHandler.addListener(this);
    }

    public void onCardboardTrigger() {
        if (processor instanceof VrInputProcessor) {
            final VrInputProcessor vrInputProcessor = (VrInputProcessor) this.processor;
            if (vrInputProcessor.performRayTest(inputRay)) {
                final Vector2 hitPoint2D = vrInputProcessor.getHitPoint2D();
                postTap((int) hitPoint2D.x, (int) hitPoint2D.y);
            }
        }
    }

    public void setProcessor(InputProcessor processor) {
        if (processor != null && !(processor instanceof VrInputProcessor))
            throw new RuntimeException("processor must implement VrInputProcessor");
        this.processor = processor;
    }

    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        if (connectionState == Controller.ConnectionStates.CONNECTED) {
            isControllerConnected = true;
            armModel.updateHeadDirection(GdxVr.graphics.getForward());
            armModel.onControllerUpdate(controller);
            controllerOrientation.set(controller.orientation.x, controller.orientation.y, controller.orientation.z, controller.orientation.w);
            controllerPosition.set(armModel.pointerPosition);
        } else {
            isControllerConnected = false;
        }
        daydreamControllerHandler.process(controller, connectionState);
    }

    private void postTouchEvent(int type, int x, int y) {
        TouchEvent event = usedTouchEvents.obtain();
        event.timeStamp = System.nanoTime();
        event.pointer = 0;
        event.x = x;
        event.y = y;
        event.button = 0;
        event.type = type;
        touchEvents.add(event);
    }

    public Ray getInputRay() {
        return inputRay;
    }

    protected void updateInputRay() {
        if (isControllerConnected && controller != null) {
            inputRay.origin.set(0, -0.002f, -0.053f).mul(controllerOrientation).add(GdxVr.app.getVrApplicationAdapter().getVrCamera().position).add(armModel.pointerPosition);
            inputRay.direction.set(ArmModel.WORLD_FORWARD).mul(armModel.pointerRotation);
        } else {
            inputRay.origin.set(GdxVr.app.getVrApplicationAdapter().getVrCamera().position);
            inputRay.direction.set(GdxVr.app.getVrApplicationAdapter().getForwardVector());
            Vecs.freeAll();
        }
    }

    public ArmModel getArmModel() {
        return armModel;
    }


    public Vector3 getControllerPosition() {
        return controllerPosition;
    }

    public Quaternion getControllerOrientation() {
        return controllerOrientation;
    }

    public boolean isControllerConnected() {
        return isControllerConnected;
    }

    @Override
    public void onConnectionStateChange(int connectionState) {
        isControllerConnected = connectionState == Controller.ConnectionStates.CONNECTED;
    }

    @Override
    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
    }

    @Override
    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public DaydreamControllerHandler getDaydreamControllerHandler() {
        return daydreamControllerHandler;
    }

    static class KeyEvent {
        static final int KEY_DOWN = 0;
        static final int KEY_UP = 1;
        static final int KEY_TYPED = 2;

        long timeStamp;
        int type;
        int keyCode;
        char keyChar;
    }

    static class TouchEvent {
        static final int TOUCH_DOWN = 0;
        static final int TOUCH_UP = 1;
        static final int TOUCH_DRAGGED = 2;
        static final int TOUCH_SCROLLED = 3;
        static final int TOUCH_MOVED = 4;

        long timeStamp;
        int type;
        int x;
        int y;
        int scrollAmount;
        int button;
        int pointer;
    }
}
