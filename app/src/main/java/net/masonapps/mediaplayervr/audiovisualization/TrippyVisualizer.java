package net.masonapps.mediaplayervr.audiovisualization;

import android.content.Context;

import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 3/18/2017.
 */

public class TrippyVisualizer extends MusicVisualizerScreen {

    public TrippyVisualizer(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);
    }
}
