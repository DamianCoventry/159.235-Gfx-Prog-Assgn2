public class Mesh {
    private final Triangle[] _triangles;
    private final Vector3[] _vertices;

    public Mesh(Triangle[] triangles, Vector3[] vertices) {
        _triangles = triangles;
        _vertices = vertices;
    }

    public Triangle[] getTriangles() { return _triangles; }
    public Vector3[] getVertices() { return _vertices; }
}
