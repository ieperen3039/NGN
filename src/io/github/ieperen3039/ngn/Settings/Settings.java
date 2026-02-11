package io.github.ieperen3039.ngn.Settings;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;

/**
 * A class that collects a number of settings.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Settings {
    public static String TITLE = "Untitled";

    // video settings
    public int TARGET_FPS = 60;
    public boolean V_SYNC = false;
    public int WINDOW_WIDTH = 1400;
    public int WINDOW_HEIGHT = 800;
    public int ANTIALIAS_LEVEL = 1;
    public static final float Z_NEAR = 0.05f;
    public static final float Z_FAR = 50f;
    public static final float FOV = (float) Math.toRadians(45);
    public Color4f AMBIENT_LIGHT = Color4f.rgb(200, 200, 255, 0.3f);

    // camera settings
    public boolean ISOMETRIC_VIEW = false;
    public float CAMERA_ZOOM_SPEED = 0.1f;
    public float MAX_CAMERA_DIST = Z_FAR * 0.75f;
    public float MIN_CAMERA_DIST = Z_NEAR * 2f;

    // UI settings
    public static final float CLICK_BOX_WIDTH = 1.5f;
    public static final float CLICK_BOX_HEIGHT = 0.1f;
    public static final float CLICK_BOX_RESOLUTION = 1f;

    // other
    public boolean PRINT_ROLL = false;
    public boolean ACCURATE_RENDER_TIMING = false;
    public boolean DEBUG = true;

}
