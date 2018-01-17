package com.divineprog.webkitplayground;

import android.util.Log;


public class JavaScriptInputListener implements Input.Listener
{
    JavaScriptWebView mWebView;

    public JavaScriptInputListener(
        JavaScriptWebView webView)
    {
        mWebView = webView;
    }

    void callJS(final String js)
    {
        mWebView.callJS(js);
    }

    @Override
    public void onTouchDown(int x, int y, int id)
    {
        callTouchFun("TouchDown", x, y, id);
    }

    @Override
    public void onTouchDrag(int x, int y, int id)
    {
        callTouchFun("TouchDrag", x, y, id);
    }

    @Override
    public void onTouchUp(int x, int y, int id)
    {
        callTouchFun("TouchUp", x, y, id);
    }

    @Override
    public void onKeyDown(int keyCode)
    {
        callKeyFun("KeyDown", keyCode);
    }

    @Override
    public void onKeyUp(int keyCode)
    {
        callKeyFun("KeyUp", keyCode);
    }

    void callTouchFun(String fun, int x, int y, int id)
    {
        callJS(fun + "(" + x + "," + y + "," + id + ")");
    }

    void callKeyFun(String fun, int keyCode)
    {
        callJS(fun + "(" + keyCode + ")");
    }
}
