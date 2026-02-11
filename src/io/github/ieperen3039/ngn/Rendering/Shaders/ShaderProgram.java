package io.github.ieperen3039.ngn.Rendering.Shaders;

import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;

import static org.lwjgl.opengl.GL20.*;

/**
 * @author Geert van Ieperen created on 7-1-2018.
 */
public interface ShaderProgram {
    Resource.Path SHADER_DIRECTORY = Resource.Path.get("ngn/shaders");

    /** shaders and meshes must use these shader locations */
    int VERTEX_LOCATION = 0;
    int NORMAL_LOCATION = 1;
    int COLOR_LOCATION = 2;
    int TEXTURE_LOCATION = 3;

    /**
     * Bind the renderer to the current rendering state
     */
    void bind();

    /**
     * Unbind the renderer from the current rendering state
     */
    void unbind();

    /**
     * Cleanup the renderer to a state of disposal
     */
    void cleanup();

    /**
     * initialize the uniforms for this shader and return a rendering state object
     */
    SGL getGL(Main main);

    /**
     * Create a new shader and return the id of the newly created shader.
     * 
     * @param shaderType The type of shader, e.g. GL_VERTEX_SHADER.
     * @param shaderCode The shaderCode as a String.
     * @return The id of the newly created shader.
     * @throws ShaderException If an error occurs during the creation of a shader.
     */
    static int createShader(int programId, int shaderType, String shaderCode) throws ShaderException {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new ShaderException("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new ShaderException("Error compiling Shader code:\n" + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }
}
