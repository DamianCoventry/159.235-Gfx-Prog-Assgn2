import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;

public class RenderPanel extends JPanel {
    public static final double DEFAULT_VERTICAL_FOV = 60.0;
    public static final double DEFAULT_ASPECT_RATIO = 1.3;
    public static final double DEFAULT_ORTHO_NCP = -1000.0;
    public static final double DEFAULT_ORTHO_FCP = 1000.0;
    public static final double DEFAULT_PERSP_NCP = 1.0;
    public static final double DEFAULT_PERSP_FCP = 10000.0;
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;

    private Projection _projection;

    private Object _object;                     // <-- model
    private final Camera _camera;               // <-- view
    private final Matrix4 _projectionMatrix;    // <-- projection
    private final Viewport _viewport;
    private final DrawingOptions _drawingOptions;

    private double _verticalFovDegrees;
    private double _aspectRatio;
    private double _orthoNcp;
    private double _orthoFcp;
    private double _perspNcp;
    private double _perspFcp;

    public RenderPanel() {
        setBorder(BorderFactory.createLoweredBevelBorder());

        _projection = Projection.PERSPECTIVE;

        _verticalFovDegrees = DEFAULT_VERTICAL_FOV;
        _aspectRatio = DEFAULT_ASPECT_RATIO;
        _orthoNcp = DEFAULT_ORTHO_NCP;
        _orthoFcp = DEFAULT_ORTHO_FCP;
        _perspNcp = DEFAULT_PERSP_NCP;
        _perspFcp = DEFAULT_PERSP_FCP;

        _object = null;
        _camera = new Camera();
        _projectionMatrix = new Matrix4();
        _viewport = new Viewport(0, 0, getWidth(), getHeight());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                _viewport._w = e.getComponent().getWidth();
                _viewport._h = e.getComponent().getHeight();
                if (_projection == Projection.ORTHOGRAPHIC) {
                    setOrthographicProjection(_orthoNcp, _orthoFcp);
                }
                else {
                    setPerspectiveProjection(_verticalFovDegrees, _perspNcp, _perspFcp);
                }
            }
        });

        _drawingOptions = new DrawingOptions(new Consumer<DrawingOptions>() {
            @Override
            public void accept(DrawingOptions drawingOptions) {
                if (_object != null) {
                    switch (drawingOptions.getFaceCulling()) {
                        case NONE: _object.setNoFaceCulling(); break;
                        case FRONT: _object.setFrontFaceCulling(); break;
                        case BACK: _object.setBackFaceCulling(); break;
                    }

                    switch (drawingOptions.getVisibleSurfaces()) {
                        case NONE: _object.setNoVsd(); break;
                        case PAINTERS: _object.setPaintersVsd(); break;
                    }

                    switch (drawingOptions.getDrawingMode()) {
                        case WIREFRAME: _object.setWireframeTriangles(); break;
                        case SOLID: _object.setSolidTriangles(); break;
                    }
                }
                repaint();
            }
        });
    }

    public void setMesh(Mesh mesh, Consumer<PaintStatistics> onReportPaintStatistics) {
        _object = new Object(mesh, onReportPaintStatistics);
        repaint();
    }

    public void clearMesh() {
        _object = null;
        repaint();
    }

    public void setObjectPosition(Vector3 position) {
        if (_object != null) {
            _object.setPosition(position);
            repaint();
        }
    }
    public void setObjectRotation(Vector3 rotation) {
        if (_object != null) {
            _object.setRotation(rotation);
            repaint();
        }
    }
    public void setObjectScale(Vector3 scale) {
        if (_object != null) {
            _object.setScale(scale);
            repaint();
        }
    }
    public Matrix4 getModelMatrix() {
        if (_object != null) {
            return _object.getModelMatrix();
        }
        return null;
    }

    public void setCameraPosition(Vector3 position) {
        _camera.setPosition(position);
        repaint();
    }
    public void setCameraRotation(Vector3 rotation) {
        _camera.setRotation(rotation);
        repaint();
    }
    public Matrix4 getViewMatrix() { return _camera.getViewMatrix(); }

    public Matrix4 getProjectionMatrix() { return _projectionMatrix; }

    public DrawingOptions getDrawingOptions() { return _drawingOptions; }

    public enum Projection { ORTHOGRAPHIC, PERSPECTIVE }
    public void setOrthographicProjection(double nearClipPlane, double farClipPlane) {
        _projection = Projection.ORTHOGRAPHIC;
        _orthoNcp = nearClipPlane;
        _orthoFcp = farClipPlane;

        int halfW, halfH;
        if (getWidth() == 0 || getHeight() == 0) { // Component has 0 size at creation time.
            halfW = DEFAULT_WIDTH / 2;
            halfH = DEFAULT_HEIGHT / 2;
        }
        else {
            halfW = getWidth() / 2;
            halfH = getHeight() / 2;
        }

        _projectionMatrix.orthographic(-halfW, halfW, -halfH, halfH, _orthoNcp, _orthoFcp);
        repaint();
    }

    public void setPerspectiveProjection(double verticalFovDegrees, double nearClipPlane, double farClipPlane) {
        _projection = Projection.PERSPECTIVE;
        _verticalFovDegrees = verticalFovDegrees;
        _perspNcp = nearClipPlane;
        _perspFcp = farClipPlane;

        if (getWidth() == 0 || getHeight() == 0) { // Component has 0 size at creation time.
            _aspectRatio = DEFAULT_ASPECT_RATIO;
        }
        else {
            _aspectRatio = (double)getWidth() / (double)getHeight();
        }

        _projectionMatrix.perspective(_verticalFovDegrees, _aspectRatio, _perspNcp, _perspFcp);
        repaint();
    }
    public Projection getProjection() { return _projection; }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setBackground(Color.black);
        g2.clearRect(0, 0, getWidth(), getHeight());
        if (_object != null) {
            Matrix4 modelViewMatrix = _camera.getViewMatrix().multiply(_object.getModelMatrix());
            _object.draw(g2, modelViewMatrix, _projectionMatrix, _viewport);
        }
    }
}
