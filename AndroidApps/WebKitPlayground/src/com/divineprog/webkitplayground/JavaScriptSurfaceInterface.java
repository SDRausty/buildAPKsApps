package com.divineprog.webkitplayground;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.webkit.JavascriptInterface;

public class JavaScriptSurfaceInterface
{
    JavaScriptWebView mWebView;
    DrawingSurface mDrawingSurface;
    Paint mPaint;

    public JavaScriptSurfaceInterface(
        JavaScriptWebView webView,
        DrawingSurface surface)
    {
        mWebView = webView;
        mDrawingSurface = surface;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(false);
        mPaint.setColor(0xffffffff);
    }

    void callJS(final String js)
    {
        mWebView.callJS(js);
    }

    @JavascriptInterface
    public void setColor(int r, int g, int b)
    {
        int c = Color.argb(255, r, g, b);
        mPaint.setColor(c);
    }

    @JavascriptInterface
    public void fillRect(int x, int y, int w, int h)
    {
        Canvas canvas = mDrawingSurface.getCanvas();
        canvas.drawRect(x, y, x + w, y + h, mPaint);
    }

    @JavascriptInterface
    public void updateScreen()
    {
        mDrawingSurface.updateSurface();
    }
}
