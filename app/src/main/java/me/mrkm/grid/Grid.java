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

    public Point getPos(float x, float y) {
        return new Point(getGridX(x), getGridY(y));
    }

    private int getGridY(float y) {
        return (int) (((y - this.y) / Chunk.BLOCK_SIZE));
    }

    private int getGridX(float x) {
        return (int) (((x - this.x) / Chunk.BLOCK_SIZE));
    }

    public float getPosY(float touchY) {
        return y + getGridY(touchY) * Chunk.BLOCK_SIZE;
    }

    public float getPosX(float touchX) {
        return x + getGridX(touchX) * Chunk.BLOCK_SIZE;
    }

    public boolean validate(float touchX, float touchY, Chunk chunk) {
        Point pos = getPos(touchX, touchY);
        int gridX = pos.x;
        int gridY = pos.y;

        int startX = gridX - 2;
        int startY = gridY - 5;

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
        Point pos = getPos(touchX, touchY);
        int gridX = pos.x;
        int gridY = pos.y;

        int startX = gridX - 2;
        int startY = gridY - 5;


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

    public boolean validateChunkPlayable(Chunk chunk) {

        boolean playable = false;

        for (int gridX = 0; gridX < WIDTH; gridX++) {
            for (int gridY = 0; gridY < HEIGHT; gridY++) {
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
                            } else {
                            }
                        } else {
                            if (chunk.data[iy * Chunk.SIZE + ix] != 0) {
                                isValid = false;

                            }
                        }
                    }
                }

                if (isValid) {
                    playable = true;
                }
            }
        }

        return playable;
    }

    public void reset() {
        clear();
    }

    public void load(String dataString) {
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = (byte) dataString.charAt(i);
        }

        for (int i = 0; i < blocksMask.length; i++) {
            blocksMask[i] = (byte) dataString.charAt(i + blocks.length);;
        }
    }

    public String save() {
        String data = "";

        for (int i = 0; i < blocks.length; i++) {
            data += ((char)((byte)blocks[i]));
        }

        for (int i = 0; i < blocksMask.length; i++) {
            data += ((char)((byte)blocksMask[i]));
        }

        return data;
    }
}
