package com.divineprog.webkitplayground;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Drawing surface with a backing bitmap.
 * @author miki
 */
public class DrawingSurface
	extends SurfaceView
	implements SurfaceHolder.Callback
{
	SurfaceHolder mSurfaceHolder;
	Context mContext;
	int mSurfaceWidth = 0;
	int mSurfaceHeight = 0;
	Bitmap mBitmap;
	Canvas mCanvas;
	Paint mBlitPaint;
    Listener mListener;
	volatile boolean mIsUpdating = false;

	public DrawingSurface(Context context)
	{
		super(context);

		mContext = context;

		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

		mBlitPaint = new Paint();
		mBlitPaint.setAntiAlias(false);
		mBlitPaint.setColor(0xffffffff);
		mBlitPaint.setAlpha(255);

		setVisibility(VISIBLE);
		setFocusableInTouchMode(true);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void surfaceChanged(
		SurfaceHolder holder,
		int format,
		int width,
		int height)
	{
		allocateScreenBuffer(width, height);
	}

	void allocateScreenBuffer(int width, int height)
	{
		// Surface must have a size.
		if ((0 == width) ||
			(0 == height))
		{
			return;
		}

		// Only create new surface if size has changed.
		if ((mSurfaceWidth == width) &&
			(mSurfaceHeight == height))
		{
			return;
		}

		mSurfaceWidth = width;
		mSurfaceHeight = height;

		mBitmap = Bitmap.createBitmap(
			mSurfaceWidth,
			mSurfaceHeight,
			Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);

		// Save clip rect.
		mCanvas.save();
		mCanvas.clipRect(0, 0, mSurfaceWidth, mSurfaceHeight, Region.Op.REPLACE);

		if (null != mListener)
		{
		    mListener.surfaceCreated(this);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		updateSurface();
	}

	@Override
	protected void onFocusChanged(
		boolean gainFocus,
		int direction,
		Rect previouslyFocusedRect)
	{
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
	    int action = event.getActionMasked();
	    if (mIsUpdating && action == MotionEvent.ACTION_MOVE)
	    {
	        // Consume MOVE events during updating.
	        return true;
	    }
	    else
	    {
	        return ((Activity) mContext).onTouchEvent(event);
	    }
	}

    public Canvas getCanvas()
    {
        return mCanvas;
    }

    public void setCanvasClipRect(int x, int y, int w, int h)
    {
        mCanvas.restore();
        mCanvas.save();
        mCanvas.clipRect(x, y, x + w, y + h, Region.Op.REPLACE);
    }

	void updateSurface()
	{
	    if (null == mBitmap) { return; }

		Canvas canvas = mSurfaceHolder.lockCanvas();
		if (null != canvas)
		{
		    mIsUpdating = true;
			canvas.drawBitmap(mBitmap, 0, 0, mBlitPaint);
			mSurfaceHolder.unlockCanvasAndPost(canvas);
            mIsUpdating = false;
		}
	}

	void setListener(Listener listener)
	{
	    mListener = listener;
	}

	/**
	 * Interface for notification of creation of a drawing surface.
	 * @author miki
	 */
	public interface Listener
	{
	    public void surfaceCreated(DrawingSurface surface);
	}
}
