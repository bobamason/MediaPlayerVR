package org.masonapps.libgdxgooglevr.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Bob Mason on 5/1/2018.
 */
public class VrInputMultiplexer implements VrInputProcessor, Disposable {
    protected final ArrayList<VrInputProcessor> processors;
    @Nullable
    protected VrInputProcessor focusedProcessor;
    protected boolean isCursorOver = false;
    protected Vector2 hitPoint2DPixels = new Vector2();
    protected Vector3 hitPoint3D = new Vector3();

    public VrInputMultiplexer() {
        processors = new ArrayList<>();
    }

    public VrInputMultiplexer(VrInputProcessor... processors) {
        this();
        Collections.addAll(this.processors, processors);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        VrInputProcessor newFocusedProcessor = null;
        isCursorOver = false;
        for (int i = processors.size() - 1; i >= 0; i--) {
            final VrInputProcessor inputProcessor = processors.get(i);
            if (inputProcessor.performRayTest(ray)) {
                isCursorOver = true;
                newFocusedProcessor = inputProcessor;
                break;
            }
        }
        if (focusedProcessor != null && focusedProcessor != newFocusedProcessor) {
            if (focusedProcessor.getHitPoint2D() != null)
                focusedProcessor.touchUp(Math.round(focusedProcessor.getHitPoint2D().x), Math.round(focusedProcessor.getHitPoint2D().y), 0, 0);
            else
                focusedProcessor.touchUp(0, 0, 0, 0);

            if (focusedProcessor instanceof VirtualStage)
                ((VirtualStage) focusedProcessor).isCursorOver = false;
            else if (focusedProcessor instanceof VrUiContainer)
                ((VrUiContainer) focusedProcessor).isCursorOver = false;
        }
        focusedProcessor = newFocusedProcessor;
        if (focusedProcessor != null) {
            if (focusedProcessor.getHitPoint2D() != null)
                hitPoint2DPixels.set(focusedProcessor.getHitPoint2D());

            if (focusedProcessor.getHitPoint3D() != null)
                hitPoint3D.set(focusedProcessor.getHitPoint3D());
        }
        return isCursorOver;
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

    public void addProcessor(VrInputProcessor processor) {
        processors.add(processor);
    }

    public void removeProcessor(VirtualStage stage) {
        processors.remove(stage);
    }

    public void clearProcessors() {
        processors.clear();
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

    @Override
    public void dispose() {
        for (VrInputProcessor processor : processors) {
            if (processor instanceof Disposable)
                ((Disposable) processor).dispose();
        }
        clearProcessors();
    }
}
