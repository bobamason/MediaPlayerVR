package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.video.DisplayMode;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import org.masonapps.libgdxgooglevr.ui.VirtualStage;

import java.util.ArrayList;

/**
 * Created by Bob on 2/8/2017.
 */

public class ModeLayout extends SingleStageUi {
    private static ObjectMap<String, DisplayMode> nameModeMap = new ObjectMap<>();
    private static ObjectMap<DisplayMode, String> modeNameMap = new ObjectMap<>();
    private static Array<String> modes = new Array<>();

    static {
        modes.add("2D");
        modes.add("3D L/R");
        modes.add("3D T/B");

        modes.add("180");
        modes.add("180 L/R");
        modes.add("180 T/B");

        modes.add("360");
        modes.add("360 L/R");
        modes.add("360 T/B");
        nameModeMap.put(modes.get(0), DisplayMode.Mono);
        nameModeMap.put(modes.get(1), DisplayMode.LR3D);
        nameModeMap.put(modes.get(2), DisplayMode.TB3D);

        nameModeMap.put(modes.get(3), DisplayMode.Mono180);
        nameModeMap.put(modes.get(4), DisplayMode.LR180);
        nameModeMap.put(modes.get(5), DisplayMode.TB180);

        nameModeMap.put(modes.get(6), DisplayMode.Mono360);
        nameModeMap.put(modes.get(7), DisplayMode.LR360);
        nameModeMap.put(modes.get(8), DisplayMode.TB360);

        modeNameMap.put(DisplayMode.Mono, modes.get(0));
        modeNameMap.put(DisplayMode.LR3D, modes.get(1));
        modeNameMap.put(DisplayMode.TB3D, modes.get(2));

        modeNameMap.put(DisplayMode.Mono180, modes.get(3));
        modeNameMap.put(DisplayMode.LR180, modes.get(4));
        modeNameMap.put(DisplayMode.TB180, modes.get(5));

        modeNameMap.put(DisplayMode.Mono360, modes.get(6));
        modeNameMap.put(DisplayMode.LR360, modes.get(7));
        modeNameMap.put(DisplayMode.TB360, modes.get(8));
    }

    private final VideoPlayerGUI videoPlayerGUI;
    private ArrayList<TextButton> textButtons = new ArrayList<>();

    public ModeLayout(final VideoPlayerGUI videoPlayerGUI) {
        super(new VirtualStage(videoPlayerGUI.getSpriteBatch(), 360, 360), videoPlayerGUI.getSkin());
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



        for (int i = 0; i < modes.size; i++) {
            final TextButton textButton = new TextButton(modes.get(i), skin);
            final Cell<TextButton> cell = table.add(textButton).expandX().fill().center().pad(padding);
            if (i % 3 == 2 && i < modes.size - 1) cell.row();
            textButtons.add(textButton);
            final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
            if (videoPlayerGUI.getVideoOptions().modeSelection == i) {
                videoPlayer.setDisplayMode(nameModeMap.get(textButton.getText().toString()));
                textButton.setChecked(true);
            }
            final int index = i;
            textButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    videoPlayer.setDisplayMode(nameModeMap.get(textButton.getText().toString()));
                    videoPlayerGUI.getVideoOptions().modeSelection = index;
                    for (TextButton b : textButtons) {
                        b.setChecked(false);
                    }
                    textButton.setChecked(true);
                }
            });
        }
        setVisible(false);
    }
}
