package net.masonapps.mediaplayervr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import org.masonapps.libgdxgooglevr.gfx.Entity;

/**
 * Created by Bob on 2/11/2017.
 */

public class Style {

    public static final String FONT_REGION = "Roboto-hdpi";
    public static final String FONT_FILE = "skin/Roboto-hdpi.fnt";
    public static final String ATLAS_FILE = "skin/uiskin.pack";
    public static final Color COLOR_UP = new Color(0x00000000);
    public static final Color COLOR_DOWN = new Color(0xcccccc99);
    public static final Color COLOR_OVER = new Color(0xcccccc66);
    public static final Color COLOR_UP_2 = new Color(Color.WHITE);
    public static final Color COLOR_DOWN_2 = new Color(Color.LIGHT_GRAY);
    public static final Color COLOR_OVER_2 = new Color(Color.GRAY);
    public static final Color COLOR_DISABLED = new Color(Color.GRAY);
    public static final Color COLOR_WINDOW = new Color(Color.DARK_GRAY);
    public static final String DEFAULT = "default";
    public static final String TOGGLE = "toggle";
    public static final String LIST_ITEM = "list_item";

    public static ImageButton.ImageButtonStyle createImageButtonStyle(Skin skin, String name, boolean useBackground) {
        final ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
        imageButtonStyle.imageUp = skin.newDrawable(name, COLOR_UP_2);
        imageButtonStyle.imageDown = skin.newDrawable(name, useBackground ? COLOR_UP_2 : COLOR_DOWN_2);
        imageButtonStyle.imageOver = skin.newDrawable(name, useBackground ? COLOR_UP_2 : COLOR_OVER_2);
        imageButtonStyle.imageDisabled = useBackground ? skin.newDrawable(name, COLOR_DISABLED) : null;
        imageButtonStyle.up = useBackground ? skin.newDrawable(Drawables.button, COLOR_UP) : null;
        imageButtonStyle.down = useBackground ? skin.newDrawable(Drawables.button, COLOR_DOWN) : null;
        imageButtonStyle.over = useBackground ? skin.newDrawable(Drawables.button, COLOR_OVER) : null;
        imageButtonStyle.disabled = useBackground ? skin.newDrawable(Drawables.button, COLOR_UP) : null;
        return imageButtonStyle;
    }

    public static ImageTextButton.ImageTextButtonStyle createImageTextButtonStyle(Skin skin, String name) {
        final ImageTextButton.ImageTextButtonStyle imageTextButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        imageTextButtonStyle.font = skin.getFont(Style.DEFAULT);
        imageTextButtonStyle.up = skin.newDrawable(Style.Drawables.button, Style.COLOR_UP);
        imageTextButtonStyle.over = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        imageTextButtonStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        imageTextButtonStyle.checked = null;
        imageTextButtonStyle.fontColor = Color.WHITE;
        imageTextButtonStyle.imageUp = skin.newDrawable(name, Style.COLOR_UP_2);
        return imageTextButtonStyle;
    }

    public static Image newBackgroundImage(Skin skin) {
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, Color.BLACK));
        bg.setFillParent(true);
        return bg;
    }

    public static Entity newGradientBackground(float r) {
        return GradientSphere.newInstance(r, 32, 16, Color.BLACK, Color.DARK_GRAY);
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
        public static final String ic_brightness_white_48dp = "ic_brightness_white_48dp";
        public static final String ic_palette_white_48dp = "ic_palette_white_48dp";
        public static final String window = "window";
        public static final String button = "button";
        public static final String round_button = "round_button";
        public static final String slider = "slider";
        public static final String slider_knob = "slider_knob";
        public static final String controller_swipe = "controller_swipe";
        public static final String loading_spinner = "loading_spinner";
    }
}
