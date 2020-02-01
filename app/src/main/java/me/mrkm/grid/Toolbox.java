package me.mrkm.grid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.logging.Logger;

public class Toolbox {

    private int size = 3;

    private Piece[] pieces;

    private float width, height;
    private float screenWidth, screenHeight;

    private boolean touch = false;

    private Piece pickUp;

    public Toolbox(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        width = screenWidth;
        height = (screenHeight - screenWidth) / 2;

        pieces = new Piece[size];
        for (int i = 0; i < size; i++) {
            pieces[i] = new Piece();
        }

        refill();
    }

    private void refill() {
        for (int i = 0; i < size; i++) {
            pieces[i].reset();
        }
    }

    public void draw(Canvas canvas, Paint paint, float touchX, float touchY) {

        float padding = 1f;
        float scale = .5f;

//        paint.setColor(Color.LTGRAY);
//        canvas.drawRect(0,screenHeight - height, width, screenHeight, paint);

        for (int i = 0; i < size; i++) {

            if (pieces[i].enabled) {

                float dx = (width / size) * i + (width / size / 2) - Grid.cellSize * scale * Piece.size / 2;
                float dy = screenHeight - height + (height / 2) - Grid.cellSize * scale * Piece.size / 2;

                canvas.translate(dx, dy);

                for (int y = 0; y < Piece.size; y++) {
                    for (int x = 0; x < Piece.size; x++) {
                        drawCell(pieces[i].cells[x][y], canvas, paint, padding, scale);
                    }
                }

                canvas.translate(-dx, -dy);
            }
        }

        // Draw picked up piece
        if (pickUp != null) {

            float dx = touchX - Grid.cellSize * Piece.size / 2;
            float dy = touchY - Grid.cellSize * Piece.size;

            canvas.translate(dx, dy);

            for (int y = 0; y < Piece.size; y++) {
                for (int x = 0; x < Piece.size; x++) {
                    drawCell(pickUp.cells[x][y], canvas, paint, 2f, 1f);
                }
            }

            canvas.translate(-dx, -dy);
        }
    }

    private void drawCell(Cell cell, Canvas canvas, Paint paint, float padding, float scale) {
        if (cell.solid) {
            float left = cell.x * Grid.cellSize * scale + Grid.cellPadding * scale * padding;
            float top = cell.y * Grid.cellSize * scale + Grid.cellPadding * scale * padding;

            float right = (cell.x + 1) * Grid.cellSize * scale - Grid.cellPadding * scale * padding;
            float bottom = (cell.y + 1) * Grid.cellSize * scale - Grid.cellPadding * scale * padding;

            paint.setColor(cell.color);
            canvas.drawRoundRect(left, top, right, bottom, Grid.cellRadius * scale, Grid.cellRadius * scale, paint);
        }
    }

    public void touch(float touchX, float touchY) {
        if (touch == false) {
            if (touchY > screenHeight - (screenHeight - screenWidth) / 2) {
                for (int i = 0; i < size; i++) {
                    if (touchX > i * width / size && touchX <= (i+1) * width / size) {
                        if (pieces[i].enabled) {
                            pickUp = pieces[i];
                            pickUp.enabled = false;
                            touch = true;
                        }
                    }
                }
            }
        }

    }

    public void unTouch(float touchX, float touchY, Grid grid) {
        touch = false;

        if (pickUp != null) {
            boolean valid = grid.validate(touchX, touchY, pickUp);
            System.out.println("valid:"+valid);
            if (valid) {
                grid.place(touchX, touchY, pickUp);
                grid.update();
            } else {
                pickUp.enabled = true;
            }
        }

        pickUp = null;

        boolean restock = true;
        for (int i = 0; i < size; i++) {
            if (pieces[i].enabled) {
                restock = false;
            }
        }

        if (restock) refill();
    }
}
