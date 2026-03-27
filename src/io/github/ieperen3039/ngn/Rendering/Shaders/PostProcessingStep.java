package io.github.ieperen3039.ngn.Rendering.Shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
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
    private static final Resource.Path VERTEX_PATH = ShaderProgram.SHADER_DIRECTORY.resolve("PostProcessing", "vertex.glsl");
    protected final ShaderUniforms uniforms;

    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;
    private final int quadVao;
    private final int quadVbo;
    private final int targetTextureWidth;
    private final int targetTextureHeight;
    private final float xOffset;
    private final float xScale;
    private final float yOffset;
    private final float yScale;

    /**
     * create a PostProcessing shader based on just a framgent shader. The fragment
     * shader accepts one input:
     * ```
     * in vec2 vTexCoord;
     * ```
     * and:
     * ```
     * uniform sampler2D texture_sampler;
     * ```
     * 
     * @throws ShaderException if a new shader could not be created for internal
     *                         reasons
     * @throws IOException     if the defined files could not be found (the file is
     *                         searched for in the shader folder
     *                         itself, and should exclude any first slash)
     * @see #PostProcessingStep(Resource.Path,int,int,float,float)
     */
    public PostProcessingStep(Resource.Path fragmentPath, int targetTextureWidth, int targetTextureHeight)
            throws ShaderException, IOException {
        this(fragmentPath, targetTextureWidth, targetTextureHeight, 0, 1, 0, 1);
    }

    /**
     * create a PostProcessing shader based on just a framgent shader. The fragment
     * shader accepts one input:
     * ```
     * in vec2 vTexCoord;
     * ```
     * and:
     * ```
     * uniform sampler2D texture_sampler;
     * ```
     * 
     * The output can be set to a subregion of the input texture, which appears like
     * a zoom factor of `(1 - widthStart - widthEnd) / width` in width, and 
     * `(1 - heightStart - heightEnd) / height` in height.
     * 
     * @param widthStart  This fraction of the lower x axis is cut off from the input
     * @param widthEnd    This fraction of the upper x axis is cut off from the input
     * @param heightStart This fraction of the lower y axis is cut off from the input
     * @param heightEnd   This fraction of the upper y axis is cut off from the input
     * @throws ShaderException if the fragment shader or the internal vertex shader
     *                         could not be created
     *                         (usually due to a syntax error in the shader code)
     * @throws IOException     if the Fragment Path does not refer to a valid text
     *                         file
     */
    public PostProcessingStep(
        Resource.Path fragmentPath, int targetTextureWidth, int targetTextureHeight,
        float widthStart, float widthEnd,
        float heightStart, float heightEnd
    ) throws ShaderException, IOException {
        this.targetTextureWidth = targetTextureWidth;
        this.targetTextureHeight = targetTextureHeight;

        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new ShaderException("OpenGL error: Could not create Shader");
        }

        final String vertexCode = VERTEX_PATH.asText();
        vertexShaderId = ShaderProgram.createShader(programId, GL_VERTEX_SHADER, vertexCode);

        final String fragmentCode = fragmentPath.asText();
        fragmentShaderId = ShaderProgram.createShader(programId, GL_FRAGMENT_SHADER, fragmentCode);

        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        glDetachShader(programId, fragmentShaderId);
        glDetachShader(programId, vertexShaderId);

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
            Logger.WARN.print("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        float widthFactor = widthEnd - widthStart;
        xOffset = (widthFactor / 2) + widthStart;
        xScale = 1 / widthFactor;

        float heightFactor = heightEnd - heightStart;
        yOffset = (heightFactor / 2) + heightStart;
        yScale = 1 / heightFactor;

        uniforms = new ShaderUniforms(programId);
        uniforms.createUniform("texture_sampler");
        uniforms.createUniform("xOffset");
        uniforms.createUniform("xScale");
        uniforms.createUniform("yOffset");
        uniforms.createUniform("yScale");

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

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
        uniforms.setUniform("xOffset", xOffset);
        uniforms.setUniform("xScale", xScale);
        uniforms.setUniform("yOffset", yOffset);
        uniforms.setUniform("yScale", yScale);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClear(GL_COLOR_BUFFER_BIT);
        glViewport(0, 0, targetTextureWidth, targetTextureHeight);
        preparePostProcessing(input.getWidth(), input.getHeight());

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glUseProgram(0);
    }

    protected abstract void preparePostProcessing(int width, int height);

    public void cleanup() {
        glDeleteVertexArrays(quadVao);
        glDeleteBuffers(quadVbo);

        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
}
