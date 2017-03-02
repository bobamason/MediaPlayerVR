package net.masonapps.mediaplayervr.gfx;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.graphics.glutils.VertexData;

/**
 * Created by Bob on 3/2/2017.
 */

public abstract class DynamicMesh extends Mesh {
    protected DynamicMesh(VertexData vertices, IndexData indices, boolean isVertexArray) {
        super(vertices, indices, isVertexArray);
    }

    public DynamicMesh(int maxVertices, int maxIndices, VertexAttribute... attributes) {
        super(false, maxVertices, maxIndices, attributes);
    }

    public DynamicMesh(int maxVertices, int maxIndices, VertexAttributes attributes) {
        super(false, maxVertices, maxIndices, attributes);
    }

    public DynamicMesh(VertexDataType type, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        super(type, false, maxVertices, maxIndices, attributes);
    }

    public abstract void update(float deltaTime);
}
