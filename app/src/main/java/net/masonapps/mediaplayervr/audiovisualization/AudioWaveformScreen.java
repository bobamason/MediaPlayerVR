package net.masonapps.mediaplayervr.audiovisualization;

import android.content.Context;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;

import net.masonapps.mediaplayervr.gfx.WaveformMesh;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 3/2/2017.
 */

public class AudioWaveformScreen extends MusicVisualizerScreen {
    private ModelInstance instance;
    private ModelInstance starsInstance;
    private WaveformMesh waveformMesh;

    public AudioWaveformScreen(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);

        final ModelBuilder modelBuilder = new ModelBuilder();

        waveformMesh = new WaveformMesh(visualizer.getCaptureSize());
        modelBuilder.begin();
        modelBuilder.part("mesh", waveformMesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createAmbient(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE)));
        instance = new ModelInstance(modelBuilder.end());
        instance.transform.setToTranslation(0, 0, -10);

        int numStars = 100;
        final Mesh stars = new Mesh(true, numStars * 3, numStars, VertexAttribute.Position());
        final float[] vertices = new float[numStars * 3];
        final short[] indices = new short[numStars];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
            final float a = MathUtils.random(0, MathUtils.PI2);
            final float r = MathUtils.random(10, 50);
            final float y = MathUtils.random(-50, 50);
            vertices[i * 3] = r * MathUtils.cos(a);
            vertices[i * 3 + 1] = y;
            vertices[i * 3 + 2] = -r * MathUtils.sin(a);
        }
        stars.setIndices(indices);
        stars.setVertices(vertices);

        modelBuilder.begin();
        modelBuilder.part("stars", stars, GL20.GL_POINTS, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        starsInstance = new ModelInstance(modelBuilder.end());
    }

    @Override
    protected void onCaptureUpdated(SpectrumAnalyzer spectrumAnalyzer) {
        super.onCaptureUpdated(spectrumAnalyzer);
        waveformMesh.setWaveform(spectrumAnalyzer.getWaveform());
    }

    @Override
    public void update() {
        super.update();
        waveformMesh.update(GdxVr.graphics.getDeltaTime());
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        getModelBatch().begin(camera);
        getModelBatch().render(starsInstance);
        getModelBatch().render(instance);
        getModelBatch().end();
    }
}
