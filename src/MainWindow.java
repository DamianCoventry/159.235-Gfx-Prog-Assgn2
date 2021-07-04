import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

// TODO: the sliders don't do anything before a file is opened

public class MainWindow extends JFrame {
    public static final int RADIO_BUTTON_WIDTH = 150;
    public static final int TEXT_FIELD_WIDTH = 40;
    public static final int CONTROL_HEIGHT = 30;

    private ItemPanel _cameraPanel;
    private ItemPanel _modelPanel;
    private Matrix4Panel _modelMatrixPanel;
    private Matrix4Panel _viewMatrixPanel;
    private Matrix4Panel _projectionMatrixPanel;
    private JRadioButton _noneFaceCulling;
    private JRadioButton _frontFaceCulling;
    private JRadioButton _backFaceCulling;
    private JRadioButton _wireframeDrawingMode;
    private JRadioButton _solidDrawingMode;
    private JRadioButton _noneVisibleSurfaces;
    private JRadioButton _paintersVisibleSurfaces;
    private JRadioButton _orthographicProjection;
    private JRadioButton _perspectiveProjection;
    private JTextField _numTrianglesCulled;
    private JTextField _numTrianglesVisible;
    private JTextField _numVerticesClipped;
    private RenderPanel _renderPanel;
    private boolean _internalSet;

    public MainWindow() {
        setTitle("159.235 Assignment 2");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setupMenu();
        setupSouthPanel();
        setupEastPanel();
        setupCenterPanel();
    }

