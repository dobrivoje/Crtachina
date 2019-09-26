package org.superbapps.android.crtachina;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.superbapps.android.crtachina.helpers.PalleteTools;

import static java.lang.Math.PI;

public class DrawingView extends View {
	private static final String TAG = "DrawingView";

	Context context;

	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Path   mPath;
	private Paint  mBitmapPaint;

	private Paint mPaint;
	private float startX, startY;
	private float endX, endY;
	private static final float TOUCH_TOLERANCE = 4;

	// pointer = a shape (little circle) which we draw with
	private Paint pointerPaint;
	private Path  pointerPath;

	private PalleteTools drawingShape;

	//<editor-fold desc="initialization">
	public DrawingView(Context ctx) {
		super( ctx );
		context = ctx;

		drawingShape = PalleteTools.ARROW;
		initGUI();
	}

	private void initGUI() {
		mPath = new Path();
		mBitmapPaint = new Paint( Paint.DITHER_FLAG );

		mPaint = new Paint();
		mPaint.setAlpha( 255 );
		mPaint.setAntiAlias( true );
		mPaint.setDither( true );
		mPaint.setColor( Color.RED );
		mPaint.setStyle( Paint.Style.STROKE );
		mPaint.setStrokeJoin( Paint.Join.ROUND );
		mPaint.setStrokeCap( Paint.Cap.ROUND );
		mPaint.setStrokeWidth( 12 );

		configureCirclePointer();
	}

	private void configureCirclePointer() {
		pointerPaint = new Paint();
		pointerPath = new Path();
		pointerPaint.setAntiAlias( true );
		pointerPaint.setColor( Color.GRAY );
		pointerPaint.setStyle( Paint.Style.STROKE );
		pointerPaint.setStrokeJoin( Paint.Join.MITER );
		pointerPaint.setStrokeWidth( 4f );
	}
	//</editor-fold>

	//<editor-fold desc="system overrides">
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged( w, h, oldw, oldh );

		mBitmap = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888 );
		mCanvas = new Canvas( mBitmap );
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw( canvas );

		canvas.drawPath( pointerPath, pointerPaint );
		canvas.drawPath( mPath, mPaint );
		canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint );
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchStart( x, y );
			break;
		case MotionEvent.ACTION_MOVE:
			touchMove( x, y );
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
		mPath.moveTo( x, y );
		endX = startX = x;
		endY = startY = y;
	}

	private void touchMove(float x, float y) {
		float dx = Math.abs( x - endX );
		float dy = Math.abs( y - endY );
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			// actual drawing
			drawingToolSelector( x, y );
			// end of actual drawing

			endX = x;
			endY = y;
			pointerPath.reset();
			pointerPath.addCircle( x, y, 15, Path.Direction.CCW );
		}
	}

	private void touchUp() {
		// actual drawing
		drawingToolSelector( endX, endY );
		// end of actual drawing

		mPaint.setAlpha( 255 );

		pointerPath.reset();
		mCanvas.drawPath( mPath, mPaint );
		mPath.reset();
	}
	//</editor-fold>

	private void drawingToolSelector(float x, float y) {
		mPaint.setAlpha( 60 );

		switch (drawingShape) {
		case FREE_HAND:
			mPath.quadTo( endX, endY, x, y );
			break;
		case LINE:
			mPath.reset();
			mPath.moveTo( startX, startY );
			mPath.lineTo( x, y );
			break;
		case CIRCLE:
			mPath.reset();
			mPath.moveTo( startX, startY );
			mPath.addCircle( startX, startY, radius(), Path.Direction.CW );
			break;
		case RECTANGLE:
			mPath.reset();
			mPath.addRect( startX, startY, endX, endY, Path.Direction.CW );
			break;
		case ARROW:
			mPath.reset();
			mPath.moveTo( startX, startY );
			mPath.lineTo( endX, endY );

			float L2 = radius() < 50 ? radius() : 50;

			float userDefinedArrowAngle = (float) (PI / 3);
			float arrowAngle = (float) (PI / 2 - userDefinedArrowAngle);

			double thetha = Math.atan( (endY - startY) / (endX - startX) );
			Log.i( TAG, "ugao : " + thetha * 180 / PI );

			float ugao;
			if (thetha > -PI / 2 && thetha < +PI / 2) {
				thetha = PI + thetha;
				ugao = (float) (arrowAngle - thetha);
			} else {
				ugao = (float) (arrowAngle + thetha);
				Log.i( TAG, "ugao2 : " + ugao );
			}

			float x4 = (float) (endX + L2 * Math.cos( ugao ));
			float y4 = (float) (endY - L2 * Math.sin( ugao ));
			mPath.lineTo( x4, y4 );

			break;
		}
	}

	private float radius() {
		float deltaX = Math.abs( endX - startX );
		float deltaY = Math.abs( endY - startY );
		float radius = (float) Math.sqrt( deltaX * deltaX + deltaY * deltaY );
		return radius;
	}
}



