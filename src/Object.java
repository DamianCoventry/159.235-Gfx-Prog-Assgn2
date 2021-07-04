import java.awt.*;
import java.util.*;
import java.util.function.*;

public class Object {
    private static final double DEPTH_NEAR = 0.0;
    private static final double DEPTH_FAR = 1.0;

    private final Mesh _mesh;
    private final Vertex[] _transformedVertices;
    private final SortableTriangle[] _visibleTriangles;
    private final int[] _xInt;
    private final int[] _yInt;
    private final Consumer<PaintStatistics> _onReportPaintStatistics;
    private Function<Triangle, Boolean> _runFaceCulling;
    private Runnable _visibleSurfaceDetermination;
    private Consumer<Graphics2D> _triangleRenderer;
    private Vector3 _position;
    private Vector3 _rotation;
    private Vector3 _scale;
    private final PaintStatistics _paintStatistics;

    public Object(Mesh mesh, Consumer<PaintStatistics> onReportPaintStatistics) {
        _mesh = mesh;
        _onReportPaintStatistics = onReportPaintStatistics;
        _paintStatistics = new PaintStatistics();
        _position = new Vector3();
        _rotation = new Vector3();
        _scale = new Vector3(50.0, 50.0, 50.0);
        _xInt = new int[3];
        _yInt = new int[3];

        _transformedVertices = new Vertex[_mesh.getVertices().length];
        for (int i = 0; i < _mesh.getVertices().length; ++i) {
            _transformedVertices[i] = new Vertex(_mesh.getVertices()[i]);
        }
        
        _visibleTriangles = new SortableTriangle[_mesh.getTriangles().length];
        for (int i = 0; i < _mesh.getTriangles().length; ++i) {
            _visibleTriangles[i] = new SortableTriangle();
        }

        setBackFaceCulling();
        setPaintersVsd();
        setSolidTriangles();
    }

    public void setPosition(Vector3 position) { _position = position; }
    public void setRotation(Vector3 rotation) { _rotation = rotation; }
    public void setScale(Vector3 scale) { _scale = scale; }

    public Matrix4 getModelMatrix() {
        Matrix4 scale = Matrix4.scale(_scale);
        Matrix4 rotZ = Matrix4.rotateZ(_rotation._z);
        Matrix4 rotY = Matrix4.rotateY(_rotation._y);
        Matrix4 rotX = Matrix4.rotateX(_rotation._x);
        Matrix4 translate = Matrix4.translate(_position);

        Matrix4 a = translate.multiply(rotX);
        Matrix4 b = a.multiply(rotY);
        Matrix4 c = b.multiply(rotZ);
        return c.multiply(scale);
    }

    public void draw(Graphics2D g2, Matrix4 modelViewMatrix, Matrix4 projectionMatrix, Viewport viewport) {
        runTransformationPipeline(modelViewMatrix, projectionMatrix, viewport);
        _visibleSurfaceDetermination.run();
        renderTriangles(g2);
        _onReportPaintStatistics.accept(_paintStatistics);
    }

    private void runTransformationPipeline(Matrix4 modelViewMatrix, Matrix4 projectionMatrix, Viewport viewport) {
        int halfW = viewport._w / 2;
        int halfH = viewport._h / 2;
        _paintStatistics._numVerticesClipped = 0;
        for (int i = 0; i < _mesh.getVertices().length; ++i) {
            // Object coords -> world coords -> eye coords
            Vector4 eyeCoords = modelViewMatrix.multiply(new Vector4(_mesh.getVertices()[i], 1.0));

            // Eye coords -> clip coords
            Vector4 clipCoords = projectionMatrix.multiply(eyeCoords);

            // Clip against the range -w -> +w
            if (clipCoords._x > -clipCoords._w && clipCoords._x < clipCoords._w &&
                clipCoords._y > -clipCoords._w && clipCoords._y < clipCoords._w &&
                clipCoords._z > -clipCoords._w && clipCoords._z < clipCoords._w)
            {
                // Divide by w to achieve normalised device coords. All values are -1 -> +1 after this.
                // For perspective projection this is divide by Z
                // For orthographic projection this has no effect because it's divide by 1.0
                Vector4 ndc = clipCoords.divide(clipCoords._w);
                _transformedVertices[i]._x = ndc._x;
                _transformedVertices[i]._y = ndc._y;
                _transformedVertices[i]._z = ndc._z;
                _transformedVertices[i]._clipped = false;

                // Viewport transformation. Scale by viewport (pixel) values.
                _transformedVertices[i]._x = (halfW * _transformedVertices[i]._x) + (viewport._x + halfW);
                _transformedVertices[i]._y = (halfH * -_transformedVertices[i]._y) + (viewport._y + halfH);
                _transformedVertices[i]._z = (((DEPTH_FAR - DEPTH_NEAR) / 2) * _transformedVertices[i]._z) + ((DEPTH_FAR + DEPTH_NEAR) / 2);
            }
            else {
                _transformedVertices[i]._clipped = true;
                ++_paintStatistics._numVerticesClipped;
            }
        }
    }

