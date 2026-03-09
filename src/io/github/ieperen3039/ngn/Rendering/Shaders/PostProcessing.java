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
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.Toolbox;

public abstract class PostProcessing implements ShaderProgram {

    protected final ShaderUniforms uniforms;

    private final int programId;
    private final int frameBuffer;
    private final int colorBuffer;
    private final int fragmentShaderId;
    private final int quadVao;
    private final int quadVbo;

    private ShaderProgram inner;

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
    public PostProcessing(Resource.Path fragmentPath, ShaderProgram inner, int windowWidth, int windowHeight)
            throws ShaderException, IOException {
        this.inner = inner;

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

        // Create FBO, texture, and fullscreen quad
        frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

        // Create a new OpenGL texture
        colorBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorBuffer);
        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, windowWidth, windowHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorBuffer, 0);
        glClearColor(1f, 1f, 1f, 1f); // white
        glClear(GL_COLOR_BUFFER_BIT);

        Toolbox.checkGLError(this.toString());

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

    @Override
    public void bind() {
        inner.bind();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
    }

    @Override
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        inner.unbind();
        // now apply the post-processing step
        apply();
    }

    private void apply() {
        glUseProgram(programId);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorBuffer);
        uniforms.setUniform("texture_sampler", 0);

        preparePostProcessing();

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glUseProgram(0);
    }

    protected abstract void preparePostProcessing();

    @Override
    public void cleanup() {
        glDeleteFramebuffers(frameBuffer);
        glDeleteVertexArrays(quadVao);
        glDeleteBuffers(quadVbo);
        glDeleteTextures(colorBuffer);

        if (programId != 0) {
            glDeleteProgram(programId);
        }
        inner.cleanup();
    }

    @Override
    public SGL getGL(Main main) {
        return inner.getGL(main);
    }
}
