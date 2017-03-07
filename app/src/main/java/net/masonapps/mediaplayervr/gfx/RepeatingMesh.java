package net.masonapps.mediaplayervr.gfx;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;

/**
 * Created by Bob on 3/7/2017.
 */

public class RepeatingMesh extends Mesh {

    public RepeatingMesh(Mesh mesh) {
        super(true, mesh.getNumVertices() * 27, mesh.getNumIndices() * 27, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.Binormal());

        final short[] meshIndices = new short[mesh.getNumIndices()];
        mesh.getIndices(meshIndices);
        final float[] meshVertices = new float[mesh.getNumVertices() * (mesh.getVertexAttributes().vertexSize / 4)];
        mesh.getVertices(meshVertices);
        final int floatsPerVertex = 3 + 3 + 3;
        final int verticesPerObj = mesh.getNumVertices();
        final int indicesPerObj = mesh.getNumIndices();
        final int numObjs = 27;
        final float[] vertices = new float[numObjs * verticesPerObj * floatsPerVertex];
        final short[] indices = new short[numObjs * indicesPerObj];
        for (int c = 0; c < numObjs; c++) {
//            Log.d(TAG, "-----start cube: " + c);
            for (int i = 0; i < indicesPerObj; i++) {
                final short index = (short) (meshIndices[i] + c * verticesPerObj);
//                Log.d(TAG, "index: " + index);
                indices[c * indicesPerObj + i] = index;
            }
            final float x = (c % 3) * 2f - 2f;
            final float y = ((c / 3) % 3) * 2f - 2f;
            final float z = (c / 9) * 2f - 2f;
            for (int v = 0; v < verticesPerObj; v++) {
//                Log.d(TAG, "---start vertex: " + (c * verticesPerObj + v));
                final int offset = c * verticesPerObj * floatsPerVertex + v * floatsPerVertex;
                for (int j = 0; j < 6; j++) {
                    final int fIndex = offset + j;
//                    Log.d(TAG, "float index: " + fIndex);
                    vertices[fIndex] = meshVertices[v * 6 + j];
                }
                vertices[offset + 6] = x;
                vertices[offset + 7] = y;
                vertices[offset + 8] = z;
//                Log.d(TAG, "---end vertex: " + (c * verticesPerObj + v));
            }
//            Log.d(TAG, "-----end cube: " + c);
        }
        setIndices(indices);
        setVertices(vertices);
    }
}
