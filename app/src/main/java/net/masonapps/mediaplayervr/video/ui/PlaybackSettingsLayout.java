package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import org.masonapps.libgdxgooglevr.ui.VirtualStage;

import java.text.DecimalFormat;

/**
 * Created by Bob on 2/8/2017.
 */

public class PlaybackSettingsLayout extends SingleStageUi {

    private static final float STEP = 0.01f;
    private final VideoPlayerGUI videoPlayerGUI;
    private Vector2 stretch = new Vector2();
    private float s = 10f;
    private DecimalFormat df = new DecimalFormat("0.00");
    private float shift = 0f;

    public PlaybackSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        super(new VirtualStage(videoPlayerGUI.getSpriteBatch(), 360, 500), videoPlayerGUI.getSkin());
        this.videoPlayerGUI = videoPlayerGUI;
        stage.setPosition(0, 0, -2.5f);
        stage.addActor(Style.newBackgroundImage(skin));

        final ImageButton closeButton = new ImageButton(Style.createImageButtonStyle(skin, Style.Drawables.ic_close_white_48dp, true));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        stage.addActor(closeButton);

        closeButton.setPosition(stage.getWidth() - padding, stage.getHeight() - padding, Align.topRight);
        table.padTop(closeButton.getHeight());
        table.setFillParent(true);
        table.center();

        df.setDecimalSeparatorAlwaysShown(true);
        final VrVideoPlayer player = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();

        final ImageButton.ImageButtonStyle leftButtonStyle = Style.createImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, false);
        final ImageButton.ImageButtonStyle rightButtonStyle = Style.createImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, false);

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

        final ImageButton shiftLeft = new ImageButton(leftButtonStyle);
        shiftLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shift -= STEP;
                player.set3dShift(shift);
            }
        });
        table.add(shiftLeft).pad(padding);

        table.add(new Label(" Shift ", skin)).pad(padding);

        final ImageButton shiftRight = new ImageButton(rightButtonStyle);
        shiftRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shift += STEP;
                player.set3dShift(shift);
            }
        });
        table.add(shiftRight).pad(padding).row();
        setVisible(false);

        final TextButton resetBtn = new TextButton("reset", skin);
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stretch.set(0, 0);
                player.setStretch(stretch);
                videoOptions.textureStretch.set(stretch);
                shift = 0;
                player.set3dShift(shift);
            }
        });
        table.add(resetBtn).pad(padding).colspan(3).row();

        final TextButton cylinderBtn = new TextButton("use cylinder", skin, Style.TOGGLE);
        cylinderBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                player.setUseFishEyeProjection(cylinderBtn.isChecked());
            }
        });
        table.add(cylinderBtn).pad(padding).colspan(3).row();
    }
}
