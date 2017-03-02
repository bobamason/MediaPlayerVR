package net.masonapps.mediaplayervr.gfx;

import com.badlogic.gdx.graphics.VertexAttribute;

/**
 * Created by Bob on 3/2/2017.
 */

public class WaveformMesh extends DynamicMesh {
    private static final int FLOATS_PER_VERTEX = 3 + 4;
    private final float[] waveform;
    private final int sampleCount;
    private final int numVertices;
    private final int numIndices;
    private final float[] vertices;

    public WaveformMesh(int sampleCount) {
        super(sampleCount * 2 * FLOATS_PER_VERTEX, (sampleCount - 1) * 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked());
        this.sampleCount = sampleCount;
        this.numVertices = sampleCount * 2;
        this.numIndices = (sampleCount - 1) * 6;
        vertices = new float[numVertices * FLOATS_PER_VERTEX];
        final short[] indices = new short[numIndices];
        for (int i = 0; i < sampleCount - 1; i++) {
            indices[i * 6] = (short) (i * 2);
            indices[i * 6 + 1] = (short) (2 + i * 2);
            indices[i * 6 + 2] = (short) (3 + i * 2);
            indices[i * 6 + 3] = (short) (i * 2);
            indices[i * 6 + 4] = (short) (3 + i * 2);
            indices[i * 6 + 5] = (short) (1 + i * 2);
        }
        waveform = new float[sampleCount];
        setIndices(indices);
        setVertices(vertices);
    }

    @Override
    public void update(float deltaTime) {
        final int step = 2 * FLOATS_PER_VERTEX;
        final float stepX = 1f / sampleCount * 10f;
        final float startX = -sampleCount * stepX / 2f;
        for (int i = 0; i < sampleCount; i++) {
            vertices[i * step] = startX + i * stepX;
            vertices[i * step + 1] = -waveform[i];
            vertices[i * step + 2] = 0;
            vertices[i * step + 3] = 1f - waveform[i];
            vertices[i * step + 4] = waveform[i];
            vertices[i * step + 5] = waveform[i];
            vertices[i * step + 6] = 1f;

            vertices[i * step + 7] = startX + i * stepX;
            vertices[i * step + 8] = waveform[i];
            vertices[i * step + 9] = 0;
            vertices[i * step + 10] = 1f - waveform[i];
            vertices[i * step + 11] = waveform[i];
            vertices[i * step + 12] = waveform[i];
            vertices[i * step + 13] = 1f;
        }
        updateVertices(0, vertices);
    }

    public void update(float deltaTime, float[] waveform) {
        setWaveform(waveform);
        update(deltaTime);
    }

    public void setWaveform(float[] waveform) {
        System.arraycopy(waveform, 0, this.waveform, 0, waveform.length);
    }

    public int getSampleCount() {
        return sampleCount;
    }
}
