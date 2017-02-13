package net.masonapps.mediaplayervr.vrinterface;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

/**
 * Created by Bob on 2/8/2017.
 */

public interface Attachable {

    void attach(VirtualStage stage);

    boolean isVisible();

    void setVisible(boolean visible);
}
