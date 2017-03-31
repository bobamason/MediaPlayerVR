package org.masonapps.libgdxgooglevr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Created by Bob on 3/31/2017.
 */

public class TableVR extends VirtualStage {

    protected final Table table;

    public TableVR(Batch batch, int tableWidth, int tableHeight) {
        this(batch, null, tableWidth, tableHeight);
    }

    public TableVR(Batch batch, Skin skin, int tableWidth, int tableHeight) {
        super(batch, tableWidth, tableHeight);
        table = new Table(skin);
        table.setFillParent(true);
        addActor(table);
    }

    public Table getTable() {
        return table;
    }
}
