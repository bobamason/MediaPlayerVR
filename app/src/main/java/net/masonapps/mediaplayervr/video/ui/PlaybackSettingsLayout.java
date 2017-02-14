package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Color;
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

/**
 * Created by Bob on 2/8/2017.
 */

public class PlaybackSettingsLayout implements Attachable {

    private static final float STEP = 0.01f;
    private final Table table;
    private Vector2 stretch = new Vector2();
    private float z = 0f;

    public PlaybackSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        final Skin skin = videoPlayerGUI.getSkin();
        table = new Table(skin);
        table.padTop(videoPlayerGUI.getHeaderHeight());
        table.setFillParent(true);
        table.center();
        table.setVisible(false);
        final VrVideoPlayer player = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();

        final ImageButton xLeft = new ImageButton(skin.newDrawable(Style.Drawables.ic_chevron_left_white_48dp), skin.newDrawable(Style.Drawables.ic_chevron_left_white_48dp, Color.LIGHT_GRAY));
        xLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.x += STEP;
                player.setStretch(stretch);
            }
        });
        table.add(xLeft).pad(VideoPlayerGUI.PADDING);

        table.add(new Label(" X ", skin)).pad(VideoPlayerGUI.PADDING);

        final ImageButton xRight = new ImageButton(skin.newDrawable(Style.Drawables.ic_chevron_right_white_48dp), skin.newDrawable(Style.Drawables.ic_chevron_right_white_48dp, Color.LIGHT_GRAY));
        xRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.x -= STEP;
                player.setStretch(stretch);
            }
        });
        table.add(xRight).pad(VideoPlayerGUI.PADDING).row();

        final ImageButton yLeft = new ImageButton(skin.newDrawable(Style.Drawables.ic_chevron_left_white_48dp), skin.newDrawable(Style.Drawables.ic_chevron_left_white_48dp, Color.LIGHT_GRAY));
        yLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.y += STEP;
                player.setStretch(stretch);
            }
        });
        table.add(yLeft).pad(VideoPlayerGUI.PADDING);

        table.add(new Label(" Y ", skin)).pad(VideoPlayerGUI.PADDING);

        final ImageButton yRight = new ImageButton(skin.newDrawable(Style.Drawables.ic_chevron_right_white_48dp), skin.newDrawable(Style.Drawables.ic_chevron_right_white_48dp, Color.LIGHT_GRAY));
        yRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.y -= STEP;
                player.setStretch(stretch);
            }
        });
        table.add(yRight).pad(VideoPlayerGUI.PADDING).row();

        final ImageButton zLeft = new ImageButton(skin.newDrawable(Style.Drawables.ic_chevron_left_white_48dp), skin.newDrawable(Style.Drawables.ic_chevron_left_white_48dp, Color.LIGHT_GRAY));
        zLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                z += STEP * 2f;
                videoPlayerGUI.getVideoPlayerScreen().setZ(z);
            }
        });
        table.add(zLeft).pad(VideoPlayerGUI.PADDING);

        table.add(new Label(" Z ", skin)).pad(VideoPlayerGUI.PADDING);

        final ImageButton zRight = new ImageButton(skin.newDrawable(Style.Drawables.ic_chevron_right_white_48dp), skin.newDrawable(Style.Drawables.ic_chevron_right_white_48dp, Color.LIGHT_GRAY));
        zRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                z -= STEP * 2f;
                videoPlayerGUI.getVideoPlayerScreen().setZ(z);
            }
        });
        table.add(zRight).pad(VideoPlayerGUI.PADDING).row();
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
    }
}
