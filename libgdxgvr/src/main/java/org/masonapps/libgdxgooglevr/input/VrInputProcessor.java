package org.masonapps.libgdxgooglevr.input;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Bob on 1/9/2017.
 */

public interface VrInputProcessor {

    boolean performRayTest(Ray ray);

    /**
     * @return 2D point on plane
     */
    Vector2 getHitPoint2D();

    Vector3 getHitPoint3D();

    boolean isCursorOver();
}
