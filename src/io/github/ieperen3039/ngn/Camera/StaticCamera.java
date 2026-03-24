package io.github.ieperen3039.ngn.Camera;


import io.github.ieperen3039.ngn.Core.Main;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 22-12-2017. a camera that doesn't move
 */
public class StaticCamera implements Camera {
    private Vector3fc eye, focus;
    private Vector3fc up;
    private boolean isometric;
    private float fov;
    private float zNear;
    private float zFar;

    public StaticCamera(Vector3fc eye, Vector3fc focus, Vector3fc up, float fov, float zNear, float zFar) {
        this(eye, focus, up, fov, zNear, zFar, false);
    }

    public StaticCamera(Vector3fc eye, Vector3fc focus, Vector3fc up) {
        this(eye, focus, up, (float) Math.toRadians(45), 0.1f, 1000.0f, false);
    }

    public static StaticCamera isometric(Vector3fc eye, Vector3fc focus, Vector3fc up, float zNear, float zFar){
        return new StaticCamera(eye, focus, up, 0, zNear, zFar, true);
    }

    private StaticCamera(Vector3fc eye, Vector3fc focus, Vector3fc up, float fov, float zNear, float zFar, boolean isometric) {
        this.eye = eye;
        this.focus = focus;
        this.up = up;
        this.fov = fov;
        this.zNear = zNear;
        this.zFar = zFar;
    }

    @Override
    public void init(Main root) {

    }

    @Override
    public Vector3f vectorToFocus() {
        return new Vector3f(focus).sub(eye);
    }

    @Override
    public void updatePosition(float deltaTime) {

    }

    @Override
    public Vector3fc getEye() {
        return eye;
    }

    @Override
    public Vector3fc getFocus() {
        return focus;
    }

    @Override
    public Vector3fc getUpVector() {
        return up;
    }

    @Override
    public void set(Vector3fc focus) {
        this.focus = new Vector3f(focus);
        this.eye = new Vector3f(eye);
    }

    @Override
    public void cleanup() {

    }
    
    @Override
    public Matrix4f getProjectionMatrix(float aspectRatio) {
        Matrix4f vpMatrix = new Matrix4f();

        if (isometric) {
            float visionSize = (vectorToFocus().length() - zNear) / 2;
            vpMatrix.setOrthoSymmetric(aspectRatio * visionSize, visionSize, zNear, zFar);
        } else {
            vpMatrix.setPerspective(fov, aspectRatio, zNear, zFar);
        }
        return vpMatrix;
    }

    @Override
    public void onMouseMove(int xDelta, int yDelta, float xPos, float yPos) {

    }

    @Override
    public void onScroll(float value) {

    }

    @Override
    public void onClick(int button, int xRel, int yRel) {

    }

    @Override
    public void onRelease(int button) {

    }
}
