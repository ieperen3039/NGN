/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.ieperen3039.ngn.Rendering.Lights;


import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.Serializable;

/**
 * A light source that is infinitely far away. Manages shadow mappings and light properties.
 * @author Dungeons-and-Drawings group
 * @author Geert van Ieperen
 */
public class FixedDirectionalLight implements Serializable, DirectionalLight {
    private Color4f color;
    private final Vector3f direction;
    private float intensity;

    public FixedDirectionalLight(Vector3fc vectorToLight, Color4f color, float intensity) {
        this.color = color;
        this.direction = new Vector3f(vectorToLight);
        this.intensity = intensity;
    }

    @Override
    public Vector3fc getDirectionToLight() {
        return direction;
    }

    public void setDirection(Vector3fc direction) {
        this.direction.set(direction).normalize();
    }

    @Override
    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    @Override
    public Color4f getColor() {
        return color;
    }

    public void setColor(Color4f color) {
        this.color = color;
    }
}
