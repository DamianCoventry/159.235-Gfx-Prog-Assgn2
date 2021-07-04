import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.function.Consumer;

public class ItemPanel extends JPanel {
    private static final double DEFAULT_SCALE = 50.0;
    private static final double DEFAULT_CAMERA_Z = 500.0;
    private static final int PANEL_WIDTH = 375;
    private static final int PANEL_HEIGHT = 100;
    private static final int LABEL_WIDTH = 40;
    private static final int SLIDER_WIDTH = 250;
    private static final int CONTROL_HEIGHT = 30;
    private static final int TRANSLATION_VALUE = 750;

    private final DecimalFormat _decimalFormat;
    private final Vector3 _position;
    private final Vector3 _rotation;
    private final Vector3 _scale;
    private final Consumer<Vector3> _onPositionChanged;
    private final Consumer<Vector3> _onRotationChanged;
    private final Consumer<Vector3> _onScaleChanged;
    
    private JSlider _positionXSlider;
    private JLabel _positionXLabel;
    private JSlider _positionYSlider;
    private JLabel _positionYLabel;
    private JSlider _positionZSlider;
    private JLabel _positionZLabel;
    private JSlider _rotationXSlider;
    private JLabel _rotationXLabel;
    private JSlider _rotationYSlider;
    private JLabel _rotationYLabel;
    private JSlider _rotationZSlider;
    private JLabel _rotationZLabel;
    private JSlider _scaleSlider;
    private JLabel _scaleLabel;
    private boolean _internalSet;
    private double _defaultZPosition;

    public ItemPanel(String title, Consumer<Vector3> onPositionChanged, Consumer<Vector3> onRotationChanged) {
        _decimalFormat = new DecimalFormat("0.00");
        _internalSet = false;
        _position = new Vector3();
        _rotation = new Vector3();
        _scale = null;
        _onPositionChanged = onPositionChanged;
        _onRotationChanged = onRotationChanged;
        _onScaleChanged = null;
        setBorder(BorderFactory.createTitledBorder(title));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createPositionPanel(DEFAULT_CAMERA_Z);
        createRotationPanel();
        _scaleSlider = null;
        _scaleLabel = null;
        createResetButton();
    }

    public ItemPanel(String title, Consumer<Vector3> onPositionChanged, Consumer<Vector3> onRotationChanged, Consumer<Vector3> onScaleChanged) {
        _decimalFormat = new DecimalFormat("0.00");
        _internalSet = false;
        _position = new Vector3();
        _rotation = new Vector3();
        _scale = new Vector3(DEFAULT_SCALE, DEFAULT_SCALE, DEFAULT_SCALE);
        _onPositionChanged = onPositionChanged;
        _onRotationChanged = onRotationChanged;
        _onScaleChanged = onScaleChanged;
        setBorder(BorderFactory.createTitledBorder(title));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createPositionPanel(0.0);
        createRotationPanel();
        createScalePanel();
        createResetButton();
    }

    public Vector3 getPosition() { return _position; }
    public Vector3 getRotation() { return _rotation; }
    public Vector3 getScale() { return _scale; }

    public void reset() {
        _internalSet = true;
        _position._x = _position._y = 0.0; _position._z = _defaultZPosition;
        _rotation._x = _rotation._y = _rotation._z = 0.0;
        _positionXSlider.setValue(0);
        _positionYSlider.setValue(0);
        _positionZSlider.setValue((int)_position._z);
        _rotationXSlider.setValue(0);
        _rotationYSlider.setValue(0);
        _rotationZSlider.setValue(0);
        _positionXLabel.setText("0.00");
        _positionYLabel.setText("0.00");
        _positionZLabel.setText(_decimalFormat.format(_position._z));
        _rotationXLabel.setText("0.00");
        _rotationYLabel.setText("0.00");
        _rotationZLabel.setText("0.00");
        if (_scale != null) {
            _scale._x = _scale._y = _scale._z = DEFAULT_SCALE;
            _scaleSlider.setValue((int)DEFAULT_SCALE);
            _scaleLabel.setText(_decimalFormat.format(DEFAULT_SCALE));
        }
        _internalSet = false;
        _onPositionChanged.accept(_position);
        _onRotationChanged.accept(_rotation);
        if (_onScaleChanged != null) {
            _onScaleChanged.accept(_scale);
        }
    }

