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
import net.masonapps.mediaplayervr.video.VideoMode;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.util.ArrayList;

/**
 * Created by Bob on 2/8/2017.
 */

public class ModeLayout extends SingleStageUi {
    private static ObjectMap<String, VideoMode> nameModeMap = new ObjectMap<>();
    private static ObjectMap<VideoMode, String> modeNameMap = new ObjectMap<>();
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
        nameModeMap.put(modes.get(0), VideoMode.Mono);
        nameModeMap.put(modes.get(1), VideoMode.LR3D);
        nameModeMap.put(modes.get(2), VideoMode.TB3D);

        nameModeMap.put(modes.get(3), VideoMode.Mono180);
        nameModeMap.put(modes.get(4), VideoMode.LR180);
        nameModeMap.put(modes.get(5), VideoMode.TB180);

        nameModeMap.put(modes.get(6), VideoMode.Mono360);
        nameModeMap.put(modes.get(7), VideoMode.LR360);
        nameModeMap.put(modes.get(8), VideoMode.TB360);

        modeNameMap.put(VideoMode.Mono, modes.get(0));
        modeNameMap.put(VideoMode.LR3D, modes.get(1));
        modeNameMap.put(VideoMode.TB3D, modes.get(2));

        modeNameMap.put(VideoMode.Mono180, modes.get(3));
        modeNameMap.put(VideoMode.LR180, modes.get(4));
        modeNameMap.put(VideoMode.TB180, modes.get(5));

        modeNameMap.put(VideoMode.Mono360, modes.get(6));
        modeNameMap.put(VideoMode.LR360, modes.get(7));
        modeNameMap.put(VideoMode.TB360, modes.get(8));
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
                videoPlayer.setVideoMode(nameModeMap.get(textButton.getText().toString()));
                textButton.setChecked(true);
            }
            final int index = i;
            textButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    videoPlayer.setVideoMode(nameModeMap.get(textButton.getText().toString()));
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
