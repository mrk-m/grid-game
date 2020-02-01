package me.mrkm.grid;

public class Mask {

    public float padding;
    public float targetPadding;
    public boolean hidden;
    public boolean[] data;

    public Mask(boolean[] data) {
        this.data = data;

        padding = Chunk.BLOCK_PADDING;
        targetPadding = Chunk.BLOCK_SIZE / 2;
        hidden = false;
    }

    public void update() {
        if (padding < targetPadding) {
            padding += (targetPadding - padding) / 7;
        } else {
            hidden = true;
        }
    }
}
