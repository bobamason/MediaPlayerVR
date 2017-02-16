package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.Attachable;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.util.ArrayList;

/**
 * Created by Bob on 2/10/2017.
 */

public class AspectRatioLayout implements Attachable {
    private static final String[] ratioLabels = {"AUTO", "1:1", "4:3", "16:10", "16:9", "2:1"};
    private static final float[] ratios = new float[]{-1f, 1f, 4f / 3f, 16f / 10f, 16f / 9f, 2f / 1f};

    private final Table table;
    private final VideoPlayerGUI videoPlayerGUI;
    private ArrayList<TextButton> textButtons = new ArrayList<>();

    public AspectRatioLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final Skin skin = videoPlayerGUI.getSkin();
        table = new Table(skin);
        table.padTop(videoPlayerGUI.getHeaderHeight());
        table.setFillParent(false);
        table.center();
        table.setVisible(false);

        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        for (int i = 0; i < ratioLabels.length; i++) {
            final TextButton textButton = new TextButton(ratioLabels[i], skin);
            final Cell<TextButton> cell = table.add(textButton).expandX().fill().center().pad(VideoPlayerGUI.PADDING);
            if (i % 3 == 2) cell.row();
            final int index = i;
            textButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    videoPlayer.setAspectRatio(ratios[index]);
                    videoPlayerGUI.getVideoOptions().aspectRatioSelection = index;
                }
            });
            textButtons.add(textButton);
        }
    }

    @Override
    public void attach(VirtualStage stage) {
        stage.addActor(table);
    }

    @Override
    public boolean isVisible() {
        return table.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        table.setVisible(visible);
        if (visible)
            videoPlayerGUI.getStage().getViewport().update((int) table.getWidth(), (int) table.getHeight(), true);
    }
}