    private void createPositionPanel(double defaultZPosition) {
        _defaultZPosition = defaultZPosition;
        _positionXSlider = new JSlider(-TRANSLATION_VALUE, TRANSLATION_VALUE, 0);
        _positionXLabel = new JLabel("0.00");
        bind(_positionXSlider, _positionXLabel, (value) -> {
            _position._x = value;
            _onPositionChanged.accept(_position);
        });
        _positionYSlider = new JSlider(-TRANSLATION_VALUE, TRANSLATION_VALUE, 0);
        _positionYLabel = new JLabel("0.00");
        bind(_positionYSlider, _positionYLabel, (value) ->{
            _position._y = value;
            _onPositionChanged.accept(_position);
        });
        _positionZSlider = new JSlider(-TRANSLATION_VALUE, TRANSLATION_VALUE, (int)defaultZPosition);
        _positionZLabel = new JLabel(_decimalFormat.format(defaultZPosition));
        bind(_positionZSlider, _positionZLabel, (value) ->{
            _position._z = value;
            _onPositionChanged.accept(_position);
        });
        add(createAxesPanel("Pos", _positionXSlider, _positionXLabel, _positionYSlider, _positionYLabel, _positionZSlider, _positionZLabel));
    }

    private void createRotationPanel() {
        _rotationXSlider = new JSlider(-180, 180, 0);
        _rotationXLabel = new JLabel("0.00");
        bind(_rotationXSlider, _rotationXLabel, (value) -> {
            _rotation._x = value;
            _onRotationChanged.accept(_rotation);
        });
        _rotationYSlider = new JSlider(-180, 180, 0);
        _rotationYLabel = new JLabel("0.00");
        bind(_rotationYSlider, _rotationYLabel, (value) -> {
            _rotation._y = value;
            _onRotationChanged.accept(_rotation);
        });
        _rotationZSlider = new JSlider(-180, 180, 0);
        _rotationZLabel = new JLabel("0.00");
        bind(_rotationZSlider, _rotationZLabel, (value) -> {
            _rotation._z = value;
            _onRotationChanged.accept(_rotation);
        });
        add(createAxesPanel("Rot", _rotationXSlider, _rotationXLabel, _rotationYSlider, _rotationYLabel, _rotationZSlider, _rotationZLabel));
    }

    private void createScalePanel() {
        _scaleSlider = new JSlider(1, 500, (int)DEFAULT_SCALE);
        _scaleLabel = new JLabel(_decimalFormat.format(DEFAULT_SCALE));
        bind(_scaleSlider, _scaleLabel, (value) -> {
            _scale._x = _scale._y = _scale._z = value; // Won't bother with individual axis scale values
            _onScaleChanged.accept(_scale);
        });
        add(createAxisPanel("Scale", _scaleSlider, _scaleLabel));
    }

    private void createResetButton() {
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        add(resetButton);
    }

    private JPanel createAxesPanel(String suffix, JSlider sliderX, JLabel labelX, JSlider sliderY, JLabel labelY, JSlider sliderZ, JLabel labelZ) {
        JPanel panel = new JPanel();
        panel.setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        panel.setMaximumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(createSliderPanel("X" + suffix, sliderX, labelX));
        panel.add(createSliderPanel("Y" + suffix, sliderY, labelY));
        panel.add(createSliderPanel("Z" + suffix, sliderZ, labelZ));
        return panel;
    }

    private JPanel createAxisPanel(String title, JSlider slider, JLabel label) {
        JPanel panel = new JPanel();
        panel.setMinimumSize(new Dimension(PANEL_WIDTH, CONTROL_HEIGHT));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(createSliderPanel(title, slider, label));
        return panel;
    }

    private JPanel createSliderPanel(String title, JSlider slider, JLabel label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(new JLabel(title));
        slider.setMinimumSize(new Dimension(SLIDER_WIDTH, CONTROL_HEIGHT));
        slider.setMaximumSize(new Dimension(SLIDER_WIDTH, CONTROL_HEIGHT));
        panel.add(slider);
        label.setMinimumSize(new Dimension(LABEL_WIDTH, CONTROL_HEIGHT));
        label.setMaximumSize(new Dimension(LABEL_WIDTH, CONTROL_HEIGHT));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(label);
        return panel;
    }

    private void bind(JSlider slider, JLabel label, Consumer<Integer> onChangedCallback) {
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!_internalSet) {
                    JSlider source = (JSlider)e.getSource();
                    label.setText(_decimalFormat.format(source.getValue()));
                    onChangedCallback.accept(source.getValue());
                }
            }
        });
    }
}
