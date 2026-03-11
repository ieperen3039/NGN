package io.github.ieperen3039.ngn.Rendering.Shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;

import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Rendering.Textures.Texture;
import io.github.ieperen3039.ngn.Tools.Logger;

public abstract class PostProcessingStep {
    protected final ShaderUniforms uniforms;

    private final int programId;
    private final int fragmentShaderId;
    private final int quadVao;
    private final int quadVbo;

    /**
     * create a PostProcessing shader based on just a framgent shader. The fragment shader accepts one input:
     * ```
     * in vec2 vTexCoord;
     * ```
     * and:
     * ```
     * uniform sampler2D texture_sampler;
     * ```
     * 
     * @param fragmentPath the path to the fragment shader
     * @throws ShaderException if a new shader could not be created for internal
     *                         reasons
     * @throws IOException     if the defined files could not be found (the file is
     *                         searched for in the shader folder
     *                         itself, and should exclude any first slash)
     */
    public PostProcessingStep(Resource.Path fragmentPath, int windowWidth, int windowHeight)
            throws ShaderException, IOException {
        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new ShaderException("OpenGL error: Could not create Shader");
        }
        final String fragmentCode = fragmentPath.asText();
        fragmentShaderId = ShaderProgram.createShader(programId, GL_FRAGMENT_SHADER, fragmentCode);

        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        glDetachShader(programId, fragmentShaderId);

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
            Logger.WARN.print("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        uniforms = new ShaderUniforms(programId);
        uniforms.createUniform("texture_sampler");

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Fullscreen quad (hardcoded here for efficiency)
        float[] quadVertices = {
                // positions // texCoords
                -1f, 1f, 0f, 1f,
                -1f, -1f, 0f, 0f,
                1f, -1f, 1f, 0f,

                -1f, 1f, 0f, 1f,
                1f, -1f, 1f, 0f,
                1f, 1f, 1f, 1f,
        };

        quadVao = glGenVertexArrays();
        quadVbo = glGenBuffers();

        glBindVertexArray(quadVao);
        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);

        // position (location = 0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // texCoord (location = 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    // draws the given texture, applying this post processing step to it.
    // make sure to bind a render target before calling this method.
    public void draw(Texture input) {
        glUseProgram(programId);

        input.attach(GL_TEXTURE0);
        uniforms.setUniform("texture_sampler", 0);

        preparePostProcessing();

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glUseProgram(0);
    }

    protected abstract void preparePostProcessing();

    public void cleanup() {
        glDeleteVertexArrays(quadVao);
        glDeleteBuffers(quadVbo);

        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
}
