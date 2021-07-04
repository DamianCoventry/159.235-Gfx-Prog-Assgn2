public class Matrix4 {
    private static final int Size = 4;
    public double[][] _m = new double[Size][Size];
    
    public Matrix4() {
        setIdentity();
    }

    public Matrix4(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
        _m[0][0] = m00; _m[0][1] = m01; _m[0][2] = m02; _m[0][3] = m03;
        _m[1][0] = m10; _m[1][1] = m11; _m[1][2] = m12; _m[1][3] = m13;
        _m[2][0] = m20; _m[2][1] = m21; _m[2][2] = m22; _m[2][3] = m23;
        _m[3][0] = m30; _m[3][1] = m31; _m[3][2] = m32; _m[3][3] = m33;
    }

    public void setIdentity() {
        _m[0][0] = 1.0; _m[0][1] = 0.0; _m[0][2] = 0.0; _m[0][3] = 0.0;
        _m[1][0] = 0.0; _m[1][1] = 1.0; _m[1][2] = 0.0; _m[1][3] = 0.0;
        _m[2][0] = 0.0; _m[2][1] = 0.0; _m[2][2] = 1.0; _m[2][3] = 0.0;
        _m[3][0] = 0.0; _m[3][1] = 0.0; _m[3][2] = 0.0; _m[3][3] = 1.0;
    }

    public Matrix4 multiply(Matrix4 other) {
        Matrix4 result = new Matrix4();
        for (int i = 0; i < Size; ++i) {
            result._m[i][0] = (_m[i][0] * other._m[0][0]) +
                              (_m[i][1] * other._m[1][0]) +
                              (_m[i][2] * other._m[2][0]) +
                              (_m[i][3] * other._m[3][0]);

            result._m[i][1] = (_m[i][0] * other._m[0][1]) +
                              (_m[i][1] * other._m[1][1]) +
                              (_m[i][2] * other._m[2][1]) +
                              (_m[i][3] * other._m[3][1]);

            result._m[i][2] = (_m[i][0] * other._m[0][2]) +
                              (_m[i][1] * other._m[1][2]) +
                              (_m[i][2] * other._m[2][2]) +
                              (_m[i][3] * other._m[3][2]);

            result._m[i][3] = (_m[i][0] * other._m[0][3]) +
                              (_m[i][1] * other._m[1][3]) +
                              (_m[i][2] * other._m[2][3]) +
                              (_m[i][3] * other._m[3][3]);
        }
        return result;
    }

    public Vector3 multiply(Vector3 v) {
        return new Vector3(
                (_m[0][0] * v._x) + (_m[0][1] * v._y) + (_m[0][2] * v._z) + _m[0][3],
                (_m[1][0] * v._x) + (_m[1][1] * v._y) + (_m[1][2] * v._z) + _m[1][3],
                (_m[2][0] * v._x) + (_m[2][1] * v._y) + (_m[2][2] * v._z) + _m[2][3]
        );
    }

    public Vector4 multiply(Vector4 v) {
        return new Vector4(
                (_m[0][0] * v._x) + (_m[0][1] * v._y) + (_m[0][2] * v._z) + (_m[0][3] * v._w),
                (_m[1][0] * v._x) + (_m[1][1] * v._y) + (_m[1][2] * v._z) + (_m[1][3] * v._w),
                (_m[2][0] * v._x) + (_m[2][1] * v._y) + (_m[2][2] * v._z) + (_m[2][3] * v._w),
                (_m[3][0] * v._x) + (_m[3][1] * v._y) + (_m[3][2] * v._z) + (_m[3][3] * v._w)
        );
    }

    public void orthographic(double left, double right, double bottom, double top, double near, double far) {
        setIdentity();
        _m[0][0] =  2 / (right - left);
        _m[1][1] =  2 / (top - bottom);
        _m[2][2] = -2 / (far - near);
        _m[0][3] = -(right + left) / (right - left);
        _m[1][3] = -(top + bottom) / (top - bottom);
        _m[2][3] = -(far + near) / (far - near);
    }

    public void perspective(double verticalFovDegrees, double aspectRatio, double near, double far) {
        double tangent = Math.tan(Math.toRadians(verticalFovDegrees) / 2.0);
        double h = near * tangent;
        double w = h * aspectRatio;
        setFrustum(-w, w, -h, h, near, far);
    }

    private void setFrustum(double left, double right, double bottom, double top, double near, double far) {
        setIdentity();
        _m[0][0] = 2.0 * near / (right - left);
        _m[1][1] = 2.0 * near / (top - bottom);
        _m[0][2] = (right + left) / (right - left);
        _m[1][2] = (top + bottom) / (top - bottom);
        _m[2][2] = -(far + near) / (far - near);
        _m[3][2] = -1.0;
        _m[2][3] = -(2 * far * near) / (far - near);
        _m[3][3] = 0.0;
    }

    public static Matrix4 translate(Vector3 amount) {
        return new Matrix4(
                1.0, 0.0, 0.0, amount._x,
                0.0, 1.0, 0.0, amount._y,
                0.0, 0.0, 1.0, amount._z,
                0.0, 0.0, 0.0, 1.0 );
    }

    public static Matrix4 scale(Vector3 amount) {
        return new Matrix4(
                amount._x, 0.0, 0.0, 0.0,
                0.0, amount._y, 0.0, 0.0,
                0.0, 0.0, amount._z, 0.0,
                0.0, 0.0, 0.0, 1.0 );
    }

    public static Matrix4 rotateX(double angleXDegrees)
    {
        double angleXRadians = Math.toRadians(angleXDegrees);
        double cosX = Math.cos(angleXRadians);
        double sinX = Math.sin(angleXRadians);
        return new Matrix4(
                1.0, 0.0, 0.0, 0.0,
                0.0, cosX, -sinX, 0.0,
                0.0, sinX, cosX, 0.0,
                0.0, 0.0, 0.0, 1.0
        );
    }

    public static Matrix4 rotateY(double angleYDegrees)
    {
        double angleYRadians = Math.toRadians(angleYDegrees);
        double cosY = Math.cos(angleYRadians);
        double sinY = Math.sin(angleYRadians);
        return new Matrix4(
                cosY, 0.0, sinY, 0.0,
                0.0, 1.0, 0.0, 0.0,
                -sinY, 0.0, cosY, 0.0,
                0.0, 0.0, 0.0, 1.0
        );
    }

    public static Matrix4 rotateZ(double angleZDegrees)
    {
        double angleZRadians = Math.toRadians(angleZDegrees);
        double cosZ = Math.cos(angleZRadians);
        double sinZ = Math.sin(angleZRadians);
        return new Matrix4(
                cosZ, -sinZ, 0.0, 0.0,
                sinZ, cosZ, 0.0, 0.0,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
        );
    }
}
