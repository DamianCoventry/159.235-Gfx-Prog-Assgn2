public class Camera {
    public static final double DEFAULT_Z_COORD = 500.0;

    private Vector3 _position;
    private Vector3 _rotation;
    
    public Camera() {
        _position = new Vector3(0.0, 0.0, DEFAULT_Z_COORD);
        _rotation = new Vector3();
    }

    public void setPosition(Vector3 position) { _position = position; }
    public void setRotation(Vector3 rotation) { _rotation = rotation; }

    public Matrix4 getViewMatrix() {
        Matrix4 translate = Matrix4.translate(_position.negate());
        Matrix4 rotX = Matrix4.rotateX(-_rotation._x);
        Matrix4 rotY = Matrix4.rotateY(-_rotation._y);
        Matrix4 rotZ = Matrix4.rotateZ(-_rotation._z);

        Matrix4 zy = rotZ.multiply(rotY);
        Matrix4 zyx = zy.multiply(rotX);
        return zyx.multiply(translate);
    }
}
