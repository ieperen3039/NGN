package io.github.ieperen3039.ngn.Rendering.Shaders;

import io.github.ieperen3039.ngn.AssetHandling.Resource;
import io.github.ieperen3039.ngn.Core.Main;
import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.Lights.DirectionalLight;
import io.github.ieperen3039.ngn.Rendering.Lights.FixedDirectionalLight;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.AbstractSGL;
import io.github.ieperen3039.ngn.Rendering.MeshLoading.Mesh;
import io.github.ieperen3039.ngn.Tools.Vectors;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Geert van Ieperen created on 7-1-2019.
 */
@SuppressWarnings("Duplicates")
public class LightDepthShader implements ShaderProgram {
    private static final Resource.Path SHADOW_SHADER_PATH = SHADER_DIRECTORY.resolve("Depth");
    private static final Resource.Path VERTEX_PATH = SHADOW_SHADER_PATH.resolve("vertex.vert");
    private static final Resource.Path FRAGMENT_PATH = SHADOW_SHADER_PATH.resolve("fragment.frag");
    private static final float LIGHT_Z_NEAR = 0.5f;
    private static final float LIGHT_Z_FAR_MULTIPLIER = 2;
    private static final float LIGHT_CUBE_SIZE = 2;

    private final Map<String, Integer> uniforms;

    private int programId;
    private int vertexShaderID;
    private int fragmentShaderID;

    private DirectionalLight light;

    public LightDepthShader() throws ShaderException, IOException {
        this.uniforms = new HashMap<>();

        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new ShaderException("OpenGL error: Could not create Shader");
        }

        final String vertexCode = VERTEX_PATH.asText();
        vertexShaderID = ShaderProgram.createShader(programId, GL_VERTEX_SHADER, vertexCode);

        final String fragmentCode = FRAGMENT_PATH.asText();
        fragmentShaderID = ShaderProgram.createShader(programId, GL_FRAGMENT_SHADER, fragmentCode);

        link();
        createUniform("lightSpaceMatrix");
        createUniform("modelMatrix");
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
     * Create a new uniform and get its memory location.
     * @param uniformName The name of the uniform.
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    private void createUniform(String uniformName) throws ShaderException {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new ShaderException("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setModelMatrix(Matrix4f modelMatrix) {
        setUniform("modelMatrix", modelMatrix);
    }

    /**
     * Set the value of a 4x4 matrix shader uniform.
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    private void setUniform(String uniformName, Matrix4fc value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    public void setLightSpaceMatrix(Matrix4fc lightSpaceMatrix) {
        setUniform("lightSpaceMatrix", lightSpaceMatrix);
    }

    public void setLight(Vector3fc direction, Color4f color, float intensity) {
        setLight(new FixedDirectionalLight(direction, color, intensity));
    }

    public void setLight(DirectionalLight light) {
        this.light = light;
    }

    /**
     * create a GL object that allows rendering the depth map of a scene
     * @return a GL object that renders a depth map in the frame buffer of the first light that is rendered.
     */
    public DepthGL getGL(Main main) {
        glClear(GL_DEPTH_BUFFER_BIT);
        return new DepthGL();
    }

    /**
     * @author Geert van Ieperen created on 30-1-2019.
     */
    public class DepthGL extends AbstractSGL {
        final Matrix4fc lightSpaceMatrix;
        
        public DepthGL() {
            Vector3f directionToLight = new Vector3f(light.getDirectionToLight());
            lightSpaceMatrix = new Matrix4f()
                    .setOrthoSymmetric(LIGHT_CUBE_SIZE, LIGHT_CUBE_SIZE, LIGHT_Z_NEAR, LIGHT_Z_NEAR * LIGHT_Z_FAR_MULTIPLIER)
                    .lookAlong(directionToLight.negate(), Vectors.Y);
        }

        @Override
        public void render(Mesh object, int index) {
            setLightSpaceMatrix(lightSpaceMatrix);
            setModelMatrix(getModelMatrix());

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

        public void cleanup() {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }
}
