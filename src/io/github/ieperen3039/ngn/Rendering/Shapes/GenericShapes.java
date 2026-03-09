package io.github.ieperen3039.ngn.Rendering.Shapes;

import io.github.ieperen3039.ngn.Rendering.MatrixStack.SGL;
import io.github.ieperen3039.ngn.Rendering.MeshLoading.Mesh;
import io.github.ieperen3039.ngn.Rendering.MeshLoading.MeshFile;
import io.github.ieperen3039.ngn.Rendering.Shapes.Primitives.Plane;
import io.github.ieperen3039.ngn.AssetHandling.GeneratorAsset;
import io.github.ieperen3039.ngn.AssetHandling.Asset;
import io.github.ieperen3039.ngn.AssetHandling.Resource;
import org.joml.AABBf;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collection;

/**
 * A collection of generic shapes
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public enum GenericShapes implements Mesh, Shape {
    ARROW("arrow.obj"),
    ICOSAHEDRON("icosahedron.obj"),
    CUBE("cube.obj"),
    TEXTURED_QUAD("quad.obj"),

    /** a quad of size 2x2 on the xy plane */
    QUAD(createQuad()),
    ;

    private final Asset<Mesh> mesh;
    private final Shape shape;

    GenericShapes(String... relative) {
        Asset<MeshFile> pars = MeshFile.createAsset(Resource.Path.get("ngn/models").resolve(relative));
        shape = pars.get().getShape();
        mesh = new GeneratorAsset<>(() -> pars.get().getMesh(), Mesh::dispose);
        pars.drop();
    }

    GenericShapes(CustomShape frame) {
        shape = frame.toShape();
        mesh = new GeneratorAsset<>(frame::toFlatMesh, Mesh::dispose);
    }

    public Asset<Mesh> meshResource() {
        return mesh;
    }

    public Asset<Shape> shapeResource() {
        return new GeneratorAsset<>(() -> shape, null);
    }

    @Override
    public void render(SGL.Painter lock) {
        mesh.get().render(lock);
    }

    @Override
    public void dispose() {
        mesh.drop();
    }

    @Override
    public Collection<? extends Plane> getPlanes() {
        return shape.getPlanes();
    }

    @Override
    public Collection<Vector3fc> getPoints() {
        return shape.getPoints();
    }

    @Override
    public AABBf getBoundingBox() {
        return shape.getBoundingBox();
    }

    private static CustomShape createQuad() {
        CustomShape frame = new CustomShape();
        frame.addQuad(
                new Vector3f(1, 1, 0),
                new Vector3f(-1, 1, 0),
                new Vector3f(-1, -1, 0),
                new Vector3f(1, -1, 0),
                new Vector3f(0, 0, 1)
        );
        return frame;
    }
}
