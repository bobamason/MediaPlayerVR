package org.masonapps.libgdxgooglevr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * a movable window with a drag bar at the top with a {@link Table} as the root widget
 * Created by Bob Mason on 10/4/2017.
 */

public class WindowTableVR extends WindowVR {


    protected Table table;

    public WindowTableVR(Batch batch, int virtualPixelWidth, int virtualPixelHeight, String title, WindowVrStyle windowStyle) {
        this(batch, null, virtualPixelWidth, virtualPixelHeight, title, windowStyle);
    }

    public WindowTableVR(Batch batch, int virtualPixelWidth, int virtualPixelHeight, WindowVrStyle windowStyle) {
        this(batch, null, virtualPixelWidth, virtualPixelHeight, windowStyle);
    }

    public WindowTableVR(Batch batch, Skin skin, int virtualPixelWidth, int virtualPixelHeight, WindowVrStyle windowStyle) {
        this(batch, skin, virtualPixelWidth, virtualPixelHeight, "", windowStyle);
    }

    public WindowTableVR(Batch batch, Skin skin, int virtualPixelWidth, int virtualPixelHeight, String title, WindowVrStyle windowStyle) {
        super(batch, virtualPixelWidth, virtualPixelHeight, title, windowStyle);
        table = new Table(skin);
        table.setFillParent(true);
        addActor(table);
        setActivationMovement(0);
    }

    @Override
    public void setSize(int virtualPixelWidth, int virtualPixelHeight) {
        super.setSize(virtualPixelWidth, virtualPixelHeight);
        if (table != null)
            table.invalidate();
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
        resizeToFitTable();
    }

    /**
     * measures the table and sets the size of the {@link VirtualStage} to match
     */
    public void resizeToFitTable() {
        table.setFillParent(false);
        table.layout();
        final int w = Math.round(table.getPrefWidth());
        final int h = Math.round(table.getPrefHeight() + getTitleBarHeight());
        Logger.d("size: " + w + " x " + h);
        table.setFillParent(true);
        setSize(w, h);
        invalidate();
    }

    @Override
    public void setTouchable(boolean touchable) {
        super.setTouchable(touchable);
        table.setTouchable(touchable ? Touchable.enabled : Touchable.childrenOnly);
    }
}
