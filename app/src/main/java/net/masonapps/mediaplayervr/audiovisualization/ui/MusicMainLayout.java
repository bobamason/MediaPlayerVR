package net.masonapps.mediaplayervr.audiovisualization.ui;

import android.media.MediaPlayer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.video.ui.VideoPlayerGUI;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

/**
 * Created by Bob on 2/13/2017.
 */

public class MusicMainLayout implements BaseUiLayout {

    public Table table;
    public Label timeLabel;
    public Slider slider;
    public ImageButton playButton;
    private MusicPlayerUI musicPlayerUI;

    public MusicMainLayout(final MusicPlayerUI musicPlayerUI) {
        this.musicPlayerUI = musicPlayerUI;
        final Skin skin = musicPlayerUI.getSkin();
        table = new Table(skin);
        table.padTop(musicPlayerUI.getHeaderHeight());
        table.setFillParent(true);
        table.center();

        final Drawable pauseUp = skin.newDrawable(Style.Drawables.ic_pause_circle_filled_white_48dp, Style.COLOR_UP);
        final Drawable pauseDown = skin.newDrawable(Style.Drawables.ic_pause_circle_filled_white_48dp, Style.COLOR_DOWN);
        final Drawable playUp = skin.newDrawable(Style.Drawables.ic_play_circle_filled_white_48dp, Style.COLOR_UP);
        final Drawable playDown = skin.newDrawable(Style.Drawables.ic_play_circle_filled_white_48dp, Style.COLOR_DOWN);
        playButton = new ImageButton(pauseUp, pauseDown);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final MediaPlayer player = musicPlayerUI.getMusicVisualizerScreen().getMediaPlayer();
                if (!musicPlayerUI.getMusicVisualizerScreen().isLoading()) {
                    if (player.isPlaying()) {
                        player.pause();
                        playButton.getStyle().imageUp = playUp;
                        playButton.getStyle().imageDown = playDown;
                    } else {
                        player.start();
                        playButton.getStyle().imageUp = pauseUp;
                        playButton.getStyle().imageDown = pauseDown;
                    }
                }
            }
        });
        table.add(playButton).pad(VideoPlayerGUI.padding);

        slider = new Slider(0f, 1f, 0.00001f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final MediaPlayer player = musicPlayerUI.getMusicVisualizerScreen().getMediaPlayer();
                if (!musicPlayerUI.getMusicVisualizerScreen().isLoading()) {
                    player.seekTo(Math.round(slider.getValue() * player.getDuration()));
                }
            }
        });
        table.add(slider).padTop(VideoPlayerGUI.padding).padBottom(VideoPlayerGUI.padding).padRight(VideoPlayerGUI.padding).colspan(4).expandX().fillX();

        timeLabel = new Label("0:00:00 / 0:00:00", skin);
        table.add(timeLabel).padTop(VideoPlayerGUI.padding).padBottom(VideoPlayerGUI.padding).padRight(VideoPlayerGUI.padding).row();
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
