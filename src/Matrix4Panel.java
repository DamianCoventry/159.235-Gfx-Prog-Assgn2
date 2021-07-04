import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class Matrix4Panel extends JPanel {
    private final JTextField[][] _textFields;
    private final DecimalFormat _decimalFormat;

    public Matrix4Panel(String title) {
        _textFields = new JTextField[4][4];
        _decimalFormat = new DecimalFormat("0.000");

        setBorder(BorderFactory.createTitledBorder(title));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for (int row = 0; row < 4; ++row) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            for (int col = 0; col < 4; ++col) {
                _textFields[row][col] = new JTextField();
                _textFields[row][col].setEditable(false);
                _textFields[row][col].setHorizontalAlignment(SwingConstants.RIGHT);
                _textFields[row][col].setMinimumSize(new Dimension(50, 30));
                _textFields[row][col].setMaximumSize(new Dimension(50, 30));
                panel.add(_textFields[row][col]);
            }
            add(panel);
        }
    }

    public void setMatrix(Matrix4 matrix) {
        Matrix4 m;
        if (matrix != null) {
            m = matrix;
        }
        else {
            m = new Matrix4();
        }
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 4; ++col) {
                _textFields[row][col].setText(_decimalFormat.format(m._m[row][col]));
            }
        }
    }
}
