package me.mrkm.grid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Grid {

    public static float cellSize;
    public static float cellPadding;
    public static float cellRadius;

    private float screenWidth, screenHeight;

    private int width = 10;
    private int height = 10;

    private Cell[][] cells;

    public float score = 0f;

    public Grid(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        cellSize = this.screenWidth  / 11;
        cellPadding = cellSize / 20;
        cellRadius = cellSize / 10;

        cells = new Cell[width][height];

        for (int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int color = Color.LTGRAY;
//                float[] hsv = {255f / (width * height) * (x * 2) * y, 1f, 1f};
//                color = Color.HSVToColor(hsv);
                cells[x][y] = new Cell(x, y, color, false);
            }
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.translate(screenWidth / 2 - width * cellSize / 2,screenHeight / 2 - height * cellSize / 2);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                drawCell(cells[x][y], canvas, paint);
            }
        }

        canvas.translate(-(screenWidth / 2 - width * cellSize / 2),-(screenHeight / 2 - height * cellSize / 2));

    }

    private void drawCell(Cell cell, Canvas canvas, Paint paint) {


        float left = cell.x * cellSize + cellPadding;
        float top = cell.y * cellSize + cellPadding;

        float right = (cell.x + 1) * cellSize - cellPadding;
        float bottom = (cell.y + 1) * cellSize - cellPadding;

        RectF rect = new RectF(left,top,right,bottom);

        paint.setColor(cell.color);
        canvas.drawRoundRect(rect,cellRadius,cellRadius,paint);
    }

    public boolean validate(float touchX, float touchY, Piece pickUp) {
        float x = touchX - (screenWidth / 2 - width * cellSize / 2);
        float y = touchY - (screenHeight / 2 - height * cellSize / 2) - cellSize / 2;

        // TOP LEFT
        int sx = (int) (x / cellSize) - 2;
        int sy = (int) (y / cellSize) - 4;

//        if ((sx >= 0 && sx < width) && (sy >= 0 && sy < height)) {
//            cells[sx][sy].color = Color.GREEN;
//        }


        // ITERATE THROUGH VCEELLS
        boolean isValid = true;

        for (int ix = 0; ix < 5; ix++) {
            for (int iy = 0; iy < 5; iy++) {
                if ((ix + sx >= 0 && ix + sx < width) && (iy + sy >= 0 && iy + sy < height)) {
                    if (cells[ix + sx][iy + sy].solid && pickUp.cells[ix][iy].solid) {
                        isValid = false;
                    }
                } else {
                    if ( pickUp.cells[ix][iy].solid) {
                        isValid = false;

                    }
                }
            }
        }

        return isValid;
    }

    public void place(float touchX, float touchY, Piece pickUp) {
        float x = touchX - (screenWidth / 2 - width * cellSize / 2);
        float y = touchY - (screenHeight / 2 - height * cellSize / 2) - cellSize / 2;

        // TOP LEFT
        int sx = (int) (x / cellSize) - 2;
        int sy = (int) (y / cellSize) - 4;

//        if ((sx >= 0 && sx < width) && (sy >= 0 && sy < height)) {
//            cells[sx][sy].color = Color.GREEN;
//        }


        // ITERATE THROUGH VCEELLS
        boolean isValid = true;

        for (int ix = 0; ix < 5; ix++) {
            for (int iy = 0; iy < 5; iy++) {
                if ((ix + sx >= 0 && ix + sx < width) && (iy + sy >= 0 && iy + sy < height)) {
                    if (cells[ix + sx][iy + sy].solid && pickUp.cells[ix][iy].solid) {
                        isValid = false;
                    } else {
                        if (pickUp.cells[ix][iy].solid) {
                            cells[ix + sx][iy + sy].color = pickUp.cells[ix][iy].color;
                            cells[ix + sx][iy + sy].solid = pickUp.cells[ix][iy].solid;
                        }

                    }
                } else {
                    if ( pickUp.cells[ix][iy].solid) {
                        isValid = false;

                    }
                }
            }
        }
    }

    public void update() {
        boolean[][] mask = new boolean[width][height];

        // rows
        for (int y = 0; y < height; y++) {
            boolean clear = true;

            for (int x = 0; x < width; x++) {
                if (!cells[x][y].solid) {
                    clear = false;
                }
            }

            if (clear) {
                for (int x = 0; x < width; x++) {
                    mask[x][y] = true;
                }
            }
        }

        // columns
        for (int x = 0; x < height; x++) {
            boolean clear = true;

            for (int y = 0; y < height; y++) {
                if (!cells[x][y].solid) {
                    clear = false;
                }
            }

            if (clear) {
                for (int y = 0; y < height; y++) {
                    mask[x][y] = true;
                }
            }
        }

        float scoreAdd = 1;

        // clear mask
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < height; y++) {
                if (mask[x][y]) {
                    cells[x][y].solid = false;
                    cells[x][y].color = Color.LTGRAY;
                    scoreAdd += 1;
                    scoreAdd *= 1.1;
                }
            }
        }


        // calculate score
        System.out.println(scoreAdd);
        score += scoreAdd;
    }
}
