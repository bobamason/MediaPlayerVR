package net.masonapps.mediaplayervr.shaders;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Bob on 12/1/2016.
 */

public class IntensityAttribute extends Attribute {
    public final static String Alias = "intensity";
    public final static long ID = register(Alias);

    public float intensity;

    public IntensityAttribute(float intensity) {
        super(ID);
        this.intensity = intensity;
    }

    @Override
    public Attribute copy() {
        return new IntensityAttribute(intensity);
    }

    @Override
    protected boolean equals(Attribute other) {
        return ((IntensityAttribute) other).intensity == intensity;
    }

    @Override
    public int compareTo(Attribute o) {
        if (type != o.type) return type < o.type ? -1 : 1;
        float otherValue = ((IntensityAttribute) o).intensity;
        return MathUtils.isEqual(intensity, otherValue) ? 0 : (intensity < otherValue ? -1 : 1);
    }
}