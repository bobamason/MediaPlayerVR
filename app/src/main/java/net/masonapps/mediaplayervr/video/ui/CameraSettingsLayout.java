package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.VideoPlayerScreen;
import net.masonapps.mediaplayervr.vrinterface.Attachable;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.text.DecimalFormat;

/**
 * Created by Bob on 2/8/2017.
 */

public class CameraSettingsLayout implements Attachable {

    private static final float STEP = 0.01f;
    private final Table table;
    private final VideoPlayerGUI videoPlayerGUI;
    private Vector2 stretch = new Vector2();
    private float z = 1f;
    private float ipd = 0f;
    private DecimalFormat df = new DecimalFormat("0.00");

    public CameraSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final Skin skin = videoPlayerGUI.getSkin();
        table = new Table(skin);
        table.padTop(videoPlayerGUI.getHeaderHeight());
        table.setFillParent(false);
        table.center();
        table.setVisible(false);
        df.setDecimalSeparatorAlwaysShown(true);

        final Label zoomLabel = new Label("Zoom " + Math.round(z * 100f) + "%", skin);

        final ImageButton zLeft = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, false));
        zLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                z += STEP;
                videoPlayerGUI.getVideoPlayerScreen().setZ(z);
                videoPlayerGUI.getVideoOptions().zoom = z;
                zoomLabel.setText("Zoom " + Math.round(z * 100f) + "%");
            }
        });
        table.add(zLeft).pad(VideoPlayerGUI.PADDING);

        table.add(zoomLabel).pad(VideoPlayerGUI.PADDING);

        final ImageButton zRight = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, false));
        zRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                z -= STEP;
                videoPlayerGUI.getVideoPlayerScreen().setZ(z);
                videoPlayerGUI.getVideoOptions().zoom = z;
                zoomLabel.setText("Zoom " + Math.round(z * 100f) + "%");
            }
        });
        table.add(zRight).pad(VideoPlayerGUI.PADDING).row();


        final Label ipdLabel = new Label("IPD " + df.format(videoPlayerGUI.getVideoPlayerScreen().getIpd()), skin);

        final ImageButton ipdLeft = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, false));
        ipdLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ipd -= STEP;
                final VideoPlayerScreen screen = videoPlayerGUI.getVideoPlayerScreen();
                screen.setIpd(screen.getDefaultIpd() + ipd);
                ipdLabel.setText("IPD " + df.format(screen.getIpd()));
                videoPlayerGUI.getVideoOptions().ipd = screen.getIpd();
            }
        });
        table.add(ipdLeft).pad(VideoPlayerGUI.PADDING);
        table.add(ipdLabel).pad(VideoPlayerGUI.PADDING);

        final ImageButton ipdRight = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, false));
        ipdRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ipd += STEP;
                final VideoPlayerScreen screen = videoPlayerGUI.getVideoPlayerScreen();
                screen.setIpd(screen.getDefaultIpd() + ipd);
                ipdLabel.setText("IPD " + df.format(screen.getIpd()));
                videoPlayerGUI.getVideoOptions().ipd = screen.getIpd();
            }
        });
        table.add(ipdRight).pad(VideoPlayerGUI.PADDING).row();
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
