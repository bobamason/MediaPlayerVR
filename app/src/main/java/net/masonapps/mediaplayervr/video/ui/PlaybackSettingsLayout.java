package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

import java.text.DecimalFormat;

/**
 * Created by Bob on 2/8/2017.
 */

public class PlaybackSettingsLayout extends BaseUiLayout {

    private static final float STEP = 0.01f;
    private final Table table;
    private final VideoPlayerGUI videoPlayerGUI;
    private VirtualStage stage;
    private Vector2 stretch = new Vector2();
    private float s = 10f;
    private DecimalFormat df = new DecimalFormat("0.00");

    public PlaybackSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final Skin skin = videoPlayerGUI.getSkin();
        stage = new VirtualStage(videoPlayerGUI.getSpriteBatch(), 360, 360);
        stage.setPosition(0, 0, -2.5f);
        stage.addActor(Style.newBackgroundImage(skin));

        final ImageButton closeButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_close_white_48dp, true));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        stage.addActor(closeButton);

        closeButton.setPosition(stage.getWidth() - padding, stage.getHeight() - padding, Align.topRight);
        table = new Table(skin);
        table.padTop(closeButton.getHeight());
        table.setFillParent(true);
        table.center();

        df.setDecimalSeparatorAlwaysShown(true);
        final VrVideoPlayer player = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();

        final ImageButton.ImageButtonStyle leftButtonStyle = Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, false);
        final ImageButton.ImageButtonStyle rightButtonStyle = Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, false);

        final VideoOptions videoOptions = videoPlayerGUI.getVideoOptions();
        stretch.set(videoOptions.textureStretch);
        player.setStretch(stretch);
        
        final ImageButton xLeft = new ImageButton(leftButtonStyle);
        xLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.x -= STEP;
                player.setStretch(stretch);
                videoOptions.textureStretch.set(stretch);
            }
        });
        table.add(xLeft).pad(padding);

        table.add(new Label(" X ", skin)).pad(padding);
        final ImageButton xRight = new ImageButton(rightButtonStyle);
        xRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.x += STEP;
                player.setStretch(stretch);
                videoOptions.textureStretch.set(stretch);
            }
        });
        table.add(xRight).pad(padding).row();

        final ImageButton yLeft = new ImageButton(leftButtonStyle);
        yLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.y -= STEP;
                player.setStretch(stretch);
                videoOptions.textureStretch.set(stretch);
            }
        });
        table.add(yLeft).pad(padding);

        table.add(new Label(" Y ", skin)).pad(padding);

        final ImageButton yRight = new ImageButton(rightButtonStyle);
        yRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.y += STEP;
                player.setStretch(stretch);
                videoOptions.textureStretch.set(stretch);
            }
        });
        table.add(yRight).pad(padding).row();
        setVisible(false);
    }

    @Override
    public void update() {
        stage.act();
    }

    @Override
    public void draw(Camera camera) {
        stage.draw(camera);
    }

    @Override
    public void attach(VrInputMultiplexer inputMultiplexer) {
        stage.addActor(table);
        inputMultiplexer.addProcessor(stage);
    }

    @Override
    public boolean isVisible() {
        return stage.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        stage.setVisible(visible);
    }

    @Override
    public void dispose() {
        if (stage != null)
            stage.dispose();
    }
}
