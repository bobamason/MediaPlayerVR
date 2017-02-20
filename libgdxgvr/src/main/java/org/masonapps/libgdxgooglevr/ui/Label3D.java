package org.masonapps.libgdxgooglevr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Created by Bob on 8/19/2015.
 */
public class Label3D {

    private static final Color tempColor = new Color();
    private static final GlyphLayout prefSizeLayout = new GlyphLayout();
    private static final Vector3 dir = new Vector3();
    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();

    private final Vector3 position = new Vector3();
    private final Quaternion rotation = new Quaternion();
    private final Quaternion rotator = new Quaternion();
    private final Matrix4 transform = new Matrix4();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 prefSize = new Vector2();
    private final StringBuilder text = new StringBuilder();
    private boolean updated = false;
    private Label.LabelStyle style;
    private BitmapFontCache cache;
    private int labelAlign = Align.left;
    private int lineAlign = Align.left;
    private boolean wrap;
    private float lastPrefHeight;
    private boolean prefSizeInvalid = true;
    private float fontScaleX = 1, fontScaleY = 1;
    private String ellipsis;
    private Color color = new Color(1, 1, 1, 1);
    private boolean needsLayout = true;

    public Label3D(Label.LabelStyle style) {
        this.style = style;
        this.cache = style.font.newFontCache();
    }

    public Label3D(String text, Label.LabelStyle style) {
        this(style);
        setText(text);
    }

