package me.mrkm.grid;

import android.graphics.Color;

import java.util.Random;

public class Piece {

    public static int size = 5;

    public Cell[][] cells;
    public boolean enabled;

    public Piece() {
        cells = new Cell[size][size];
        createPiece();
    }

    public void reset() {
        createPiece();
    }

    private void createPiece () {
        enabled = true;

        Random r = new Random();
        float[] hsv = {255f * r.nextFloat(), 1f, 1f};
        int color = Color.HSVToColor(hsv);

        int templateID = (int) (r.nextFloat() * (float) PieceTemplates.templates.length);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                if (PieceTemplates.templates[templateID][x][y] == 1) {
                    cells[x][y] = new Cell(x, y, color, true);
                } else {
                    cells[x][y] = new Cell(x, y, color, false);
                }

            }
        }
    }

}
