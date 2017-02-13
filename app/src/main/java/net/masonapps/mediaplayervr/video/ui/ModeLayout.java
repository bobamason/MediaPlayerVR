package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import net.masonapps.mediaplayervr.video.VideoMode;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.Attachable;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.util.ArrayList;

/**
 * Created by Bob on 2/8/2017.
 */

public class ModeLayout implements Attachable {
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

    private final Table table;
    private ArrayList<TextButton> textButtons = new ArrayList<>();

    public ModeLayout(VideoPlayerGUI videoPlayerGUI) {
        final Skin skin = videoPlayerGUI.getSkin();
        table = new Table(skin);
        table.padTop(videoPlayerGUI.getHeaderHeight());
        table.setFillParent(true);
        table.center();
        table.setVisible(false);


        for (int i = 0; i < modes.size; i++) {
            final TextButton textButton = new TextButton(modes.get(i), skin);
            final Cell<TextButton> cell = table.add(textButton).expandX().fill().center().pad(VideoPlayerGUI.PADDING);
            if (i % 3 == 2 && i < modes.size - 1) cell.row();
            textButtons.add(textButton);
        }
        for (final TextButton button : textButtons) {
            final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
            final VideoMode type = videoPlayer.getVideoMode();
            button.setChecked(type == nameModeMap.get(button.getText().toString()));
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    videoPlayer.setVideoMode(nameModeMap.get(button.getText().toString()));
                    for (TextButton b : textButtons) {
                        b.setChecked(false);
                    }
                    button.setChecked(true);
                }
            });
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
    }
}
