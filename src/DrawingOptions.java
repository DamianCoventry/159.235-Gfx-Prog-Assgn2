import java.util.function.Consumer;

public class DrawingOptions {
    private FaceCulling _faceCulling;
    private DrawingMode _drawingMode;
    private VisibleSurfaces _visibleSurfaces;
    private final Consumer<DrawingOptions> _changeNotifier;

    public DrawingOptions(Consumer<DrawingOptions> changeNotifier) {
        _faceCulling = FaceCulling.BACK;
        _drawingMode = DrawingMode.SOLID;
        _visibleSurfaces = VisibleSurfaces.PAINTERS;
        _changeNotifier = changeNotifier;
    }

    public enum FaceCulling { NONE, FRONT, BACK }
    public void setFaceCulling(FaceCulling faceCulling) {
        _faceCulling = faceCulling;
        _changeNotifier.accept(this);
    }
    public FaceCulling getFaceCulling() { return _faceCulling; }

    public enum DrawingMode { WIREFRAME, SOLID }
    public void setDrawingMode(DrawingMode drawingMode) {
        _drawingMode = drawingMode;
        _changeNotifier.accept(this);
    }
    public DrawingMode getDrawingMode() { return _drawingMode; }

    public enum VisibleSurfaces { NONE, PAINTERS }
    public void setVisibleSurfaces(VisibleSurfaces visibleSurfaces) {
        _visibleSurfaces = visibleSurfaces;
        _changeNotifier.accept(this);
    }
    public VisibleSurfaces getVisibleSurfaces() { return _visibleSurfaces; }
}