    public void setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        updated = false;
    }

    public void setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        updated = false;
    }

    public void setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        updated = false;
    }

    public void rotateX(float angle) {
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        updated = false;
    }

    public void rotateY(float angle) {
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        updated = false;
    }

    public void rotateZ(float angle) {
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        updated = false;
    }

    public void setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        updated = false;
    }

    public void setRotation(Vector3 dir, Vector3 up) {
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        updated = false;
    }

    public void lookAt(Vector3 position, Vector3 up) {
        dir.set(position).sub(this.position).nor();
        setRotation(dir, up);
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion q) {
        rotation.set(q);
        updated = false;
    }

    public void translateX(float units) {
        this.position.x += units;
        updated = false;
    }

    public float getX() {
        return this.position.x;
    }

    public void setX(float x) {
        this.position.x = x;
        updated = false;
    }

    public void translateY(float units) {
        this.position.y += units;
        updated = false;
    }

    public float getY() {
        return this.position.y;
    }

    public void setY(float y) {
        this.position.y = y;
        updated = false;
    }

    public void translateZ(float units) {
        this.position.z += units;
        updated = false;
    }

    public float getZ() {
        return this.position.z;
    }

    public void setZ(float z) {
        this.position.z = z;
        updated = false;
    }

    public void translate(float x, float y, float z) {
        this.position.add(x, y, z);
        updated = false;
    }

    public void translate(Vector3 trans) {
        this.position.add(trans);
        updated = false;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        updated = false;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 pos) {
        this.position.set(pos);
        updated = false;
    }

    public boolean textEquals(CharSequence other) {
        int length = text.length;
        char[] chars = text.chars;
        if (length != other.length()) return false;
        for (int i = 0; i < length; i++)
            if (chars[i] != other.charAt(i)) return false;
        return true;
    }

    public StringBuilder getText() {
        return text;
    }

    public void setText(CharSequence newText) {
        if (newText == null) newText = "";
        if (newText instanceof StringBuilder) {
            if (text.equals(newText)) return;
            text.setLength(0);
            text.append((StringBuilder) newText);
        } else {
            if (textEquals(newText)) return;
            text.setLength(0);
            text.append(newText);
        }
        invalidateHierarchy();
    }

    public void invalidateHierarchy() {
        invalidate();
    }

    private void scaleAndComputePrefSize() {
        BitmapFont font = cache.getFont();
        float oldScaleX = font.getScaleX();
        float oldScaleY = font.getScaleY();
        if (fontScaleX != oldScaleX || fontScaleY != oldScaleY)
            font.getData().setScale(fontScaleX, fontScaleY);

        computePrefSize();

        if (fontScaleX != oldScaleX || fontScaleY != oldScaleY)
            font.getData().setScale(oldScaleX, oldScaleY);
    }

    private void computePrefSize() {
        prefSizeInvalid = false;
        GlyphLayout prefSizeLayout = Label3D.prefSizeLayout;
        if (wrap && ellipsis == null) {
            float width = getWidth();
            if (style.background != null)
                width -= style.background.getLeftWidth() + style.background.getRightWidth();
            prefSizeLayout.setText(cache.getFont(), text, Color.WHITE, width, Align.left, true);
        } else
            prefSizeLayout.setText(cache.getFont(), text);
        prefSize.set(prefSizeLayout.width, prefSizeLayout.height);
    }

    public void layout() {
        BitmapFont font = cache.getFont();
        float oldScaleX = font.getScaleX();
        float oldScaleY = font.getScaleY();
        if (fontScaleX != oldScaleX || fontScaleY != oldScaleY)
            font.getData().setScale(fontScaleX, fontScaleY);

        boolean wrap = this.wrap && ellipsis == null;
        if (wrap) {
            float prefHeight = getPrefHeight();
            if (prefHeight != lastPrefHeight) {
                lastPrefHeight = prefHeight;
                invalidateHierarchy();
            }
        }

        float width = getWidth(), height = getHeight();
        Drawable background = style.background;
        float x = 0, y = 0;
        if (background != null) {
            x = background.getLeftWidth();
            y = background.getBottomHeight();
            width -= background.getLeftWidth() + background.getRightWidth();
            height -= background.getBottomHeight() + background.getTopHeight();
        }

        GlyphLayout layout = this.layout;
        float textWidth, textHeight;
        if (wrap || text.indexOf("\n") != -1) {
            // If the text can span multiple lines, determine the text's actual size so it can be aligned within the label.
            layout.setText(font, text, 0, text.length, Color.WHITE, width, lineAlign, wrap, ellipsis);
            textWidth = layout.width;
            textHeight = layout.height;

            if ((labelAlign & Align.left) == 0) {
                if ((labelAlign & Align.right) != 0)
                    x += width - textWidth;
                else
                    x += (width - textWidth) / 2;
            }
        } else {
            textWidth = width;
            textHeight = font.getData().capHeight;
        }

        if ((labelAlign & Align.top) != 0) {
            y += cache.getFont().isFlipped() ? 0 : height - textHeight;
            y += style.font.getDescent();
        } else if ((labelAlign & Align.bottom) != 0) {
            y += cache.getFont().isFlipped() ? height - textHeight : 0;
            y -= style.font.getDescent();
        } else {
            y += (height - textHeight) / 2;
        }
        if (!cache.getFont().isFlipped()) y += textHeight;

        layout.setText(font, text, 0, text.length, Color.WHITE, textWidth, lineAlign, wrap, ellipsis);
        cache.setText(layout, x, y);

        if (fontScaleX != oldScaleX || fontScaleY != oldScaleY)
            font.getData().setScale(oldScaleX, oldScaleY);
    }

    public void draw(Batch batch) {
        validate();
        if (!updated) {
            transform.set(position, rotation);
            updated = true;
        }
        batch.setTransformMatrix(transform);
        Color color = tempColor.set(getColor());
        if (style.background != null) {
            batch.setColor(color.r, color.g, color.b, color.a);
            style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
        if (style.fontColor != null) color.mul(style.fontColor);
//        cache.tint(color);
//        cache.setPosition(0, 0);
//        cache.draw(batch);
        style.font.draw(batch, text, 0, 0, getWidth(), getLabelAlign(), wrap);
    }

    private void validate() {
        if (!needsLayout) return;
        layout();
        needsLayout = false;
    }

    public float getPrefWidth() {
        if (wrap) return 0;
        if (prefSizeInvalid) scaleAndComputePrefSize();
        float width = prefSize.x;
        Drawable background = style.background;
        if (background != null) width += background.getLeftWidth() + background.getRightWidth();
        return width;
    }

    public float getPrefHeight() {
        if (prefSizeInvalid) scaleAndComputePrefSize();
        float height = prefSize.y - style.font.getDescent() * fontScaleY * 2;
        Drawable background = style.background;
        if (background != null) height += background.getTopHeight() + background.getBottomHeight();
        return height;
    }

    public GlyphLayout getGlyphLayout() {
        return layout;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
        invalidateHierarchy();
    }

    public int getLabelAlign() {
        return labelAlign;
    }

    public int getLineAlign() {
        return lineAlign;
    }

    /**
     * @param alignment Aligns all the text within the label (default left center) and each line of text horizontally (default
     *                  left).
     * @see Align
     */
    public void setAlignment(int alignment) {
        setAlignment(alignment, alignment);
    }

    /**
     * @param labelAlign Aligns all the text within the label (default left center).
     * @param lineAlign  Aligns each line of text horizontally (default left).
     * @see Align
     */
    public void setAlignment(int labelAlign, int lineAlign) {
        this.labelAlign = labelAlign;

        if ((lineAlign & Align.left) != 0)
            this.lineAlign = Align.left;
        else if ((lineAlign & Align.right) != 0)
            this.lineAlign = Align.right;
        else
            this.lineAlign = Align.center;

        invalidate();
    }

    public void setFontScale(float fontScale) {
        this.fontScaleX = fontScale;
        this.fontScaleY = fontScale;
        invalidateHierarchy();
    }

    public void setFontScale(float fontScaleX, float fontScaleY) {
        this.fontScaleX = fontScaleX;
        this.fontScaleY = fontScaleY;
        invalidateHierarchy();
    }

    public float getFontScaleX() {
        return fontScaleX;
    }

    public void setFontScaleX(float fontScaleX) {
        this.fontScaleX = fontScaleX;
        invalidateHierarchy();
    }

    public float getFontScaleY() {
        return fontScaleY;
    }

    public void setFontScaleY(float fontScaleY) {
        this.fontScaleY = fontScaleY;
        invalidateHierarchy();
    }

    public void setFontSize(float size) {
        setFontScale(size / cache.getFont().getLineHeight());
    }

    public float getWidth() {
        return getPrefWidth();
    }

    public float getHeight() {
        return getPrefHeight();
    }

    public void invalidate() {
        prefSizeInvalid = true;
        needsLayout = true;
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    /**
     * Returns the color the actor will be tinted when drawn. The returned instance can be modified to change the color.
     */
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    /**
     * When non-null the text will be truncated "..." if it does not fit within the width of the label. Wrapping will not occur
     * when ellipsis is enabled. Default is false.
     */
    public void setEllipsis(String ellipsis) {
        this.ellipsis = ellipsis;
    }

    /**
     * When true the text will be truncated "..." if it does not fit within the width of the label. Wrapping will not occur when
     * ellipsis is true. Default is false.
     */
    public void setEllipsis(boolean ellipsis) {
        if (ellipsis)
            this.ellipsis = "...";
        else
            this.ellipsis = null;
    }

    public String toString() {
        return super.toString() + ": " + text;
    }
}
