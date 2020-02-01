package me.mrkm.grid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import java.util.Vector;

public class Grid {

    public static final int WIDTH = 10;
    public static final int HEIGHT = 10;

    public int x, y;
    private int scaleWidth, scaleHeight;

    private int[] blocks;
    private int[] blocksMask;

    public boolean[] mask;

    public Grid() {
        blocks = new int[WIDTH * HEIGHT];
        blocksMask = new int[WIDTH * HEIGHT];


        clear();
    }

    public void setPosition(int screenWidth, int screenHeight) {
        scaleWidth = WIDTH * Chunk.BLOCK_SIZE;
        scaleHeight = HEIGHT * Chunk.BLOCK_SIZE;

        x = (screenWidth - scaleWidth) / 2;
        y = (screenHeight - scaleHeight) / 2;
    }

    private void clear() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                blocks[y * WIDTH + x] = 0;
                blocksMask[y * WIDTH + x] = 0;
            }
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.translate(x,y);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                paint.setColor(Palette.getColor(blocks[y * WIDTH + x]));
                canvas.drawRect(x * Chunk.BLOCK_SIZE + Chunk.BLOCK_PADDING,
                        y * Chunk.BLOCK_SIZE  + Chunk.BLOCK_PADDING,
                        (x+1) * Chunk.BLOCK_SIZE - Chunk.BLOCK_PADDING,
                        (y+1) * Chunk.BLOCK_SIZE - Chunk.BLOCK_PADDING,
                        paint);
            }
        }

        canvas.translate(-x,-y);
    }

    public Point getPos(float x, float y, float width, float height) {
        return new Point((int) (((x - this.x) / Chunk.BLOCK_SIZE)), (int) (((y - this.y) / Chunk.BLOCK_SIZE) - height / 2));
    }

    public float getPosY(float touchY) {
        return y + ((((int) (touchY + Chunk.BLOCK_SIZE / 2 - y) / Chunk.BLOCK_SIZE)) * Chunk.BLOCK_SIZE);
    }

    public float getPosX(float touchX) {
        return x + ((((int) (touchX - x) / Chunk.BLOCK_SIZE)) * Chunk.BLOCK_SIZE  + Chunk.BLOCK_SIZE / 2);
    }

    public boolean validate(float touchX, float touchY, Chunk chunk) {
        Point pos = getPos(touchX, touchY, chunk.width, chunk.height);
        int gridX = pos.x;
        int gridY = pos.y;

        System.out.println("x" + gridX + " y" + gridY);


        int startX = gridX - 2;
        int startY = gridY - 2;

        boolean isValid = true;

        for (int ix = 0; ix < 5; ix++) {
            for (int iy = 0; iy < 5; iy++) {
                if ((ix + startX >= 0 && ix + startX < WIDTH) &&
                        (iy + startY >= 0 && iy + startY < HEIGHT)) {

                    if (
                            (blocks[(iy + startY) * WIDTH + (ix + startX)] != 0 ||
                            blocksMask[(iy + startY) * WIDTH + (ix + startX)] != 0) &&
                            chunk.data[iy * Chunk.SIZE + ix] != 0) {
                        isValid = false;
                    }
                } else {
                    if (chunk.data[iy * Chunk.SIZE + ix] != 0) {
                        isValid = false;

                    }
                }
            }
        }

        return isValid;
    }

    public void place(float touchX, float touchY, Chunk chunk) {
        Point pos = getPos(touchX, touchY, chunk.width, chunk.height);
        int gridX = pos.x;
        int gridY = pos.y;

        int startX = gridX - 2;
        int startY = gridY - 2;


        for (int ix = 0; ix < 5; ix++) {
            for (int iy = 0; iy < 5; iy++) {
                if ((ix + startX >= 0 && ix + startX < WIDTH) &&
                        (iy + startY >= 0 && iy + startY < HEIGHT)) {

                    if ((blocks[(iy + startY) * WIDTH + ix + startX] == 0 &&
                            blocksMask[(iy + startY) * WIDTH + ix + startX] == 0 &&
                            chunk.data[iy * Chunk.SIZE + ix] != 0)) {
                        // PLACE
                        blocksMask[(iy + startY) * WIDTH + ix + startX] = chunk.color;
                    }
                }
            }
        }
    }



    public float update() {
        mask = new boolean[WIDTH * HEIGHT];

        float score = 0;

        // rows
        for (int y = 0; y < HEIGHT; y++) {
            boolean clear = true;

            for (int x = 0; x < WIDTH; x++) {
                if (blocks[y * WIDTH + x] == 0) {
                    clear = false;
                }
            }

            if (clear) {
                for (int x = 0; x < WIDTH; x++) {
                    mask[y * WIDTH + x] = true;
                }
            }
        }

        // columns
        for (int x = 0; x < WIDTH; x++) {
            boolean clear = true;

            for (int y = 0; y < HEIGHT; y++) {
                if (blocks[y * WIDTH + x] == 0) {
                    clear = false;
                }
            }

            if (clear) {
                for (int y = 0; y < HEIGHT; y++) {
                    mask[y * WIDTH + x] = true;
                }
            }
        }

        // clear mask
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (mask[y * WIDTH + x]) {
                    blocks[y * WIDTH + x] = 0;
                    score += 1;
                    score *= 1.1;
                }
            }
        }

        return score;
    }

    public void placeMask() {
        for (int i = 0; i < blocks.length; i++) {
            if (blocksMask[i] != 0) {
                blocks[i] = blocksMask[i];
                blocksMask[i] = 0;
            }
        }
    }
}
