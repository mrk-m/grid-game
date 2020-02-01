package me.mrkm.grid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static me.mrkm.grid.Grid.WIDTH;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private Paint paint;

    private int screenWidth;
    private int screenHeight;

    private boolean touch;
    private float touchX = 0f;
    private float touchY = 0f;

    private Grid grid;

    public static final int NUMBER_OF_CHUNKS = 3;
    private Chunk[] chunks;
    private Chunk chunk;

    private float score = 0;
    private int counter = 0;
    private boolean restock = false;

    private Mask mask;

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        load();

        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    public void load() {
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        Chunk.setBlockSizeAndPadding(screenWidth  / 11, screenWidth / 220);

        paint = new Paint();

        grid = new Grid();
        grid.setPosition(screenWidth, screenHeight);

        chunks = new Chunk[NUMBER_OF_CHUNKS];

        resetChunks();
    }

    private void resetChunks() {
        for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
            chunks[i] = new Chunk(((float) (((float) i) + 0.5f) * (screenWidth / NUMBER_OF_CHUNKS)), (screenHeight - (screenHeight - screenWidth) / 8));
            chunks[i].x += screenWidth;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        touchX = e.getX();
        touchY = e.getY();

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            touch = true;
        }

        if (e.getAction() == MotionEvent.ACTION_UP) {
            touch = false;
        }

        handleTouch();
        
        return true;
    }

    private void handleTouch() {
        if (touch) {
            if (chunk == null &&  counter > 12) {
                if (touchY > screenHeight - (screenHeight - screenWidth) / 2) {
                    for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
                        if (touchX > i * screenWidth / NUMBER_OF_CHUNKS &&
                                touchX <= (i + 1) * screenWidth / NUMBER_OF_CHUNKS) {
                            if (!chunks[i].used) {
                                chunk = chunks[i];
                                chunk.used = true;
                                chunk.targetScale = 1;
                                chunk.targetPadding = Chunk.BLOCK_PADDING * 2;

                            }
                        }
                    }
                }
            }
        } else {
            if (chunk != null) {
                float vx = touchX;
                float vy = touchY;

                boolean valid = grid.validate(vx, vy, chunk);
                System.out.println("valid:"+valid);
                if (valid) {
                    score += scoreChunk(chunk);
                    grid.place(vx, vy, chunk);
                    chunk.targetPadding = Chunk.BLOCK_PADDING;
                    counter = 0;


                    chunk.setOrigin(grid.getPosX(vx), grid.getPosY(vy));
                } else {
                    chunk.used = false;
                    chunk.targetScale = 2;
                    chunk.targetPadding = Chunk.BLOCK_PADDING;
                }
            }

            chunk = null;
    
            restock = true;
            for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
                if (!chunks[i].used) {
                    restock = false;
                }
            }
        }
    }

    private float scoreChunk(Chunk chunk) {
        float s = 0;

        for (int i = 0; i < chunk.data.length; i++) {
            if (chunk.data[i] != 0) {
                s += 1;
            }
        }

        return s;
    }

    public void update() {

        for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
            chunks[i].update();
        }

        if (chunk != null) {
            chunk.x = touchX;
            chunk.y = touchY - chunk.height;
        }


        if (counter < 12) {
            counter++;
        } else if (counter == 12) {
            grid.placeMask();
            score += grid.update();
            mask = new Mask(grid.mask);
            for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
                if (chunks[i].used) {
                    chunks[i].active = false;
                }
            }

            if (restock) {
                resetChunks();
                restock = false;
            }
            counter++;
        }

        if (mask != null)
        mask.update();

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Palette.BACKGROUND);

            grid.draw(canvas, paint);
            
            drawChunks(canvas);

            drawMask(canvas);

            paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(100);
            canvas.drawText("Score / " + (int) score,100,200, paint);

        }
    }

    private void drawChunks(Canvas canvas) {

        for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {

            if (chunks[i].active) {
                canvas.translate(chunks[i].x, chunks[i].y);

                canvas.scale(1f/chunks[i].scale, 1f/chunks[i].scale);

                for (int y = 0; y < Chunk.SIZE; y++) {
                    for (int x = 0; x < Chunk.SIZE; x++) {
                        if (chunks[i].data[y * Chunk.SIZE + x] != 0) {
                            paint.setColor(Palette.getColor(chunks[i].color));
                            canvas.drawRect((x - chunks[i].width / 2)  * Chunk.BLOCK_SIZE + chunks[i].padding,
                                    (y - chunks[i].height) * Chunk.BLOCK_SIZE + chunks[i].padding,
                                    ((x + 1) - chunks[i].width / 2) * Chunk.BLOCK_SIZE - chunks[i].padding,
                                    ((y + 1) - chunks[i].height) * Chunk.BLOCK_SIZE - chunks[i].padding,
                                    paint);
                        }
                    }
                }

                canvas.scale(1f*chunks[i].scale, 1f*chunks[i].scale);

                canvas.translate(-chunks[i].x,-chunks[i].y);
            }

        }
    }

    private void drawMask(Canvas canvas) {
        if (mask != null)
        if (!mask.hidden) {
            canvas.translate(grid.x, grid.y);

            for (int y = 0; y < Grid.HEIGHT; y++) {
                for (int x = 0; x < Grid.WIDTH; x++) {
                    if (mask.data[y * Grid.WIDTH + x]) {
                        paint.setColor(Palette.FOREGROUND);
                        canvas.drawRect((x) * Chunk.BLOCK_SIZE + mask.padding,
                                (y) * Chunk.BLOCK_SIZE + mask.padding,
                                ((x + 1)) * Chunk.BLOCK_SIZE - mask.padding,
                                ((y + 1)) * Chunk.BLOCK_SIZE - mask.padding,
                                paint);
                    }
                }
            }

            canvas.translate(-grid.x, -grid.y);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }
}
