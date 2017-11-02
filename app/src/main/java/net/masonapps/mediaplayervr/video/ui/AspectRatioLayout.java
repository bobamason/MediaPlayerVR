package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.R;
import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import java.util.ArrayList;

/**
 * Created by Bob on 2/10/2017.
 */

public class AspectRatioLayout extends SingleStageUi {
    private static final String[] ratioLabels = {"AUTO", "1:1", "4:3", "16:10", "16:9", "2:1"};
    private static final float[] ratios = new float[]{-1f, 1f, 4f / 3f, 16f / 10f, 16f / 9f, 2f / 1f};

    private final VideoPlayerGUI videoPlayerGUI;
    private ArrayList<TextButton> textButtons = new ArrayList<>();

    public AspectRatioLayout(final VideoPlayerGUI videoPlayerGUI) {
        super(videoPlayerGUI.getSpriteBatch(), videoPlayerGUI.getSkin());
        this.videoPlayerGUI = videoPlayerGUI;
        setPosition(0, 0, -2.5f);


        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        for (int i = 0; i < ratioLabels.length; i++) {
            final TextButton textButton = new TextButton(ratioLabels[i], skin);
            final Cell<TextButton> cell = table.add(textButton).expandX().fill().center().pad(padding);
            if (i == videoPlayerGUI.getVideoOptions().aspectRatioSelection) {
                textButton.setChecked(true);
                videoPlayer.setAspectRatio(ratios[i]);
            }
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
        setVisible(false);

        final TextButton customButton = new TextButton(videoPlayerGUI.getVideoPlayerScreen().getStringResource(R.string.custom), skin);
        customButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToPlaybackSettingsLayout();
            }
        });
        table.add(customButton).expandX().fill().center().pad(padding);
        setVisible(false);
        setBackground(skin.newDrawable(Style.Drawables.window, Color.BLACK));
        resizeToFitTable();
    }
}