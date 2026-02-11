/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.ieperen3039.ngn.Rendering.Lights;


import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.Serializable;

/**
 * A light source that is infinitely far away. Manages shadow mappings and light properties.
 * @author Dungeons-and-Drawings group
 * @author Geert van Ieperen
 */
public class DirectionalLight implements Serializable, Light {
    private static final float LIGHT_Z_NEAR = 0.5f;
    public static final int LIGHT_Z_FAR_MULTIPLIER = 2;
    public static final int LIGHT_CUBE_SIZE_MULTIPLIER = 2;
    private Color4f color;
    private final Vector3f direction;
    private float intensity;

    private Matrix4f ortho = new Matrix4f();
    private Matrix4f lightSpaceMatrix = new Matrix4f();

    private float lightCubeSize;

    public DirectionalLight(Color4f color, Vector3fc direction, float intensity) {
        this.color = color;
        this.direction = new Vector3f(direction);
        this.intensity = intensity;
    }

    public Vector3fc getDirectionToLight() {
        return direction;
    }

    public void setDirection(Vector3fc direction) {
        this.direction.set(direction).normalize();
    }

    public float getIntensity() {
        return intensity;
    }

    @Override
    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public Matrix4fc getLightSpaceMatrix() {
        return lightSpaceMatrix;
    }

    public Color4f getColor() {
        return color;
    }

    @Override
    public void setColor(Color4f color) {
        this.color = color;
    }

    public void setLightSize(float lightSize) {
        lightCubeSize = lightSize * LIGHT_CUBE_SIZE_MULTIPLIER;

        float zFar = lightCubeSize * LIGHT_Z_FAR_MULTIPLIER + LIGHT_Z_NEAR;
        ortho.setOrthoSymmetric(lightCubeSize, lightCubeSize, LIGHT_Z_NEAR, zFar);
    }
}
