package io.github.ieperen3039.ngn.Rendering.Lights;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;

public interface Light {

    void setIntensity(float intensity);

    void setColor(Color4f color);

}