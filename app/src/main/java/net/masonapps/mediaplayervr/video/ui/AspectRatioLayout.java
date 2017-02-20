package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

import java.util.ArrayList;

/**
 * Created by Bob on 2/10/2017.
 */

public class AspectRatioLayout extends BaseUiLayout {
    private static final String[] ratioLabels = {"AUTO", "1:1", "4:3", "16:10", "16:9", "2:1"};
    private static final float[] ratios = new float[]{-1f, 1f, 4f / 3f, 16f / 10f, 16f / 9f, 2f / 1f};

    private final Table table;
    private final VideoPlayerGUI videoPlayerGUI;
    private ArrayList<TextButton> textButtons = new ArrayList<>();
    private VirtualStage stage;

    public AspectRatioLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final Skin skin = videoPlayerGUI.getSkin();
        stage = new VirtualStage(videoPlayerGUI.getSpriteBatch(), 360, 360);
        stage.setPosition(0, 0, -2.5f);
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        bg.setFillParent(true);
        stage.addActor(bg);

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


        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        for (int i = 0; i < ratioLabels.length; i++) {
            final TextButton textButton = new TextButton(ratioLabels[i], skin);
            final Cell<TextButton> cell = table.add(textButton).expandX().fill().center().pad(padding);
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