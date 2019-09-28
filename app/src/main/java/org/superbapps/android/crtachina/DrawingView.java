package org.superbapps.android.crtachina;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import org.superbapps.android.crtachina.helpers.PalleteTools;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class DrawingView extends View {

    private static final String TAG = "DrawingView";

    Context context;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;

    private Paint mPaint;
    private float X1, Y1;
    private float X2, Y2;
    private static final float TOUCH_PIXELS_TOLERANCE = 4;

    // pointer = a shape (little circle) which we draw with
    private Paint pointerPaint;
    private Path pointerPath;

    private PalleteTools drawingShape;

    //<editor-fold desc="initialization">
    public DrawingView(Context ctx) {
        super(ctx);
        context = ctx;

        drawingShape = PalleteTools.ARROW;
        initGUI();
    }

    private void initGUI() {
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mPaint = new Paint();
        mPaint.setAlpha(255);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(33);

        configureCirclePointer();
    }

    private void configureCirclePointer() {
        pointerPaint = new Paint();
        pointerPath = new Path();
        pointerPaint.setAntiAlias(true);
        pointerPaint.setColor(Color.GRAY);
        pointerPaint.setStyle(Paint.Style.STROKE);
        pointerPaint.setStrokeJoin(Paint.Join.MITER);
        pointerPaint.setStrokeWidth(4f);
    }
    //</editor-fold>

    //<editor-fold desc="system overrides">
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(pointerPath, pointerPaint);
        canvas.drawPath(mPath, mPaint);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }

        invalidate();
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="move handler">
    private void touchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        X2 = X1 = x;
        Y2 = Y1 = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - X2);
        float dy = Math.abs(y - Y2);
        if (dx >= TOUCH_PIXELS_TOLERANCE || dy >= TOUCH_PIXELS_TOLERANCE) {
            mPaint.setAlpha(60);

            // actual drawing
            drawingToolSelector(x, y);
            // end of actual drawing

            // remember last position
            X2 = x;
            Y2 = y;
            // ...and make an illusion of circle pointer moving
            pointerPath.reset();
            pointerPath.addCircle(x, y, 15, Path.Direction.CCW);
        }
    }

    private void touchUp() {
        drawingToolSelector(X2, Y2);

        mPaint.setAlpha(255);

        pointerPath.reset();
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();
    }
    //</editor-fold>

    private void drawingToolSelector(float x, float y) {
        float dy = Y2 - Y1;
        float dx = X2 - X1;

        switch (drawingShape) {
            case FREE_HAND:
                mPath.quadTo(X2, Y2, x, y);
                break;
            case LINE:
                mPath.reset();
                mPath.moveTo(X1, Y1);
                mPath.lineTo(x, y);
                break;
            case CIRCLE:
                mPath.reset();
                mPath.moveTo(X1, Y1);
                mPath.addCircle(X1, Y1, radius(), Path.Direction.CW);
                break;
            case RECTANGLE:
                mPath.reset();
                mPath.addRect(X1, Y1, X2, Y2, Path.Direction.CW);
                break;
            case ARROW:
                mPath.reset();
                mPath.moveTo(X1, Y1);
                mPath.lineTo(X2, Y2);

                float L = radius();

                // length og the arrow side lines :
                float L3 = L < 90 ? L : 90;

                // userDefinedArrowAngle :
                float alpha = (float) (PI / 6);
                // controls sharpness of the arrow
                // the less, the sharper
                float angleFactor = 0.5f;

                // dinamicaly adapt an agle once it reaches PI radians:
                double thetha = atan(dy / dx);
                thetha += dx < 0 ? PI : 0;

                // pol coord system :
                double polAngle = angleFactor * atan(L3 / (L - L3 * cos(alpha)));
                double polRadius = (L - L3 * cos(alpha)) / cos(polAngle);

                float x3 = (float) (X1 + (polRadius * cos(thetha - polAngle)));
                float y3 = (float) (Y1 + (polRadius * sin(thetha - polAngle)));

                float x4 = (float) (X1 + (polRadius * cos(thetha + polAngle)));
                float y4 = (float) (Y1 + (polRadius * sin(thetha + polAngle)));

                mPath.lineTo(x3, y3);
                mPath.lineTo(X2, Y2);
                mPath.lineTo(x4, y4);
                break;

            default:
                mPath.reset();
                mPath.moveTo(X1, Y1);

                float r1 = radius();
                double teta = atan(dy / dx);
                teta += dx < 0 ? PI : 0;

                float xx = (float) (X1 + r1 * cos(teta));
                float yy = (float) (Y1 + r1 * sin(teta));
                mPath.lineTo(xx, yy);
        }
    }

    private float radius() {
        return (float) sqrt(pow(X2 - X1, 2) + pow(Y2 - Y1, 2));
    }
}



