import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DatFile {
    private Vector3[] _vertices;
    private Triangle[] _triangles;

    // https://stackoverflow.com/questions/2231369/scanner-vs-bufferedreader
    public DatFile(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        readVertices(bufferedReader);
        readTriangles(bufferedReader);
    }

    public Vector3[] getVertices() { return _vertices; }
    public Triangle[] getTriangles() { return _triangles; }

    private void readVertices(BufferedReader bufferedReader) throws IOException {
        String line = bufferedReader.readLine();
        int numVertices = Integer.parseInt(line);
        _vertices = new Vector3[numVertices];

        for (int i = 0; i < numVertices; ++i) {
            line = bufferedReader.readLine();
            String[] splits = line.split("\\s+");
            if (splits.length != 4) {
                throw new IOException("Unrecognised file format");
            }

            int index = Integer.parseInt(splits[0]);
            if (index < 0 || index >= numVertices) {
                throw new IOException("Invalid vertex index within the file (index = " + index + ")");
            }

            _vertices[index] = new Vector3(
                    Double.parseDouble(splits[1]),
                    Double.parseDouble(splits[2]),
                    Double.parseDouble(splits[3])
            );
        }
    }

    private void readTriangles(BufferedReader bufferedReader) throws IOException {
        String line = bufferedReader.readLine();
        int numTriangles = Integer.parseInt(line);
        _triangles = new Triangle[numTriangles];

        for (int i = 0; i < numTriangles; ++i)
        {
            line = bufferedReader.readLine();
            String[] splits = line.split("\\s+");
            if (splits.length != 4) {
                throw new IOException("Unrecognised file format");
            }

            int index = Integer.parseInt(splits[0]);
            if (index < 0 || index >= numTriangles) {
                throw new IOException("Invalid triangle index within the file (index = " + index + ")");
            }

            _triangles[index] = new Triangle(
                    Integer.parseInt(splits[1]),
                    Integer.parseInt(splits[2]),
                    Integer.parseInt(splits[3]));
        }
    }
}
