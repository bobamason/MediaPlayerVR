package org.masonapps.libgdxgooglevr.input;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;

/**
 * Created by Bob on 2/17/2017.
 */

public class VrInputMultiplexer implements VrInputProcessor {

    private final ArrayList<VrInputProcessor> inputProcessors;

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
        return false;
    }

    @Override
    public Vector2 getHitPoint2D() {
        return null;
    }

    @Override
    public Vector3 getHitPoint3D() {
        return null;
    }

    @Override
    public boolean isCursorOver() {
        for (VrInputProcessor inputProcessor : inputProcessors) {
            if (inputProcessor.isCursorOver()) return true;
        }
        return false;
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
}
