package me.mrkm.grid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static me.mrkm.grid.Menu.*;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private Paint paint;
    private Paint stkPaint;

    private int screenWidth;
    private int screenHeight;

    private boolean touch;
    private boolean touchDown;
    private boolean touchDownTrigger;
    private boolean touchRelease;
    private float touchX = 0f;
    private float touchY = 0f;


    private Grid grid;

    public static final int NUMBER_OF_CHUNKS = 3;
    private Chunk[] chunks;
    private Chunk chunk;

    private float highscore;

    private float score = 0;
    private float displayScore = 0;
    private int counter = 0;
    private boolean restock = false;

    private Mask mask;

    public static Menu menu;


    private float splashCounter = 150;
    private float splashX;
    private float splashY;

    private RectF playButton, scoreButton, colorButton, settingsButton, resumeButton, exitButton,
    lightButton, darkButton;

    private boolean pauseDown;
    private boolean playDown;
    private boolean scoreDown;
    private boolean colorDown;
    private boolean settingsDown;
    private boolean resumeDown;
    private boolean exitDown;
    private boolean lightDown;
    private boolean darkDown;

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
        stkPaint = new Paint();
        stkPaint.setStyle(Paint.Style.STROKE);
        stkPaint.setStrokeWidth(Chunk.BLOCK_SIZE / 8);
        stkPaint.setColor(Palette.BACKGROUND);

        grid = new Grid();
        grid.setPosition(screenWidth, screenHeight);

        chunks = new Chunk[NUMBER_OF_CHUNKS];

        resetChunks();

        menu = SPLASH;

        playButton =
                new RectF(Chunk.BLOCK_SIZE, screenHeight / 6 *2- Chunk.BLOCK_SIZE / 2,screenWidth - Chunk.BLOCK_SIZE, screenHeight / 6 *3- Chunk.BLOCK_SIZE);
        scoreButton =
                new RectF(Chunk.BLOCK_SIZE, screenHeight / 6*3- Chunk.BLOCK_SIZE / 2,screenWidth - Chunk.BLOCK_SIZE, screenHeight / 6 *4- Chunk.BLOCK_SIZE);
        colorButton =
                new RectF(Chunk.BLOCK_SIZE, screenHeight / 6*4- Chunk.BLOCK_SIZE / 2,screenWidth - Chunk.BLOCK_SIZE, screenHeight / 6 *5- Chunk.BLOCK_SIZE);
        settingsButton =
                new RectF(Chunk.BLOCK_SIZE, screenHeight / 6*5- Chunk.BLOCK_SIZE / 2,screenWidth - Chunk.BLOCK_SIZE, screenHeight / 6 *6- Chunk.BLOCK_SIZE);

        resumeButton =
                new RectF(Chunk.BLOCK_SIZE, screenHeight / 6*4- Chunk.BLOCK_SIZE / 2,screenWidth - Chunk.BLOCK_SIZE, screenHeight / 6 *5- Chunk.BLOCK_SIZE);
        exitButton =
                new RectF(Chunk.BLOCK_SIZE, screenHeight / 6*5- Chunk.BLOCK_SIZE / 2,screenWidth - Chunk.BLOCK_SIZE, screenHeight / 6 *6- Chunk.BLOCK_SIZE);

        lightButton =
                new RectF(Chunk.BLOCK_SIZE, screenHeight / 6*3- Chunk.BLOCK_SIZE / 2,screenWidth - Chunk.BLOCK_SIZE, screenHeight / 6 *4- Chunk.BLOCK_SIZE);
        darkButton =
                new RectF(Chunk.BLOCK_SIZE, screenHeight / 6*4- Chunk.BLOCK_SIZE / 2,screenWidth - Chunk.BLOCK_SIZE, screenHeight / 6 *5- Chunk.BLOCK_SIZE);

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
            if (!touchDownTrigger)
                touchDown = true;
                touchDownTrigger = true;
        }

        if (e.getAction() == MotionEvent.ACTION_UP) {
            touch = false;
            touchRelease = true;
            touchDownTrigger = false;
        }

        handleTouch();

        touchDown = false;
        touchRelease = false;
        
        return true;
    }

    private void handleTouch() {
        if (menu != null) {
            handleMenuTouch();
        } else {
            if (touchRelease) {
                if (pauseDown) {
                    menu = PAUSE;
                    pauseDown = false;
                }
            }

            if (touchDown) {
                if (chunk == null && counter > 12) {
                    if (touchY < Chunk.BLOCK_SIZE * 2 && touchX < Chunk.BLOCK_SIZE * 2) {
                        pauseDown = true;
                    } else if (touchY > screenHeight - (screenHeight - screenWidth) / 2) {
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
            }

            if (!touch) {
                if (chunk != null) {
                    float vx = touchX;
                    float vy = touchY;

                    boolean valid = grid.validate(vx, vy, chunk);
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
    }

    private void handleMenuTouch() {
        if (menu == SPLASH) {

        }

        if (menu == MAIN) {
            if (touchDown) {
                if (playButton.contains(touchX, touchY)) {
                    playDown = true;
                }
                if (scoreButton.contains(touchX, touchY)) {
                    scoreDown = true;
                }
                if (colorButton.contains(touchX, touchY)) {
                    colorDown = true;
                }
                if (settingsButton.contains(touchX, touchY)) {
                    settingsDown = true;
                }
            }

            if (touchRelease) {
                if (playDown) {
                    menu = null;
                    playDown = false;
                } else if (scoreDown) {
                    menu = SCORE;
                    scoreDown = false;
                } else if (colorDown) {
                    menu = COLOR;
                    colorDown = false;
                } else if (settingsDown) {
                    menu = SETTINGS;
                    settingsDown = false;
                }
            }
        }

        if (menu == SCORE) {
            if (touchDown) {
                if (exitButton.contains(touchX, touchY)) {
                    exitDown = true;
                }
            }
            if (touchRelease) {
                if (exitDown) {
                    menu = MAIN;
                    exitDown = false;
                }
            }
        }

        if (menu == COLOR) {
            if (touchDown) {
                if (lightButton.contains(touchX, touchY)) {
                    lightDown = true;
                }
                if (darkButton.contains(touchX, touchY)) {
                    darkDown = true;
                }
                if (exitButton.contains(touchX, touchY)) {
                    exitDown = true;
                }
            }
            if (touchRelease) {
                if (lightDown) {
                    Palette.setLight();
                    lightDown = false;
                }
                if (darkDown) {
                    Palette.setDark();
                    darkDown = false;
                }
                if (exitDown) {
                    menu = MAIN;
                    exitDown = false;
                }
            }
        }

        if (menu == SETTINGS) {
            if (touchDown) {
                if (exitButton.contains(touchX, touchY)) {
                    exitDown = true;
                }
            }
            if (touchRelease) {
                if (exitDown) {
                    menu = MAIN;
                    exitDown = false;
                }
            }
        }

        if (menu == PAUSE) {
            if (touchDown) {
                if (resumeButton.contains(touchX, touchY)) {
                    resumeDown = true;
                }
                if (exitButton.contains(touchX, touchY)) {
                    exitDown = true;
                }
            }

            if (touchRelease) {
                if (resumeDown) {
                    menu = null;
                    resumeDown = false;
                } else if (exitDown) {
                    menu = MAIN;
                    exitDown = false;
                }
            }
        }

        if (menu == GAME_OVER) {
            if (touchDown) {
                if (resumeButton.contains(touchX, touchY)) {
                    resumeDown = true;
                }
                if (exitButton.contains(touchX, touchY)) {
                    exitDown = true;
                }
            }

            if (touchRelease) {
                if (resumeDown) {
                    menu = null;
                    resumeDown = false;
                } else if (exitDown) {
                    menu = MAIN;
                    exitDown = false;
                }
            }
        }
    }

    private void restartGame() {
        displayScore = 0;
        score = 0;
        grid.reset();
        resetChunks();
    }

    private void handleMenuUpdate() {
        if (menu == SPLASH) {
            if (splashCounter == 150) {
                splashX = screenWidth * 1.5f;
                splashY = screenHeight / 2;
            }
            splashCounter -= 1;
            if (splashCounter <= 30) {
                menu = MAIN;
            }

            if (splashCounter > 50) {
                splashX -= (splashX - screenWidth * 0.5f) / 5;
            }

            if (splashCounter < 50) {
                splashY -= (splashY - screenHeight * 0.175f) / 5;
            }
        }

        if (menu == MAIN) {

        }

        if (menu == SCORE) {

        }

        if (menu == COLOR) {

        }

        if (menu == SETTINGS) {

        }
    }

    private void handleMenuDraw(Canvas canvas) {
        if (menu == SPLASH) {
                        paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(Chunk.BLOCK_SIZE * 1.5f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Grid", splashX, splashY, paint);

        }
        if (menu == PAUSE) {
            paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(Chunk.BLOCK_SIZE * 1.5f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Paused", screenWidth / 2, screenHeight / 3, paint);

            paint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextAlign(Paint.Align.CENTER);

            if (resumeDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(8));
            canvas.drawRect(resumeButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Resume", resumeButton.centerX(), resumeButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Resume", resumeButton.centerX(), resumeButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

            if (exitDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(3));
            canvas.drawRect(exitButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

        }

        if (menu == GAME_OVER) {
            paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(Chunk.BLOCK_SIZE * 1.5f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Game Over", screenWidth / 2, screenHeight / 3, paint);

            paint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextAlign(Paint.Align.CENTER);

            if (resumeDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(8));
            canvas.drawRect(resumeButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Play Again", resumeButton.centerX(), resumeButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Play Again", resumeButton.centerX(), resumeButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

            if (exitDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(3));
            canvas.drawRect(exitButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

        }

        if (menu == MAIN) {
            paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(Chunk.BLOCK_SIZE * 1.5f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Grid", splashX, splashY, paint);

            paint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextAlign(Paint.Align.CENTER);



            if (playDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(8));
            canvas.drawRect(playButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Play", playButton.centerX(), playButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Play", playButton.centerX(), playButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

            if (scoreDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(3));
            canvas.drawRect(scoreButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Scores", scoreButton.centerX(), scoreButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Scores", scoreButton.centerX(), scoreButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

            if (colorDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(7));
            canvas.drawRect(colorButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Palette", colorButton.centerX(), colorButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Palette", colorButton.centerX(), colorButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

            if (settingsDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(6));
            canvas.drawRect(settingsButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Settings", settingsButton.centerX(), settingsButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Settings", settingsButton.centerX(), settingsButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);
        }

        if (menu == SCORE) {
            paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(Chunk.BLOCK_SIZE * 1.5f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Scores", splashX, splashY, paint);

            paint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            canvas.drawText("Highscore", splashX, screenHeight / 2, paint);
            canvas.drawText("" + (int) highscore, splashX, screenHeight / 2 + Chunk.BLOCK_SIZE * 1, paint);

            paint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextAlign(Paint.Align.CENTER);

            if (exitDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(3));
            canvas.drawRect(exitButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

        }

        if (menu == COLOR) {
            paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(Chunk.BLOCK_SIZE * 1.5f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Palette", splashX, splashY, paint);

            paint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextAlign(Paint.Align.CENTER);

            if (lightDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.MIDGROUND);

            canvas.drawRect(lightButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Light", lightButton.centerX(), lightButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Light", lightButton.centerX(), lightButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

            if (darkDown)
                paint.setColor(Palette.MIDGROUND);
            else
                paint.setColor(Palette.FOREGROUND);
            canvas.drawRect(darkButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Dark", darkButton.centerX(), darkButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Dark", darkButton.centerX(), darkButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);


            if (exitDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(3));
            canvas.drawRect(exitButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

        }

        if (menu == SETTINGS) {
            paint.setColor(Palette.FOREGROUND);
            paint.setTextSize(Chunk.BLOCK_SIZE * 1.5f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Settings", splashX, splashY, paint);

            paint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextSize(Chunk.BLOCK_SIZE * 1f);
            stkPaint.setTextAlign(Paint.Align.CENTER);

            if (exitDown)
                paint.setColor(Palette.FOREGROUND);
            else
                paint.setColor(Palette.getColor(3));
            canvas.drawRect(exitButton, paint);
            paint.setColor(Palette.TEXT);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, stkPaint);
            canvas.drawText("Menu", exitButton.centerX(), exitButton.centerY() + Chunk.BLOCK_SIZE / 3, paint);

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

        if (menu != null) {
            handleMenuUpdate();
        } else {


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

                checkGameOver();

            }

            if (mask != null)
                mask.update();

            if (displayScore < score)
                displayScore += (score - displayScore) / 100 * 5;

            if (displayScore > highscore) {
                highscore = displayScore;
            }

        }

    }

    private void checkGameOver() {
        boolean gameOver = true;

        for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
            if (!chunks[i].used) {
                if (grid.validateChunkPlayable(chunks[i]))
                    gameOver = false;
            }
        }

        if (gameOver) {
            if (displayScore < score)
                displayScore = score;

            if (displayScore > highscore) {
                highscore = displayScore;
            }

            menu = GAME_OVER;
            restartGame();
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {

            canvas.drawColor(Palette.BACKGROUND);

            if (menu != null) {
                handleMenuDraw(canvas);

            } else {
                grid.draw(canvas, paint);

                drawChunks(canvas);

                drawMask(canvas);

                paint.setTextSize(Chunk.BLOCK_SIZE * 1.2f);

                paint.setColor(Palette.FOREGROUND);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(""+(int) displayScore,screenWidth / 2 + Chunk.BLOCK_SIZE,Chunk.BLOCK_SIZE * 2.5f, paint);

                paint.setColor(Palette.MIDGROUND);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("/",screenWidth / 2,Chunk.BLOCK_SIZE * 2.5f, paint);

                paint.setColor(Palette.MIDGROUND);
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText("" + (int) highscore,screenWidth / 2 - Chunk.BLOCK_SIZE,Chunk.BLOCK_SIZE * 2.5f, paint);

                if (pauseDown)
                    paint.setColor(Palette.FOREGROUND);
                else
                    paint.setColor(Palette.MIDGROUND);

                canvas.drawRect(Chunk.BLOCK_SIZE * 0.5f, Chunk.BLOCK_SIZE * 0.5f,
                        Chunk.BLOCK_SIZE * .75f, Chunk.BLOCK_SIZE * 1.5f, paint);

                canvas.drawRect(Chunk.BLOCK_SIZE * 1f, Chunk.BLOCK_SIZE * 0.5f,
                        Chunk.BLOCK_SIZE * 1.25f, Chunk.BLOCK_SIZE * 1.5f, paint);

            }

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
