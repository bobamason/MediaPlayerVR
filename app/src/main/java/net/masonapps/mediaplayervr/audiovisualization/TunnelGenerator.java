package net.masonapps.mediaplayervr.audiovisualization;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import org.masonapps.libgdxgooglevr.utils.Vecs;


/**
 * Created by Bob on 12/2/2016.
 */

public class TunnelGenerator {

    private static final Vector3 tempV = new Vector3();

    public static Model generateTunnelSection(ModelBuilder modelBuilder, float innerRad, float stripWidth, float length, int numLayers, float layerSpacing, Material material) {
        float stripHeight = 0.5f;
        final Vector3 normal = new Vector3();
        final Vector3 corner00 = new Vector3();
        final Vector3 corner10 = new Vector3();
        final Vector3 corner11 = new Vector3();
        final Vector3 corner01 = new Vector3();
        final Vector3 tmp = new Vector3();
        final Vector3 center = new Vector3();
        modelBuilder.begin();
        final MeshPartBuilder builder = modelBuilder.part("section", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material);
        float halfLen = length / 2f;
        for (int layer = 0; layer < numLayers; layer++) {
            float r = innerRad + layer * layerSpacing;
            final int numStrips = MathUtils.floor(MathUtils.PI2 / Math.abs(2f * 2f * MathUtils.atan2(stripWidth * 0.5f, r)));
            float aStep = MathUtils.PI2 / numStrips;
            float offset = aStep / 2f;
            final float end = MathUtils.PI2 - aStep + offset;
            for (float a = offset; a <= end; a += aStep) {
                center.set(MathUtils.cos(a) * r, MathUtils.sin(a) * r, 0);
                normal.set(center).scl(-1f).nor();
                tmp.set(Vector3.Z).crs(normal).nor().scl(stripWidth * 0.5f);
                float z1 = -halfLen + halfLen * (a / end);
                float z2 = z1 + halfLen;
//                float z1 = -halfLen + MathUtils.random(0f, length / 8f);
//                float z2 = z1 + MathUtils.random(length / 12f, length / 4f);
//                while (z2 <= halfLen) {
                corner00.set(center.x - tmp.x, center.y - tmp.y, z2);
                corner10.set(center.x - tmp.x, center.y - tmp.y, z1);
                corner11.set(center.x + tmp.x, center.y + tmp.y, z1);
                corner01.set(center.x + tmp.x, center.y + tmp.y, z2);
                builder.rect(corner00, corner10, corner11, corner01, normal);

                corner00.set(center.x - tmp.x - normal.x * stripHeight, center.y - tmp.y - normal.y * stripHeight, z1);
                corner10.set(center.x - tmp.x, center.y - tmp.y, z1);
                corner11.set(center.x + tmp.x, center.y + tmp.y, z1);
                corner01.set(center.x + tmp.x - normal.x * stripHeight, center.y + tmp.y - normal.y * stripHeight, z1);
                builder.rect(corner00, corner10, corner11, corner01, tempV.set(Vector3.Z).scl(-1));

                corner00.set(center.x - tmp.x - normal.x * stripHeight, center.y - tmp.y - normal.y * stripHeight, z2);
                corner10.set(center.x - tmp.x, center.y - tmp.y, z2);
                corner11.set(center.x + tmp.x, center.y + tmp.y, z2);
                corner01.set(center.x + tmp.x - normal.x * stripHeight, center.y + tmp.y - normal.y * stripHeight, z2);
                builder.rect(corner00, corner10, corner11, corner01, tempV.set(Vector3.Z));

                corner00.set(center.x + tmp.x, center.y + tmp.y, z1);
                corner10.set(center.x + tmp.x, center.y + tmp.y, z2);
                corner11.set(center.x + tmp.x - normal.x * stripHeight, center.y + tmp.y - normal.y * stripHeight, z2);
                corner01.set(center.x + tmp.x - normal.x * stripHeight, center.y + tmp.y - normal.y * stripHeight, z1);
                builder.rect(corner00, corner10, corner11, corner01, tempV.set(tmp));


                corner00.set(center.x - tmp.x, center.y - tmp.y, z2);
                corner10.set(center.x - tmp.x, center.y - tmp.y, z1);
                corner11.set(center.x - tmp.x - normal.x * stripHeight, center.y - tmp.y - normal.y * stripHeight, z1);
                corner01.set(center.x - tmp.x - normal.x * stripHeight, center.y - tmp.y - normal.y * stripHeight, z2);
                builder.rect(corner00, corner10, corner11, corner01, tempV.set(tmp).scl(-1));

//                    z1 = z2 + MathUtils.random(length / 12f, length / 4f);
//                    z2 = z1 + MathUtils.random(length / 12f, length / 4f);
//                }
            }
        }
        return modelBuilder.end();
    }

