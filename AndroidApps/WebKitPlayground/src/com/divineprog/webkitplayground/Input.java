package com.divineprog.webkitplayground;

public class Input
{
    public interface Listener
    {
        public void onTouchDown(int touchX, int touchY, int touchId);
        public void onTouchDrag(int touchX, int touchY, int touchId);
        public void onTouchUp(int touchX, int touchY, int touchId);
        public void onKeyDown(int keyCode);
        public void onKeyUp(int keyCode);
    }

    public static class Adapter implements Listener
    {
        @Override
        public void onTouchDown(int touchX, int touchY, int touchId) {}
        @Override
        public void onTouchDrag(int touchX, int touchY, int touchId) {}
        @Override
        public void onTouchUp(int touchX, int touchY, int touchId) {}
        @Override
        public void onKeyDown(int keyCode) {}
        @Override
        public void onKeyUp(int keyCode) {}
    }
}
