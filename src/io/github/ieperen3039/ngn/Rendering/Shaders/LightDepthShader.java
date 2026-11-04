package io.github.ieperen3039.ngn.Rendering.Shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.IOException;

import org.joml.Matrix4fc;

import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.Rendering.Lights.SingleShadowMapLights;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.AbstractSGL;
import io.github.ieperen3039.ngn.Rendering.MeshLoading.Mesh;
import io.github.ieperen3039.ngn.Rendering.RenderTargets.DepthRenderTarget;

/**
 * @author Geert van Ieperen created on 7-1-2019.
 */
@SuppressWarnings("Duplicates")
public class LightDepthShader implements ShaderProgram {
    private static final Resource.Path SHADOW_SHADER_PATH = SHADER_DIRECTORY.resolve("Depth");
    private static final Resource.Path VERTEX_PATH = SHADOW_SHADER_PATH.resolve("vertex.glsl");
    private static final Resource.Path FRAGMENT_PATH = SHADOW_SHADER_PATH.resolve("fragment.glsl");

    private final ShaderUniforms uniforms;

    private int programId;
    private int vertexShaderID;
    private int fragmentShaderID;

    private SingleShadowMapLights lightManager;

    public LightDepthShader(SingleShadowMapLights lightManager) throws ShaderException, IOException {
        this.lightManager = lightManager;
        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new ShaderException("OpenGL error: Could not create Shader");
        }

        final String vertexCode = VERTEX_PATH.asText();
        vertexShaderID = ShaderProgram.createShader(programId, GL_VERTEX_SHADER, vertexCode);

        final String fragmentCode = FRAGMENT_PATH.asText();
        fragmentShaderID = ShaderProgram.createShader(programId, GL_FRAGMENT_SHADER, fragmentCode);

        link();
        uniforms = new ShaderUniforms(programId);
        uniforms.createUniform("lightSpaceMatrix");
        uniforms.createUniform("modelMatrix");
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
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    public void link() throws ShaderException {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        glDetachShader(programId, vertexShaderID);
        glDetachShader(programId, fragmentShaderID);

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    /**
     * create a GL object that allows rendering the depth map of a scene
     * @return a GL object that renders a depth map in the frame buffer of the first light that is rendered.
     */
    @Override
    public DepthGL getGL(Main main) {
        return new DepthGL();
    }

    /**
     * @author Geert van Ieperen created on 30-1-2019.
     */
    public class DepthGL extends AbstractSGL {
        private DepthRenderTarget renderTarget;
        private Matrix4fc lightSpaceMatrix;

        public DepthGL() {
            this.lightSpaceMatrix = lightManager.loadLightSpaceMatrix();
            this.renderTarget = lightManager.getRenderTarget();

            uniforms.setUniform("lightSpaceMatrix", lightSpaceMatrix);
            
            renderTarget.bind();
            glViewport(0, 0, renderTarget.getWidth(), renderTarget.getHeight());
            glClear(GL_DEPTH_BUFFER_BIT);
        }

        @Override
        public void render(Mesh object, int index) {
            uniforms.setUniform("modelMatrix", getModelMatrix());

            object.render(LOCK);
        }

        @Override
        public ShaderProgram getShader() {
            return LightDepthShader.this;
        }

        @Override
        public Matrix4fc getViewProjectionMatrix() {
            return lightSpaceMatrix;
        }

        @Override
        public void cleanup() {
            renderTarget.unbind();
        }
    }
}