    public static Model generateCylindricalSection(ModelBuilder modelBuilder, float radius, float length, int divisions, Material material) {
        final MeshPartBuilder.VertexInfo corner00info = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner10info = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner11info = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner01info = new MeshPartBuilder.VertexInfo();
        final Vector3 normal = new Vector3();
        final Vector3 v1 = new Vector3();
        final Vector3 v2 = new Vector3();
        modelBuilder.begin();
        final MeshPartBuilder builder = modelBuilder.part("section", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material);
        float halfLen = length / 2f;
        float aStep = MathUtils.PI2 / divisions;
        for (float a = 0f; a < MathUtils.PI2; a += aStep) {
            v1.set(MathUtils.cos(a) * radius, MathUtils.sin(a) * radius, 0);
            v2.set(MathUtils.cos(a + aStep) * radius, MathUtils.sin(a + aStep) * radius, 0);

            corner00info.setPos(v1.x, v1.y, -halfLen);
            corner10info.setPos(v1.x, v1.y, halfLen);
            corner11info.setPos(v2.x, v2.y, halfLen);
            corner01info.setPos(v2.x, v2.y, -halfLen);

            corner00info.setNor(normal.set(v1.x, v1.y, 0).scl(-1f).nor());
            corner10info.setNor(normal.set(v1.x, v1.y, 0).scl(-1f).nor());
            corner11info.setNor(normal.set(v2.x, v2.y, 0).scl(-1f).nor());
            corner01info.setNor(normal.set(v2.x, v2.y, 0).scl(-1f).nor());

            corner00info.setUV(a / MathUtils.PI2, 0f);
            corner10info.setUV(a / MathUtils.PI2, 1f);
            corner11info.setUV((a + aStep) / MathUtils.PI2, 1f);
            corner01info.setUV((a + aStep) / MathUtils.PI2, 0f);

            corner00info.hasColor = false;
            corner10info.hasColor = false;
            corner11info.hasColor = false;
            corner01info.hasColor = false;

            builder.rect(corner11info, corner01info, corner00info, corner10info);
        }
        return modelBuilder.end();
    }

    public static Model generateMovingBarTunnel(ModelBuilder modelBuilder, float radius, float length, int divisions, Material material, float minThickness, float maxThickness) {
        final MeshPartBuilder.VertexInfo corner000 = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner100 = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner110 = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner010 = new MeshPartBuilder.VertexInfo();

        final MeshPartBuilder.VertexInfo corner001 = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner101 = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner111 = new MeshPartBuilder.VertexInfo();
        final MeshPartBuilder.VertexInfo corner011 = new MeshPartBuilder.VertexInfo();
        final Vector3 vN = new Vector3();
        final Vector3 fNor = new Vector3();
        final Vector3 v1 = new Vector3();
        final Vector3 v2 = new Vector3();
        modelBuilder.begin();
        final MeshPartBuilder builder = modelBuilder.part("section", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked, material);
        float halfLen = length / 2f;
        float aStep = MathUtils.PI2 / divisions;
        for (float a = 0f; a < MathUtils.PI2; a += aStep * 2f) {
            v1.set(MathUtils.cos(a) * radius, MathUtils.sin(a) * radius, 0);
            v2.set(MathUtils.cos(a + aStep) * radius, MathUtils.sin(a + aStep) * radius, 0);
            fNor.set(MathUtils.cos(a + aStep * 0.5f), MathUtils.sin(a + aStep * 0.5f), 0).scl(-1f).nor();
            vN.set(fNor).scl(-minThickness);

            corner000.setPos(v1.x + vN.x, v1.y + vN.y, v1.z - halfLen);
            corner010.setPos(v1.x, v1.y, v1.z - halfLen);
            corner100.setPos(v2.x + vN.x, v2.y + vN.y, v1.z - halfLen);
            corner110.setPos(v2.x, v2.y, v2.z - halfLen);

            corner001.setPos(v1.x + vN.x, v1.y + vN.y, v1.z + halfLen);
            corner011.setPos(v1.x, v1.y, v1.z + halfLen);
            corner101.setPos(v2.x + vN.x, v2.y + vN.y, v1.z + halfLen);
            corner111.setPos(v2.x, v2.y, v2.z + halfLen);

            final Vector3 negZ = Vecs.obtainV3().set(Vector3.Z).scl(-1);
            corner000.setNor(negZ);
            corner010.setNor(negZ);
            corner100.setNor(negZ);
            corner110.setNor(negZ);

            corner001.setNor(Vector3.Z);
            corner011.setNor(Vector3.Z);
            corner101.setNor(Vector3.Z);
            corner111.setNor(Vector3.Z);

            corner000.setUV(0, 1);
            corner010.setUV(0, 0);
            corner100.setUV(1, 1);
            corner110.setUV(1, 0);

            corner001.setUV(0, 1);
            corner011.setUV(0, 0);
            corner101.setUV(1, 1);
            corner111.setUV(1, 0);

            fNor.nor().add(1f, 1f, 1f).scl(0.5f);
            corner000.setCol(0f, 0f, 0f, 1f);
            corner010.hasColor = true;
            corner010.color.r = fNor.x;
            corner010.color.g = fNor.y;
            corner010.color.b = fNor.z;
            corner100.setCol(0f, 0f, 0f, 1f);
            corner110.hasColor = true;
            corner110.color.r = fNor.x;
            corner110.color.g = fNor.y;
            corner110.color.b = fNor.z;

            corner001.setCol(0f, 0f, 0f, 1f);
            corner011.hasColor = true;
            corner011.color.r = fNor.x;
            corner011.color.g = fNor.y;
            corner011.color.b = fNor.z;
            corner101.setCol(0f, 0f, 0f, 1f);
            corner111.hasColor = true;
            corner111.color.r = fNor.x;
            corner111.color.g = fNor.y;
            corner111.color.b = fNor.z;

            BoxShapeBuilder.build(builder, corner000, corner010, corner100, corner110, corner001, corner011, corner101, corner110);
        }
        return modelBuilder.end();
    }

    public static Model generateCircleLayerSection(ModelBuilder modelBuilder, float radius, float length, int divisions, Material material) {
        modelBuilder.begin();
        final MeshPartBuilder builder = modelBuilder.part("section", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material);
        float halfLen = length / 2f;
        float step = length / divisions;
        for (float z = -halfLen; z <= halfLen; z += step) {
            EllipseShapeBuilder.build(builder, radius, 64, 0, 0, z, 0, 0, 1);
        }
        return modelBuilder.end();
    }
}
