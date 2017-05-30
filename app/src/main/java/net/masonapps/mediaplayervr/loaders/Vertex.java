package net.masonapps.mediaplayervr.loaders;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 5/11/2017.
 */

public class Vertex {
    public final Vector3 position = new Vector3();
    public final Vector2 uv = new Vector2();
    public int index;

    public Vertex() {
    }

    public Vertex(Vertex vertex) {
        set(vertex);
    }

    public Vertex set(Vertex vertex) {
        position.set(vertex.position);
        uv.set(vertex.uv);
        index = vertex.index;
        return this;
    }

    public Vertex lerp(Vertex vertex, float t) {
        position.lerp(vertex.position, t);
        uv.lerp(vertex.uv, t);
        return this;
    }
}
