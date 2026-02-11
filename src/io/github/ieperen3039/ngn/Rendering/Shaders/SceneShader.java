package io.github.ieperen3039.ngn.Rendering.Shaders;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.Toolbox;
import org.joml.*;
import java.io.IOException;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

/**
 * An abstract shader that initializes a view-projection matrix, a model matrix, and a normal matrix. allows for setting
 * multiple unforms, and gives utility methods as {@link #createLightsUniform(String, int)}
 * @author Yoeri Poels
 * @author Geert van Ieperen
 */
public abstract class SceneShader implements ShaderProgram, MaterialShader, LightShader {

    protected final ShaderUniforms uniforms;

    private int programId;
    private int vertexShaderID;
    private int geometryShaderID;
    private int fragmentShaderID;

    /**
     * create a shader and manages the interaction of its uniforms. This initializer must be called on the main thread
     * @param vertexPath   the path to the vertex shader, or null for the standard implementation
     * @param geometryPath the path to the geometry shader, or null for the standard implementation
     * @param fragmentPath the path to the fragment shader, or null for the standard implementation
     * @throws ShaderException if a new shader could not be created for internal reasons
     * @throws IOException     if the defined files could not be found (the file is searched for in the shader folder
     *                         itself, and should exclude any first slash)
     */
    public SceneShader(Resource.Path vertexPath, Resource.Path geometryPath, Resource.Path fragmentPath) throws ShaderException, IOException {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new ShaderException("OpenGL error: Could not create Shader");
        }

        if (vertexPath != null) {
            final String shaderCode = vertexPath.asText();
            vertexShaderID = ShaderProgram.createShader(programId, GL_VERTEX_SHADER, shaderCode);
        }

        if (geometryPath != null) {
            final String shaderCode = geometryPath.asText();
            geometryShaderID = ShaderProgram.createShader(programId, GL_GEOMETRY_SHADER, shaderCode);
        }

        if (fragmentPath != null) {
            final String shaderCode = fragmentPath.asText();
            fragmentShaderID = ShaderProgram.createShader(programId, GL_FRAGMENT_SHADER, shaderCode);
        }

        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderID != 0) {
            glDetachShader(programId, vertexShaderID);
        }

        if (geometryShaderID != 0) {
            glDetachShader(programId, geometryShaderID);
        }

        if (fragmentShaderID != 0) {
            glDetachShader(programId, fragmentShaderID);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
            Logger.WARN.print("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        uniforms = new ShaderUniforms(programId);

        // Create uniforms for world and projection matrices
        uniforms.createUniform("viewProjectionMatrix");
        uniforms.createUniform("modelMatrix");
        uniforms.createUniform("normalMatrix");

        Toolbox.checkGLError(toString());
    }

    @Override
    public void bind() {
        glUseProgram(programId);
    }

    @Override
    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public void cleanup() {
        if (programId != 0) {
            unbind();
            glDeleteProgram(programId);
            programId = 0;
        }
    }

    public void setProjectionMatrix(Matrix4fc viewProjectionMatrix) {
        uniforms.setUniform("viewProjectionMatrix", viewProjectionMatrix);
    }

    public void setModelMatrix(Matrix4fc modelMatrix) {
        uniforms.setUniform("modelMatrix", modelMatrix);
    }

    public void setNormalMatrix(Matrix3fc normalMatrix) {
        uniforms.setUniform("normalMatrix", normalMatrix);
    }

    /**
     * Create an uniform for a point-light array.
     * @param name the name of the uniform in the shader
     * @param size The size of the array.
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    protected void createLightsUniform(String name, int size) throws ShaderException {
        for (int i = 0; i < size; i++) {
            try {
                uniforms.createUniform((name + "[" + i + "]") + ".color");
                uniforms.createUniform((name + "[" + i + "]") + ".mPosition");
                uniforms.createUniform((name + "[" + i + "]") + ".intensity");

            } catch (ShaderException ex) {
                if (i == 0) {
                    throw ex;
                } else {
                    throw new IllegalArgumentException(
                            "Number of lights in shader is not equal to program value (" + (i - 1) + " instead of " + size + ")", ex);
                }
            }
        }
    }

    protected void setLightsUniform(String name, int index, Color4f color, Vector3fc position, float intensity)
    {
        uniforms.setUniform((name + "[" + index + "]") + ".color", color.rawVector3f());
        uniforms.setUniform((name + "[" + index + "]") + ".mPosition", position);
        uniforms.setUniform((name + "[" + index + "]") + ".intensity", color.alpha * intensity);
    }
}
