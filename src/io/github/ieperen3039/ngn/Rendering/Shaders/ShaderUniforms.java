package io.github.ieperen3039.ngn.Rendering.Shaders;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;

public class ShaderUniforms {
    
    private int programId;
    private final Map<String, Integer> uniforms = new HashMap<>();

    public ShaderUniforms(int programId)
    {
        this.programId = programId;
    }

    /**
     * Create a new uniform and get its memory location.
     * @param uniformName The name of the uniform.
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    public void createUniform(String uniformName) throws ShaderException {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new ShaderException("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    /**
     * Set the value of a 4x4 matrix shader uniform.
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    public void setUniform(String uniformName, Matrix4fc value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(unif(uniformName), false, fb);
        }
    }

    /**
     * Set the value of a 3x3 matrix shader uniform.
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    public void setUniform(String uniformName, Matrix3fc value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(9);
            value.get(fb);
            glUniformMatrix3fv(unif(uniformName), false, fb);
        }
    }

    /**
     * Set the value of a certain integer shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    public void setUniform(String uniformName, int value) {
        glUniform1i(unif(uniformName), value);
    }

    /**
     * Set the value of a certain float shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    public void setUniform(String uniformName, float value) {
        glUniform1f(unif(uniformName), value);
    }

    /**
     * Set the value of a certain 3D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    public void setUniform(String uniformName, Vector3fc value) {
        glUniform3f(unif(uniformName), value.x(), value.y(), value.z());
    }

    /**
     * Set the value of a certain 2D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    public void setUniform(String uniformName, Vector2fc value) {
        glUniform2f(unif(uniformName), value.x(), value.y());
    }

    private int unif(String uniformName) {
        try {
            return uniforms.get(uniformName);
        } catch (NullPointerException ex) {
            throw new ShaderException("Uniform '" + uniformName + "' does not exist");
        }
    }

    public void setUniform(String uniformName, float[] value) {
        glUniform4f(unif(uniformName), value[0], value[1], value[2], value[3]);
    }

    /**
     * Set the value of a certain 4D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    public void setUniform(String uniformName, Vector4fc value) {
        glUniform4f(unif(uniformName), value.x(), value.y(), value.z(), value.w());
    }

    public void setUniform(String uniformName, boolean value) {
        setUniform(uniformName, value ? 1 : 0);
    }

    public void setUniform(String uniformName, Color4f color) {
        glUniform4f(unif(uniformName), color.red, color.green, color.blue, color.alpha);
    }
}
