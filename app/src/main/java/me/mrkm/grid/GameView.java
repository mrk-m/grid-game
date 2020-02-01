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
            chunks[i] = new Chunk(i * screenWidth / NUMBER_OF_CHUNKS - Chunk.BLOCK_SIZE, screenHeight - (screenHeight - screenWidth) / 2);
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
            if (chunk == null) {
                if (touchY > screenHeight - (screenHeight - screenWidth) / 2) {
                    for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
                        if (touchX > i * screenWidth / NUMBER_OF_CHUNKS &&
                                touchX <= (i + 1) * screenWidth / NUMBER_OF_CHUNKS) {
                            if (!chunks[i].used) {
                                chunk = chunks[i];
                                chunk.used = true;
                            }
                        }
                    }
                }
            }
        } else {
            if (chunk != null) {
                boolean valid = grid.validate(touchX, touchY, chunk);
                System.out.println("valid:"+valid);
                if (valid) {
                    score += scoreChunk(chunk);
                    grid.place(touchX, touchY, chunk);
                    counter = 0;
                    score += grid.update();

                    chunk.setOrigin(grid.getPosX(touchX), grid.getPosY(touchY));
                } else {
                    chunk.used = false;
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
            chunk.x = touchX - Chunk.BLOCK_SIZE * 2.5f;
            chunk.y = touchY - Chunk.BLOCK_SIZE * 2.5f;
        }


        if (counter < 12) {
            counter++;
        } else if (counter == 12) {
            grid.placeMask();
            grid.update();

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

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Palette.BACKGROUND);

            grid.draw(canvas, paint);
            
            drawChunks(canvas);

            paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(100);
            canvas.drawText("Score / " + (int) score,100,200, paint);

        }
    }

    private void drawChunks(Canvas canvas) {

        for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {

            if (chunks[i].active) {
                canvas.translate(chunks[i].x, chunks[i].y);

                if (!chunks[i].used) {
                    canvas.translate(Chunk.BLOCK_SIZE * 1.6f, 0);

                    canvas.scale(1f/2, 1f/2);
                }

                for (int y = 0; y < Chunk.SIZE; y++) {
                    for (int x = 0; x < Chunk.SIZE; x++) {
                        if (chunks[i].data[y * Chunk.SIZE + x] != 0) {
                            paint.setColor(Palette.getColor(chunks[i].color));
                            canvas.drawRect(x * Chunk.BLOCK_SIZE + Chunk.BLOCK_PADDING,
                                    y * Chunk.BLOCK_SIZE + Chunk.BLOCK_PADDING,
                                    (x + 1) * Chunk.BLOCK_SIZE - Chunk.BLOCK_PADDING,
                                    (y + 1) * Chunk.BLOCK_SIZE - Chunk.BLOCK_PADDING,
                                    paint);
                        }
                    }
                }

                if (!chunks[i].used) {

                    canvas.scale(1f*2, 1f*2);
                    canvas.translate(-Chunk.BLOCK_SIZE * 1.6f, 0);

                }

                canvas.translate(-chunks[i].x,-chunks[i].y);
            }

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