    private void setupMenu() {
        JMenuItem openMenuItem = new JMenuItem("Open...", KeyEvent.VK_O);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onFileOpen();
            }});

        JMenuItem closeMenuItem = new JMenuItem("Close", KeyEvent.VK_C);
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK));
        closeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onFileClose();
            }});

        JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }});

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(openMenuItem);
        fileMenu.add(closeMenuItem);
        fileMenu.add(exitMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private void onFileOpen() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Data Files", "dat");
        chooser.setFileFilter(filter);
        File workingDirectory = new File(System.getProperty("user.dir"));
        chooser.setCurrentDirectory(workingDirectory);
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                DatFile datFile = new DatFile(file);

                _modelPanel.reset();
                _cameraPanel.reset();

                _renderPanel.setMesh(
                        new Mesh(datFile.getTriangles(), datFile.getVertices()),
                        (paintStatistics) -> {
                            _numTrianglesCulled.setText(String.valueOf(paintStatistics._numTrianglesCulled));
                            _numTrianglesVisible.setText(String.valueOf(paintStatistics._numTrianglesVisible));
                            _numVerticesClipped.setText(String.valueOf(paintStatistics._numVerticesClipped));
                        });

                rebuildModelMatrix();
                rebuildViewMatrix();
                _projectionMatrixPanel.setMatrix(_renderPanel.getProjectionMatrix());
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Unable to read the file.\n\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onFileClose() {
        _renderPanel.clearMesh();
    }

    private void setupSouthPanel() {
        _modelPanel = new ItemPanel("Model",
                new Consumer<Vector3>() {
                    @Override
                    public void accept(Vector3 vector3) { // Position
                        rebuildModelMatrix();
                    }
                }, new Consumer<Vector3>() {
                    @Override
                    public void accept(Vector3 vector3) { // Rotation
                        rebuildModelMatrix();
                    }
                }, new Consumer<Vector3>() {
                    @Override
                    public void accept(Vector3 vector3) { // Scale
                        rebuildModelMatrix();
                    }
                });

        _cameraPanel = new ItemPanel("Camera",
                new Consumer<Vector3>() {
                    @Override
                    public void accept(Vector3 vector3) { // Position
                        rebuildViewMatrix();
                    }
                }, new Consumer<Vector3>() {
                    @Override
                    public void accept(Vector3 vector3) { // Rotation
                        rebuildViewMatrix();
                    }
                });

        _modelMatrixPanel = new Matrix4Panel("Model");
        _viewMatrixPanel = new Matrix4Panel("View");
        _projectionMatrixPanel = new Matrix4Panel("Projection");

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
        southPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        southPanel.add(_modelPanel);
        southPanel.add(_cameraPanel);
        southPanel.add(_modelMatrixPanel);
        southPanel.add(_viewMatrixPanel);
        southPanel.add(_projectionMatrixPanel);
        getContentPane().add(BorderLayout.SOUTH, southPanel);
    }

    private void setupEastPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(setupFaceCullingPanel());
        panel.add(setupDrawingModePanel());
        panel.add(setupVisibleSurfacesPanel());
        panel.add(setupProjectionPanel());
        panel.add(setupStatisticsPanel());
        getContentPane().add(BorderLayout.EAST, panel);
    }

    private JPanel setupFaceCullingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Face Culling"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        _noneFaceCulling = createRadioButton("None");
        _noneFaceCulling.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.getDrawingOptions().setFaceCulling(DrawingOptions.FaceCulling.NONE);
                }
            }
        });

        _frontFaceCulling = createRadioButton("Front");
        _frontFaceCulling.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.getDrawingOptions().setFaceCulling(DrawingOptions.FaceCulling.FRONT);
                }
            }
        });

        _backFaceCulling = createRadioButton("Back");
        _backFaceCulling.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.getDrawingOptions().setFaceCulling(DrawingOptions.FaceCulling.BACK);
                }
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(_noneFaceCulling);
        group.add(_frontFaceCulling);
        group.add(_backFaceCulling);
        panel.add(_noneFaceCulling);
        panel.add(_frontFaceCulling);
        panel.add(_backFaceCulling);
        return panel;
    }

    private JPanel setupDrawingModePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Drawing Mode"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        _wireframeDrawingMode = createRadioButton("Wireframe");
        _wireframeDrawingMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.getDrawingOptions().setDrawingMode(DrawingOptions.DrawingMode.WIREFRAME);
                }
            }
        });

        _solidDrawingMode = createRadioButton("Solid");
        _solidDrawingMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.getDrawingOptions().setDrawingMode(DrawingOptions.DrawingMode.SOLID);
                }
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(_wireframeDrawingMode);
        group.add(_solidDrawingMode);
        panel.add(_wireframeDrawingMode);
        panel.add(_solidDrawingMode);
        return panel;
    }

    private JPanel setupVisibleSurfacesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Visible Surfaces"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        _noneVisibleSurfaces = createRadioButton("None");
        _noneVisibleSurfaces.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.getDrawingOptions().setVisibleSurfaces(DrawingOptions.VisibleSurfaces.NONE);
                }
            }
        });

        _paintersVisibleSurfaces = createRadioButton("Painter's");
        _paintersVisibleSurfaces.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.getDrawingOptions().setVisibleSurfaces(DrawingOptions.VisibleSurfaces.PAINTERS);
                }
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(_noneVisibleSurfaces);
        group.add(_paintersVisibleSurfaces);
        panel.add(_noneVisibleSurfaces);
        panel.add(_paintersVisibleSurfaces);
        return panel;
    }

    private JPanel setupProjectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Projection"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        _orthographicProjection = createRadioButton("Orthographic");
        _orthographicProjection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.setOrthographicProjection(RenderPanel.DEFAULT_ORTHO_NCP, RenderPanel.DEFAULT_ORTHO_FCP);
                    _projectionMatrixPanel.setMatrix(_renderPanel.getProjectionMatrix());
                }
            }
        });

        _perspectiveProjection = createRadioButton("Perspective");
        _perspectiveProjection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!_internalSet && e.getStateChange() == ItemEvent.SELECTED) {
                    _renderPanel.setPerspectiveProjection(RenderPanel.DEFAULT_VERTICAL_FOV, RenderPanel.DEFAULT_PERSP_NCP, RenderPanel.DEFAULT_PERSP_FCP);
                    _projectionMatrixPanel.setMatrix(_renderPanel.getProjectionMatrix());
                }
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(_orthographicProjection);
        group.add(_perspectiveProjection);
        panel.add(_orthographicProjection);
        panel.add(_perspectiveProjection);
        return panel;
    }

    private JRadioButton createRadioButton(String title) {
        JRadioButton radioButton = new JRadioButton(title);
        radioButton.setMinimumSize(new Dimension(RADIO_BUTTON_WIDTH, CONTROL_HEIGHT));
        radioButton.setMaximumSize(new Dimension(RADIO_BUTTON_WIDTH, CONTROL_HEIGHT));
        radioButton.setPreferredSize(new Dimension(RADIO_BUTTON_WIDTH, CONTROL_HEIGHT));
        return radioButton;
    }

    private JPanel setupStatisticsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        _numTrianglesCulled = new JTextField(8);
        _numTrianglesVisible = new JTextField(8);
        _numVerticesClipped = new JTextField(8);

        panel.add(setupStatisticPanel("Num Tris Culled", _numTrianglesCulled));
        panel.add(setupStatisticPanel("Num Tris Visible", _numTrianglesVisible));
        panel.add(setupStatisticPanel("Num Verts Clipped", _numVerticesClipped));

        return panel;
    }

    private JPanel setupStatisticPanel(String title, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel label = new JLabel(title);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        textField.setEditable(false);
        textField.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, CONTROL_HEIGHT));
        textField.setMinimumSize(new Dimension(TEXT_FIELD_WIDTH, CONTROL_HEIGHT));
        textField.setMaximumSize(new Dimension(TEXT_FIELD_WIDTH, CONTROL_HEIGHT));
        textField.setText("0");

        panel.add(label);
        panel.add(textField);
        return panel;
    }

    private void setupCenterPanel() {
        _renderPanel = new RenderPanel();
        getContentPane().add(BorderLayout.CENTER, _renderPanel);

        _internalSet = true;
        _noneFaceCulling.setSelected(_renderPanel.getDrawingOptions().getFaceCulling() == DrawingOptions.FaceCulling.NONE);
        _frontFaceCulling.setSelected(_renderPanel.getDrawingOptions().getFaceCulling() == DrawingOptions.FaceCulling.FRONT);
        _backFaceCulling.setSelected(_renderPanel.getDrawingOptions().getFaceCulling() == DrawingOptions.FaceCulling.BACK);
        _wireframeDrawingMode.setSelected(_renderPanel.getDrawingOptions().getDrawingMode() == DrawingOptions.DrawingMode.WIREFRAME);
        _solidDrawingMode.setSelected(_renderPanel.getDrawingOptions().getDrawingMode() == DrawingOptions.DrawingMode.SOLID);
        _noneVisibleSurfaces.setSelected(_renderPanel.getDrawingOptions().getVisibleSurfaces() == DrawingOptions.VisibleSurfaces.NONE);
        _paintersVisibleSurfaces.setSelected(_renderPanel.getDrawingOptions().getVisibleSurfaces() == DrawingOptions.VisibleSurfaces.PAINTERS);
        _orthographicProjection.setSelected(_renderPanel.getProjection() == RenderPanel.Projection.ORTHOGRAPHIC);
        _perspectiveProjection.setSelected(_renderPanel.getProjection() == RenderPanel.Projection.PERSPECTIVE);
        _internalSet = false;

        _renderPanel.setPerspectiveProjection(RenderPanel.DEFAULT_VERTICAL_FOV, RenderPanel.DEFAULT_PERSP_NCP, RenderPanel.DEFAULT_PERSP_FCP);

        rebuildModelMatrix();
        rebuildViewMatrix();
        _projectionMatrixPanel.setMatrix(_renderPanel.getProjectionMatrix());
    }

    private void rebuildViewMatrix() {
        _renderPanel.setCameraPosition(_cameraPanel.getPosition());
        _renderPanel.setCameraRotation(_cameraPanel.getRotation());
        _viewMatrixPanel.setMatrix(_renderPanel.getViewMatrix());
    }

    private void rebuildModelMatrix() {
        _renderPanel.setObjectPosition(_modelPanel.getPosition());
        _renderPanel.setObjectRotation(_modelPanel.getRotation());
        _renderPanel.setObjectScale(_modelPanel.getScale());
        _modelMatrixPanel.setMatrix(_renderPanel.getModelMatrix());
    }

    public static void main(String[] args) {
        MainWindow mainWindow = new MainWindow();
        mainWindow.pack();
        mainWindow.setLocationRelativeTo(null);
        mainWindow.setVisible(true);
        mainWindow.setExtendedState(mainWindow.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }
}
