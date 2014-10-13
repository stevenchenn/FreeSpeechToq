package steven.example.com.fsmtoqapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingView extends View {
    // Paint
    private Paint drawPaint;
    // Holds draw calls
    private Canvas drawCanvas;
    // Bitmap to hold pixels
    private Bitmap canvasBitmap;
    private Path mPath;
    private static int originalColor = Color.RED;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        int paintColor = Color.RED;
        mPath = new Path();
        // Setup the drawing area for user interaction
        drawPaint = new Paint();
        // Change the color of paint to use
        drawPaint.setColor(paintColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(10.0f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(canvasBitmap, 0, 0, drawPaint);
        canvas.drawPath(mPath, drawPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Make a new bitmap that stores each pixel on 4 bytes
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    //guideline from stackoverflow on drawing
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

//            circlePath.reset();
//            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }
    private void touch_up() {
        mPath.lineTo(mX, mY);
//        circlePath.reset();
        // commit the path to our offscreen
        drawCanvas.drawPath(mPath,  drawPaint);
        // kill this so we don't double draw
        mPath.reset();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                break;
        }
        invalidate();
        return true;
    }

    public int getColor(){
        return drawPaint.getColor();
    }

    public void setColor(int color){
        if(color == Color.WHITE) {
            originalColor = drawPaint.getColor();
        }
        drawPaint.setColor(color);
    }

    public void setStrokeWidth(float width){
        drawPaint.setStrokeWidth(width);
    }

    public int lastColor(){
        return originalColor;
    }

    public boolean save(){
        FileOutputStream fos;
        try {
            fos = new FileOutputStream("/mnt/sdcard/fsm.png");
            canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
