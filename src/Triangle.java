public class Triangle {
    private final int[] _indices = new int[3];
    private Vector3 _normal;
    private final Vector3 _center;

    public Triangle(int a, int b, int c) {
        _indices[0] = a;
        _indices[1] = b;
        _indices[2] = c;
        _normal = new Vector3();
        _center = new Vector3();
    }

    public int[] getIndices() { return _indices; }

    public void calculateNormal(Vector3 a, Vector3 b, Vector3 c) {
        Vector3 edge0 = Vector3.subtract(a, b);
        Vector3 edge1 = Vector3.subtract(a, c);
        edge0.normalise();
        edge1.normalise();
        _normal = edge0.crossProduct(edge1);
    }
    public Vector3 getNormal() { return _normal; }

    public void calculateCenter(Vector3 a, Vector3 b, Vector3 c) {
        _center._x = (a._x + b._x + c._x) / 3.0;
        _center._y = (a._y + b._y + c._y) / 3.0;
        _center._z = (a._z + b._z + c._z) / 3.0;
    }
    public Vector3 getCenter() { return _center; }
}
