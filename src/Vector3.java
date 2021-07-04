public class Vector3 {
    public double _x, _y, _z;

    public Vector3() {
        _x = _y = _z = 0.0;
    }

    public Vector3(Vector3 v) {
        _x = v._x; _y = v._y; _z = v._z;
    }

    public Vector3(double x, double y, double z) {
        _x = x; _y = y; _z = z;
    }

    public Vector3 negate() {
        return new Vector3(-_x, -_y, -_z);
    }

    public Vector3 divide(double value) {
        return new Vector3(_x/value, _y/value, _z/value);
    }

    public double dotProduct() {
        return _x * _x + _y * _y + _z * _z;
    }

    public Vector3 crossProduct(Vector3 v) {
        return new Vector3(
                _y * v._z - _z * v._y,
                _z * v._x - _x * v._z,
                _x * v._y - _y * v._x);
    }

    public double magnitude() {
        return Math.sqrt(dotProduct());
    }

    public void normalise() {
        double length = magnitude();
        if (length != 0.0) {
            _x /= length;
            _y /= length;
            _z /= length;
        }
    }

    public static Vector3 subtract(Vector3 a, Vector3 b) {
        return new Vector3(a._x - b._x, a._y - b._y, a._z - b._z);
    }
}
