package net.masonapps.mediaplayervr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by Bob on 2/11/2017.
 */

public class Style {

    public static final String FONT_REGION = "Roboto-hdpi";
    public static final String FONT_FILE = "skin/Roboto-hdpi.fnt";
    public static final String ATLAS_FILE = "skin/uiskin.pack";
    public static final Color COLOR_UP = new Color(0x0088aaff);
    public static final Color COLOR_DOWN = new Color(0x00aad4ff);
    public static final Color COLOR_OVER = new Color(0x2ad4ffff);
    public static final Color COLOR_UP_2 = new Color(Color.WHITE);
    public static final Color COLOR_DOWN_2 = new Color(Color.GRAY);
    public static final Color COLOR_OVER_2 = new Color(Color.LIGHT_GRAY);
    public static final Color COLOR_WINDOW = new Color(Color.DARK_GRAY);
    public static final String DEFAULT = "default";
    public static final String TOGGLE = "toggle";

    public static ImageButton.ImageButtonStyle getImageButtonStyle(Skin skin, String name, boolean useBackground) {
        final Drawable drawableUp = useBackground ? skin.newDrawable(Drawables.round_button, COLOR_UP) : null;
        final Drawable drawableDown = useBackground ? skin.newDrawable(Drawables.round_button, COLOR_DOWN) : null;
        final Drawable imageUp = skin.newDrawable(name, COLOR_UP_2);
        final Drawable imageDown = skin.newDrawable(name, COLOR_DOWN_2);
        return new ImageButton.ImageButtonStyle(drawableUp, drawableDown, null, imageUp, imageDown, null);
    }

    public static Image newBackgroundImage(Skin skin) {
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        bg.setFillParent(true);
        return bg;
    }

    public static class Drawables {
        public static final String ic_album_white_48dp = "ic_album_white_48dp";
        public static final String ic_movie_white_48dp = "ic_movie_white_48dp";
        public static final String ic_arrow_back_white_48dp = "ic_arrow_back_white_48dp";
        public static final String ic_close_white_48dp = "ic_close_white_48dp";
        public static final String ic_chevron_left_white_48dp = "ic_chevron_left_white_48dp";
        public static final String ic_chevron_right_white_48dp = "ic_chevron_right_white_48dp";
        public static final String ic_fast_forward_white_48dp = "ic_fast_forward_white_48dp";
        public static final String ic_fast_rewind_white_48dp = "ic_fast_rewind_white_48dp";
        public static final String ic_pause_circle_filled_white_48dp = "ic_pause_circle_filled_white_48dp";
        public static final String ic_play_circle_filled_white_48dp = "ic_play_circle_filled_white_48dp";
        public static final String ic_skip_next_white_48dp = "ic_skip_next_white_48dp";
        public static final String ic_skip_previous_white_48dp = "ic_skip_previous_white_48dp";
        public static final String ic_volume_down_white_48dp = "ic_volume_down_white_48dp";
        public static final String ic_volume_off_white_48dp = "ic_volume_off_white_48dp";
        public static final String ic_volume_up_white_48dp = "ic_volume_up_white_48dp";
        public static final String window = "window";
        public static final String button = "button";
        public static final String round_button = "round_button";
        public static final String slider = "slider";
        public static final String slider_knob = "slider_knob";
        public static final String controller_swipe = "controller_swipe";
        public static final String loading_spinner = "";
    }
}
