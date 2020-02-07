package me.mrkm.grid;

import java.util.Random;

public class Chunk {

    private final int[][] CHUNKS = {
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,1,0,0,
        0,0,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,1,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,1,1,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,1,1,1,0},
        {0,0,0,0,0,
        0,0,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,1,1,1,1},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        1,1,1,1,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        1,1,1,1,1},
        {0,0,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,1,1,0,0,
        0,1,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,1,1,0,
        0,0,1,1,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,1,1,1,0,
        0,1,1,1,0,
        0,1,1,1,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,1,1,0,0,
        0,0,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,1,1,0,
        0,0,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,1,0,0,
        0,1,1,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,1,0,
        0,0,1,1,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,1,1,1,0,
        0,0,0,1,0,
        0,0,0,1,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,1,1,1,0,
        0,1,0,0,0,
        0,1,0,0,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,1,0,0,0,
        0,1,0,0,0,
        0,1,1,1,0},
        {0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,1,0,
        0,0,0,1,0,
        0,1,1,1,0},
    };

    public static final int SIZE = 5;

    public static int BLOCK_SIZE;
    public static int BLOCK_PADDING;

    public int data[];
    public int color;
    public boolean active;
    public boolean used;

    public float x,y;
    public float originX, originY;

    public float scale;
    public float targetScale;

    public float padding;
    public float targetPadding;

    public float offsetX;
    public float offsetY;

    public Chunk(float x, float y) {
        this.x = x;
        this.y = y;
        originX = x;
        originY = y;

        scale = 3f;
        targetScale = 3f;

        padding = Chunk.BLOCK_PADDING;
        targetPadding = Chunk.BLOCK_PADDING;

        int type = new Random().nextInt(CHUNKS.length);
        data = CHUNKS[type];
//        int[] test = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
//        data = test;

        // Calculate width and height

        // width
        int leftIndex = SIZE;
        int rightIndex = 0;
        for (int iy = 0; iy < SIZE; iy++) {
            for (int ix = 0; ix < SIZE; ix++) {
                if (data[iy * SIZE + ix] != 0) {
                    if (ix < leftIndex) {
                        leftIndex = ix;
                    }
                    if (ix > rightIndex) {
                        rightIndex = ix;
                    }
                }
            }
        }

        color = new Random().nextInt(Palette.colors.length - 1) + 1;

        active = true;
        used = false;
    }

    public void update() {
        if (x != originX) {
            x -= (x - originX) / 5;
        }

        if (y != originY) {
            y -= (y - originY) / 5;
        }

        if (scale != targetScale) {
            scale += (targetScale - scale) / 3;
        }

        if (padding != targetPadding) {
            padding += (targetPadding - padding) / 5;
        }
    }

    public static void setBlockSizeAndPadding(int size, int padding) {
        BLOCK_SIZE = size;
        BLOCK_PADDING = padding;
    }

    public void setOrigin(float posX, float posY) {
        originX = posX + BLOCK_SIZE / 2f;
        originY = posY;
    }
}
