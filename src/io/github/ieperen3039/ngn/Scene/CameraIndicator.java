package io.github.ieperen3039.ngn.Scene;

import static org.lwjgl.opengl.GL11.glDepthMask;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import io.github.ieperen3039.ngn.DataStructures.Generic.Color4f;
import io.github.ieperen3039.ngn.Rendering.Material;
import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.Shaders.MaterialShader;
import io.github.ieperen3039.ngn.Rendering.Shaders.ShaderProgram;
import io.github.ieperen3039.ngn.Rendering.Shapes.GenericShapes;

public class CameraIndicator implements Entity {
    // in meters
    private static final float CAMERA_SIZE = 0.02f;
    private static final float POINTER_SIZE = CAMERA_SIZE / 10f;
    private static final float POINTER_LENGTH = CAMERA_SIZE * 10f;
    private static final float UP_POINTER_LENGTH = CAMERA_SIZE * 2f;

    private Matrix4fc viewProjectionMatrix;
    private Matrix4fc viewProjectionMatrixInv;
    private Vector3fc postion;
    private Quaternionfc rotation;

    private boolean showFrustum = false;

    public CameraIndicator(
        Matrix4fc viewProjectionMatrix, Vector3fc postion, Quaternionfc rotation
    ) {
        this.viewProjectionMatrix = viewProjectionMatrix;
        this.viewProjectionMatrixInv = new Matrix4f(viewProjectionMatrix).invert();
        this.postion = postion;
        this.rotation = rotation;
    }

    public void setShowFrustum(boolean setVisible) {
        showFrustum = setVisible;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(SGL gl) {
        Material mat = Material.ROUGH;
        ShaderProgram shader = gl.getShader();
        MaterialShader matShader = (diffuse, specular, reflectance) -> {};

        if (shader instanceof MaterialShader) {
            matShader = (MaterialShader) shader;
        }

        // draw the camera itself
        gl.pushMatrix();
        {
            gl.translate(postion);
            gl.rotate(rotation);

            gl.pushMatrix();
            {
                gl.scale(CAMERA_SIZE);
                matShader.setMaterial(mat, Color4f.GREY);
                gl.render(GenericShapes.CUBE, 0);
            }
            gl.popMatrix();

            gl.pushMatrix();
            {
                gl.scale(POINTER_SIZE, POINTER_SIZE, POINTER_LENGTH / 2);
                gl.translate(0, 0, -1);
                matShader.setMaterial(mat, Color4f.RED);
                gl.render(GenericShapes.CUBE, 0);
            }
            gl.popMatrix();

            gl.pushMatrix();
            {
                gl.scale(POINTER_SIZE, UP_POINTER_LENGTH / 2, POINTER_SIZE);
                gl.translate(0, 1, 0);
                matShader.setMaterial(mat, Color4f.GREEN);
                gl.render(GenericShapes.CUBE, 0);
            }
            gl.popMatrix();
        }
        gl.popMatrix();

        // now visualize the perspective
        if (showFrustum) 
        {
            glDepthMask(false);
            gl.pushMatrix();
            {
                gl.multiply(viewProjectionMatrixInv);
    
                // faint blue
                matShader.setMaterial(mat, new Color4f(0, 0, 1, 0.05f));
                gl.render(GenericShapes.CUBE, 0);
            }
            gl.popMatrix();
            glDepthMask(true);
        }
    }

    public Matrix4fc getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }
}
