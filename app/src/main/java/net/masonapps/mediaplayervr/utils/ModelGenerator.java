package net.masonapps.mediaplayervr.utils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by Bob on 1/2/2017.
 */

public class ModelGenerator {

    private final static ShortArray tmpIndices = new ShortArray();
    private static final MeshPartBuilder.VertexInfo vertTmp3 = new MeshPartBuilder.VertexInfo();

    public static Model createHalfSphere(ModelBuilder modelBuilder, float radius, int divisionsU, int divisionsV) {
        return createSphere(modelBuilder, radius, 0, 180, 0, 180, divisionsU, divisionsV);
    }

    public static Model createSphere(ModelBuilder modelBuilder, float radius, int divisionsU, int divisionsV) {
        return createSphere(modelBuilder, radius, -90, 270, 0, 180, divisionsU, divisionsV);
    }

    public static Model createPartialSphere(ModelBuilder modelBuilder, float radius, float angleU, float angleV, int divisionsU, int divisionsV) {
        float angleUFrom = (180f - angleU) * 0.5f;
        float angleUTo =  angleUFrom + angleU;
        float angleVFrom = (180f - angleV) * 0.5f;
        float angleVTo =  angleVFrom + angleV;
        return createSphere(modelBuilder, radius, angleUFrom, angleUTo, angleVFrom, angleVTo, divisionsU, divisionsV);
    }

    private static Model createSphere(ModelBuilder modelBuilder, float radius, float angleUFrom, float angleUTo, float angleVFrom, float angleVTo, int divisionsU, int divisionsV) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates, new Material());
        final float auo = MathUtils.degreesToRadians * angleUFrom;
        final float stepU = (MathUtils.degreesToRadians * (angleUTo - angleUFrom)) / divisionsU;
        final float avo = MathUtils.degreesToRadians * angleVFrom;
        final float stepV = (MathUtils.degreesToRadians * (angleVTo - angleVFrom)) / divisionsV;
        final float us = 1f / divisionsU;
        final float vs = 1f / divisionsV;
        float u, v, angleU, angleV;
        MeshPartBuilder.VertexInfo curr1 = vertTmp3.set(null, null, null, null);
        curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;

        final int s = divisionsU + 3;
        tmpIndices.clear();
        tmpIndices.ensureCapacity(divisionsU * 2);
        tmpIndices.size = s;
        int tempOffset = 0;

        builder.ensureVertices((divisionsV + 1) * (divisionsU + 1));
        builder.ensureRectangleIndices(divisionsU);
        for (int iv = 0; iv <= divisionsV; iv++) {
            angleV = avo + stepV * iv;
            v = vs * iv;
            final float t = MathUtils.sin(angleV);
            final float h = MathUtils.cos(angleV) * radius;
            for (int iu = 0; iu <= divisionsU; iu++) {
                angleU = auo + stepU * iu + MathUtils.PI;
                u = us * iu;
                curr1.position.set(MathUtils.cos(angleU) * radius * t, h, MathUtils.sin(angleU) * radius * t);
                curr1.normal.set(curr1.position).scl(-1).nor();
                curr1.uv.set(u, v);
                tmpIndices.set(tempOffset, builder.vertex(curr1));
                final int o = tempOffset + s;
                if ((iv > 0) && (iu > 0)) 
                    builder.rect(tmpIndices.get(tempOffset), tmpIndices.get((o - 1) % s), tmpIndices.get((o - (divisionsU + 2)) % s),
                            tmpIndices.get((o - (divisionsU + 1)) % s));
                tempOffset = (tempOffset + 1) % tmpIndices.size;
            }
        }
        return modelBuilder.end();
    }

    public static Model createRect(ModelBuilder modelBuilder) {
        final float x = -0.5f;
        final float y = -0.5f;
        final float s = 1f;
        return modelBuilder.createRect(
                x, y, 0,
                x + s, y, 0,
                x + s, y + s, 0,
                x, y + s, 0,
                0, 0, 1,
                new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates);
    }
}
