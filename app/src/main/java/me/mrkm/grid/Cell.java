package me.mrkm.grid;

public class Cell {

    public int x;
    public int y;
    public int color;
    public boolean solid;

    public Cell(int x, int y, int color, boolean solid) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.solid = solid;
    }

}
