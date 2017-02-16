package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.Attachable;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.text.DecimalFormat;

/**
 * Created by Bob on 2/8/2017.
 */

public class PlaybackSettingsLayout implements Attachable {

    private static final float STEP = 0.01f;
    private final Table table;
    private final VideoPlayerGUI videoPlayerGUI;
    private Vector2 stretch = new Vector2();
    private float z = 1f;
    private float ipd = 0f;
    private DecimalFormat df = new DecimalFormat("0.00");

    public PlaybackSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final Skin skin = videoPlayerGUI.getSkin();
        table = new Table(skin);
        table.padTop(videoPlayerGUI.getHeaderHeight());
        table.setFillParent(false);
        table.center();
        table.setVisible(false);
        df.setDecimalSeparatorAlwaysShown(true);
        final VrVideoPlayer player = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();

        final ImageButton xLeft = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, false));
        xLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.x += STEP;
                player.setStretch(stretch);
                videoPlayerGUI.getVideoOptions().textureStretch.set(stretch);
            }
        });
        table.add(xLeft).pad(VideoPlayerGUI.PADDING);

        table.add(new Label(" X ", skin)).pad(VideoPlayerGUI.PADDING);

        final ImageButton xRight = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, false));
        xRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.x -= STEP;
                player.setStretch(stretch);
                videoPlayerGUI.getVideoOptions().textureStretch.set(stretch);
            }
        });
        table.add(xRight).pad(VideoPlayerGUI.PADDING).row();

        final ImageButton yLeft = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, false));
        yLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.y += STEP;
                player.setStretch(stretch);
                videoPlayerGUI.getVideoOptions().textureStretch.set(stretch);
            }
        });
        table.add(yLeft).pad(VideoPlayerGUI.PADDING);

        table.add(new Label(" Y ", skin)).pad(VideoPlayerGUI.PADDING);

        final ImageButton yRight = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, false));
        yRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.y -= STEP;
                player.setStretch(stretch);
                videoPlayerGUI.getVideoOptions().textureStretch.set(stretch);
            }
        });
        table.add(yRight).pad(VideoPlayerGUI.PADDING).row();
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
