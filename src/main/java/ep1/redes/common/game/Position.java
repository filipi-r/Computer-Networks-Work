package ep1.redes.common.game;

public record Position(int row, int column) {
    public int x() { return column; }
    public int y() { return row; }
}
