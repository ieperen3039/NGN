package io.github.ieperen3039.ngn.Rendering.Shaders;

import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.AbstractSGL;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.MeshLoading.Mesh;
import io.github.ieperen3039.ngn.Rendering.RenderTargets.RenderTarget;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.TickTime;
import io.github.ieperen3039.ngn.Tools.Toolbox;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3i;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;

public class ClickShader implements ShaderProgram {
    private static final Resource.Path CLICK_SHADER_PATH = SHADER_DIRECTORY.resolve("Click");
    private static final Resource.Path VERTEX_PATH = CLICK_SHADER_PATH.resolve("vertex.glsl");
    private static final Resource.Path FRAGMENT_PATH = CLICK_SHADER_PATH.resolve("fragment.glsl");
    protected final ShaderUniforms uniforms;

    private int programId;
    private int vertexShaderID;
    private int fragmentShaderID;
    private RenderTarget renderTarget;

    /**
     * @throws ShaderException if a new shader could not be created by some opengl reason
     * @throws IOException     if the defined files could not be found (the file is searched for in the shader folder
     *                         itself, and should exclude any first slash)
     */
    public ClickShader(RenderTarget renderTarget) throws ShaderException, IOException {
        this.renderTarget = renderTarget;
        this.programId = glCreateProgram();
        if (programId == 0) {
            throw new ShaderException("OpenGL error: Could not create Shader");
        }

        if (VERTEX_PATH != null) {
            final String shaderCode = VERTEX_PATH.asText();
            vertexShaderID = ShaderProgram.createShader(programId, GL_VERTEX_SHADER, shaderCode);
        }

        if (FRAGMENT_PATH != null) {
            final String shaderCode = FRAGMENT_PATH.asText();
            fragmentShaderID = ShaderProgram.createShader(programId, GL_FRAGMENT_SHADER, shaderCode);
        }

        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderID != 0) {
            glDetachShader(programId, vertexShaderID);
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
        uniforms.createUniform("color");

        Toolbox.checkGLError(toString());
    }

    @Override
    public SGL getGL(Main main, TickTime time) {
        Matrix4f viewProjectionMatrix = main.camera().getViewProjection(main.getViewPort());
        uniforms.setUniform("viewProjectionMatrix", viewProjectionMatrix);
        return new ClickShaderGL(viewProjectionMatrix);
    }

    @Override
    public void bind() {
        glUseProgram(programId);
        renderTarget.bind();
    }

    @Override
    public void unbind() {
        glUseProgram(0);
        renderTarget.unbind();
    }

    @Override
    public void cleanup() {
        if (programId != 0) {
            unbind();
            glDeleteProgram(programId);
            programId = 0;
        }
    }

    private class ClickShaderGL extends AbstractSGL {
        Matrix4fc viewProjectionMatrix;

        public ClickShaderGL(Matrix4fc viewProjectionMatrix) {
            this.viewProjectionMatrix = viewProjectionMatrix;
        }

        @Override
        public void render(Mesh object, int index) {
            uniforms.setUniform("modelMatrix", getModelMatrix());
            Vector3i color = ClickShaderTarget.numberToColor(index);
            uniforms.setUniform("color", color);
            object.render(LOCK);
        }

        @Override
        public ShaderProgram getShader() {
            return ClickShader.this;
        }

        @Override
        public Matrix4fc getViewProjectionMatrix() {
            return viewProjectionMatrix;
        }
    }
}
