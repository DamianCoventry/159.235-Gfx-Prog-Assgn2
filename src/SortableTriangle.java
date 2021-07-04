public class SortableTriangle implements Comparable<SortableTriangle> {
    public int _index;
    public double _z;

    public SortableTriangle() {
        _index = 0;
        _z = 0;
    }

    @Override
    public int compareTo(SortableTriangle t) {
        if (_z == t._z) {
            return 0;
        }
        return _z < t._z ? 1 : -1;
    }
}
