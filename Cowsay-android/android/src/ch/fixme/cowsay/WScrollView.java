package ch.fixme.cowsay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

public class WScrollView extends HorizontalScrollView {
    public TextView sv;

    public WScrollView(Context context) {
        super(context);
    }

    public WScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        ret = ret | sv.onTouchEvent(event);
        return ret;
    }

}
