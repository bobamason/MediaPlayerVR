package org.masonapps.libgdxgooglevr.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FlushablePool;

/**
 * Created by Bob on 12/14/2016.
 */

public class Vecs {

    private static FlushablePool<Vector2> vector2Pool = new FlushablePool<Vector2>() {
        @Override
        protected Vector2 newObject() {
            return new Vector2();
        }
    };


    private static FlushablePool<Vector3> vector3Pool = new FlushablePool<Vector3>() {
        @Override
        protected Vector3 newObject() {
            return new Vector3();
        }
    };

    public static Vector2 obtainV2() {
        return vector2Pool.obtain();
    }

    public static Vector3 obtainV3() {
        return vector3Pool.obtain();
    }

    public static void freeAll() {
        vector2Pool.flush();
        vector3Pool.flush();
    }
}
