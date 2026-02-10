package NG.Scene;

import NG.Rendering.MatrixStack.SGL;

import java.io.Serializable;

/**
 * An entity is anything that is both visible in the world, and allows interaction with other entities (including the
 * map). Particles and other visual things are not entities.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface Entity extends Serializable {
    /**
     * Updates the state of the entity. The frequency this method is called depend on the return value of {@link
     * #getUpdateFrequency()}. Use {@link GameTimer#getGameTimeDifference()} for speed calculations and {@link
     * GameTimer#getGameTime()} for position calculations
     */
    void update();

    /**
     * Draws this entity using the provided SGL object. This method may only be called from the rendering loop, and
     * should not change the internal representation of this object. Possible animations should be based on {@link
     * GameTimer#getRenderTime()}. Material must be set using {@link SGL#getShader()}.
     * @param gl the graphics object to be used for rendering. It is initialized at world's origin. (no translation or
     *           scaling has been applied)
     */
    void draw(SGL gl);
}
