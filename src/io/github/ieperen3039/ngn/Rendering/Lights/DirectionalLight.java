package io.github.ieperen3039.ngn.Rendering.Lights;

import org.joml.Vector3fc;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;

public interface DirectionalLight {
    Color4f getColor();

    float getIntensity();

    Vector3fc getDirectionToLight();
}