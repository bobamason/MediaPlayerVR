package org.masonapps.libgdxgooglevr.input;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;

/**
 * Created by Bob on 2/17/2017.
 */

public class VrInputMultiplexer implements VrInputProcessor {

    private final ArrayList<VrInputProcessor> inputProcessors;
    private boolean isCursorOver = false;
    private Vector2 hitPoint2DPixels = new Vector2();
    private Vector3 hitPoint3D = new Vector3();
    @Nullable
    private VrInputProcessor focusedProcessor;

    public VrInputMultiplexer() {
        inputProcessors = new ArrayList<>();
    }

    public VrInputMultiplexer(VrInputProcessor... processors) {
        this();
        for (VrInputProcessor processor : processors) {
            inputProcessors.add(processor);
        }
    }

    @Override
    public boolean performRayTest(Ray ray) {
        for (VrInputProcessor inputProcessor : inputProcessors) {
            if (inputProcessor.performRayTest(ray)) {
                focusedProcessor = inputProcessor;
                hitPoint2DPixels.set(inputProcessor.getHitPoint2D());
                hitPoint3D.set(inputProcessor.getHitPoint3D());
                isCursorOver = true;
                return true;
            }
        }
        focusedProcessor = null;
        isCursorOver = false;
        return false;
    }

    @Override
    public Vector2 getHitPoint2D() {
        return hitPoint2DPixels;
    }

    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint3D;
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    public void addProcessor(VrInputProcessor inputProcessor) {
        inputProcessors.add(inputProcessor);
    }

    public void removeProcessor(VrInputProcessor inputProcessor) {
        inputProcessors.remove(inputProcessor);
    }

    public void clearProcessors() {
        inputProcessors.clear();
    }

    @Override
    public boolean keyDown(int keycode) {
        return focusedProcessor != null && focusedProcessor.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return focusedProcessor != null && focusedProcessor.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return focusedProcessor != null && focusedProcessor.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return focusedProcessor != null && focusedProcessor.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return focusedProcessor != null && focusedProcessor.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return focusedProcessor != null && focusedProcessor.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return focusedProcessor != null && focusedProcessor.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
        return focusedProcessor != null && focusedProcessor.scrolled(amount);
    }
}
