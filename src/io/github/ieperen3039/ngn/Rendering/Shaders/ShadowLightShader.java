package io.github.ieperen3039.ngn.Rendering.Shaders;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.Textures.Texture;

public interface ShadowLightShader extends LightShader {
    void setLightWithShadows(Matrix4fc lightSpace, Color4f color, float intensity, Texture shadowMap);
}
