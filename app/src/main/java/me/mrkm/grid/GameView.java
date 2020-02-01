package me.mrkm.grid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private Paint paint;

    private Grid grid;
    private Toolbox toolbox;

    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    private boolean touch;
    private float touchX = 0f;
    private float touchY = 0f;

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        load();

        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    public void load() {
        paint = new Paint();

        grid = new Grid(screenWidth, screenHeight);
        toolbox = new Toolbox(screenWidth, screenHeight);
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
        // Check for Pause button

        // Check for Toolbox
            if (touch) {
                toolbox.touch(touchX, touchY);
            } else {
                toolbox.unTouch(touchX, touchY, grid);
            }
    }

    public void update() {

        // Update animations?
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);

            grid.draw(canvas, paint);

            toolbox.draw(canvas, paint, touchX, touchY);

//            paint.setColor(Color.LTGRAY);
//            canvas.drawRect(0,0,screenWidth,(screenHeight - screenWidth) / 2, paint);
            paint.setColor(Color.DKGRAY);
            paint.setTextSize(100);
            canvas.drawText("Score / " + ((int) grid.score),100,200, paint);

//            if (touch)
//                paint.setColor(Color.MAGENTA);
//            else
//                paint.setColor(Color.DKGRAY);
//
//            canvas.drawCircle(touchX, touchY, 20, paint);

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