    public void setNoFaceCulling() {
        _runFaceCulling = (triangle) -> { return false; };
    }

    public void setFrontFaceCulling() {
        _runFaceCulling = (triangle) -> { return recalculateTriangleNormal(triangle) <= 0.0; };
    }

    public void setBackFaceCulling() {
        _runFaceCulling = (triangle) -> { return recalculateTriangleNormal(triangle) > 0.0; };
    }

    private double recalculateTriangleNormal(Triangle t) {
        int i0 = t.getIndices()[0];
        int i1 = t.getIndices()[1];
        int i2 = t.getIndices()[2];
        t.calculateNormal(_transformedVertices[i0], _transformedVertices[i1], _transformedVertices[i2]);
        return t.getNormal()._z;
    }
    
    public void setNoVsd() {
        _visibleSurfaceDetermination = () -> noVisibleSurfaceDetermination();
    }

    public void setPaintersVsd() {
        _visibleSurfaceDetermination = () -> runPaintersAlgorithm();
    }

    public void setWireframeTriangles() {
        _triangleRenderer = (g2) -> drawWireframeTriangle(g2);
    }

    public void setSolidTriangles() {
        _triangleRenderer = (g2) -> drawSolidTriangle(g2);
    }

    private void noVisibleSurfaceDetermination() {
        _paintStatistics._numTrianglesCulled = 0;
        _paintStatistics._numTrianglesVisible = 0;
        for (int i = 0; i < _mesh.getTriangles().length; ++i) {
            if (_runFaceCulling.apply(_mesh.getTriangles()[i])) {
                ++_paintStatistics._numTrianglesCulled;
            }
            else {
                _visibleTriangles[_paintStatistics._numTrianglesVisible]._index = i;
                ++_paintStatistics._numTrianglesVisible;
            }
        }
    }

    private void runPaintersAlgorithm() {
        _paintStatistics._numTrianglesCulled = 0;
        _paintStatistics._numTrianglesVisible = 0;
        for (int i = 0; i < _mesh.getTriangles().length; ++i) {
            Triangle t = _mesh.getTriangles()[i];
            if (_runFaceCulling.apply(t)) {
                ++_paintStatistics._numTrianglesCulled;
            }
            else {
                int i0 = t.getIndices()[0];
                int i1 = t.getIndices()[1];
                int i2 = t.getIndices()[2];
                t.calculateCenter(_transformedVertices[i0], _transformedVertices[i1], _transformedVertices[i2]);
                _visibleTriangles[_paintStatistics._numTrianglesVisible]._index = i;
                _visibleTriangles[_paintStatistics._numTrianglesVisible]._z = t.getCenter()._z;
                ++_paintStatistics._numTrianglesVisible;
            }
        }

        Arrays.sort(_visibleTriangles, 0, _paintStatistics._numTrianglesVisible);
    }

    private void renderTriangles(Graphics2D g2) {
        for (int i = 0; i < _paintStatistics._numTrianglesVisible; ++i) {
            Triangle t = _mesh.getTriangles()[_visibleTriangles[i]._index];
            int i0 = t.getIndices()[0];
            int i1 = t.getIndices()[1];
            int i2 = t.getIndices()[2];
            if (_transformedVertices[i0]._clipped || _transformedVertices[i1]._clipped || _transformedVertices[i2]._clipped) {
                continue;
            }
            _xInt[0] = (int)_transformedVertices[i0]._x;
            _yInt[0] = (int)_transformedVertices[i0]._y;
            _xInt[1] = (int)_transformedVertices[i1]._x;
            _yInt[1] = (int)_transformedVertices[i1]._y;
            _xInt[2] = (int)_transformedVertices[i2]._x;
            _yInt[2] = (int)_transformedVertices[i2]._y;
            _triangleRenderer.accept(g2);
        }
    }

    private void drawWireframeTriangle(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.drawPolygon(_xInt, _yInt, 3);
    }

    private void drawSolidTriangle(Graphics2D g2) {
        g2.setColor(Color.blue);
        g2.fillPolygon(_xInt, _yInt, 3);
        g2.setColor(Color.white);
        g2.drawPolygon(_xInt, _yInt, 3);
    }
}
