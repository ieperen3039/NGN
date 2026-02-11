package io.github.ieperen3039.ngn.Rendering.Shapes;

import io.github.ieperen3039.ngn.Rendering.MeshLoading.Face;
import io.github.ieperen3039.ngn.Rendering.MeshLoading.Mesh;
import io.github.ieperen3039.ngn.Rendering.MeshLoading.SmoothMesh;
import io.github.ieperen3039.ngn.Tools.Vectors;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

public class TexturedCustomShape {
    public record Vertex(Vector3fc position, Vector3fc normal, Vector2fc textureCoord) {
    }

    private Map<Vertex, Integer> vertices;
    private List<Face> faces;

    /**
     * A shape that may be defined by the client code using methods of this class. When the shape is finished, call
     * {@link #toMesh()} to load it into the GPU. The returned shape should be re-used as a static mesh for any
     * future calls to such shape.
     */
    public TexturedCustomShape() {
        this.vertices = new HashMap<>();
        this.faces = new ArrayList<>();
    }

    /**
     * add a quad with points in rotational order. The vectors do not have to be given clockwise
     * @param A      (0, 0)
     * @param B      (0, 1)
     * @param C      (1, 1)
     * @param D      (1, 0)
     * @throws NullPointerException if any of the vectors is null
     */
    public void addQuad(Vertex A, Vertex B, Vertex C, Vertex D) {
        addTriangle(A, C, B);
        addTriangle(A, D, C);
    }

    /**
     * add a triangle. The vectors do not have to be given clockwise
     * @see CustomShape#addQuad(Vector3fc, Vector3fc, Vector3fc, Vector3fc)
     */
    public void addTriangle(Vertex A, Vertex B, Vertex C) {
        Vector3f givenNormal = new Vector3f(A.normal).add(B.normal).add(C.normal);
        Vector3f expectedNormal = Vectors.getNormalVector(A.position, B.position, C.position);
        boolean isClockwise = givenNormal.dot(expectedNormal) > 0;
        
        // must be counterclockwise
        if (isClockwise)
        {
            addFinalTriangle(A, C, B);
        }
        else 
        {
            addFinalTriangle(A, B, C);
        }
    }

    /**
     * adds a triangle with the given points in counterclockwise ordering
     */
    private void addFinalTriangle(Vertex A, Vertex B, Vertex C) {
        int aInd = addVertex(A);
        int bInd = addVertex(B);
        int cInd = addVertex(C);
        faces.add(Face.createWithTexture(new int[]{aInd, bInd, cInd}));
    }

    /**
     * stores a vertex in the collection, and returns its resulting position
     * @return index of the vertex
     */
    private int addVertex(Vertex vertex) {
        int preSize = vertices.size();
        return vertices.computeIfAbsent(vertex, v -> preSize);
    }

    /**
     * adds a strip as separate quad objects
     * @param quads an array of 2n+4 vertices defining quads as {@link #addQuad(Vertex, Vertex, Vertex, Vertex)} for every natural number n.
     */
    public void addStrip(Vertex... quads) {
        final int inputSize = quads.length;
        if (((inputSize % 2) != 0) || (inputSize < 4)) {
            throw new IllegalArgumentException(
                    "input arguments can not be of odd length or less than 4 (length is " + inputSize + ")");
        }

        for (int i = 4; i < inputSize; i += 2) {
            // create quad as [1, 2, 4, 3], as rotational order is required
            addQuad(quads[i - 4], quads[i - 3], quads[i - 1], quads[i - 2]);
        }
    }
    
    /**
     * Adds an arbitrary polygon to the object. For correct rendering, the plane should be flat
     * @param edges  the edges of this plane
     */
    public void addPlane(Vertex... edges) {
        switch (edges.length) {
            case 3:
                addTriangle(edges[0], edges[1], edges[2]);
                return;
            case 4:
            addQuad(edges[0], edges[1], edges[2], edges[3]);
                return;
        }
        for (int i = 1; i < (edges.length - 2); i++) {
            addTriangle(edges[i], edges[i + 1], edges[i + 2]);
        }
    }

    public Collection<Vertex> getVertices() {
        return vertices.keySet();
    }

    /**
     * convert this object into a smoothed Mesh
     * @return a hardware-accelerated Mesh object
     */
    public Mesh toMesh() {
        Vector3fc[] sortedVertices = new Vector3fc[vertices.size()];
        Vector3fc[] sortedNormals = new Vector3fc[vertices.size()];
        Vector2fc[] sortedTexCoords = new Vector2fc[vertices.size()];

        vertices.forEach((v, i) -> {
            sortedVertices[i] = v.position;
            sortedNormals[i] = v.normal;
            sortedTexCoords[i] = v.textureCoord;
        });

        return new SmoothMesh(Arrays.asList(sortedVertices), Arrays.asList(sortedNormals), Arrays.asList(sortedTexCoords), faces);
    }

    public Shape toShape() {
        Vector3fc[] sortedVertices = new Vector3fc[vertices.size()];
        Vector3fc[] sortedNormals = new Vector3fc[vertices.size()];

        vertices.forEach((v, i) -> {
            sortedVertices[i] = v.position;
            sortedNormals[i] = v.normal;
        });

        return new BasicShape(Arrays.asList(sortedVertices), Arrays.asList(sortedNormals), faces);
    }
}
