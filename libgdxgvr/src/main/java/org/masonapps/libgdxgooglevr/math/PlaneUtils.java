package org.masonapps.libgdxgooglevr.math;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 8/25/2017.
 */

public class PlaneUtils {

    private static final Vector3 NEG_Z = Vector3.Z.cpy().scl(-1);
    private static final Vector3 u = new Vector3();
    private static final Vector3 v = new Vector3();

    public static Vector2 toSubSpace(Plane plane, Vector3 point) {
        return toSubSpace(plane, point, new Vector2());
    }

    public static Vector2 toSubSpace(Plane plane, Vector3 point, Vector2 out) {
        u.set(Math.abs(plane.normal.dot(Vector3.Y)) < 0.998f ? Vector3.Y : NEG_Z).crs(plane.normal).nor();
        v.set(plane.normal).crs(u).nor();
        out.x = point.dot(u);
        out.y = point.dot(v);
        return out;
    }

    public static Vector3 toSpace(Plane plane, Vector2 point) {
        return toSpace(plane, point, new Vector3());
    }

    public static Vector3 toSpace(Plane plane, Vector2 point, Vector3 out) {
        u.set(Math.abs(plane.normal.dot(Vector3.Y)) < 0.998f ? Vector3.Y : NEG_Z).crs(plane.normal).nor();
        v.set(plane.normal).crs(u).nor();
        final float a = point.x;
        final float b = point.y;
        final float c = -plane.d;
        out.x = a * u.x + b * v.x + c * plane.normal.x;
        out.y = a * u.y + b * v.y + c * plane.normal.y;
        out.z = a * u.z + b * v.z + c * plane.normal.z;
        return out;
    }

    public static Matrix4 getToSpaceMatrix(Plane plane, Matrix4 out) {
        u.set(Math.abs(plane.normal.dot(Vector3.Y)) < 0.998f ? Vector3.Y : NEG_Z).crs(plane.normal).nor();
        v.set(plane.normal).crs(u).nor();
        final float c = -plane.d;
        out.idt();
        out.val[Matrix4.M00] = u.x;
        out.val[Matrix4.M01] = v.x;
//        out.val[Matrix4.M02] = 0f;
        out.val[Matrix4.M03] = c * plane.normal.x;

        out.val[Matrix4.M10] = u.y;
        out.val[Matrix4.M11] = v.y;
//        out.val[Matrix4.M12] = 0f;
        out.val[Matrix4.M13] = c * plane.normal.x;

        out.val[Matrix4.M20] = u.z;
        out.val[Matrix4.M21] = v.z;
//        out.val[Matrix4.M22] = 0f;
        out.val[Matrix4.M23] = c * plane.normal.x;
        return out;
    }

    public static void project(Vector3 in, Plane plane, Vector3 out) {
        out.set(plane.normal).scl(-plane.distance(in)).add(in);
    }

    public static void debugDraw(ShapeRenderer renderer, Plane plane) {
        final Vector2 p0 = new Vector2(-1, -1);
        final Vector2 p1 = new Vector2(1, -1);
        final Vector2 p2 = new Vector2(1, 1);
        final Vector2 p3 = new Vector2(-1, 1);
        renderer.line(toSpace(plane, p0), toSpace(plane, p1));
        renderer.line(toSpace(plane, p1), toSpace(plane, p2));
        renderer.line(toSpace(plane, p2), toSpace(plane, p3));
        renderer.line(toSpace(plane, p3), toSpace(plane, p0));
    }
}
