package com.sdesimeur.android.mapmockgps;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by sam on 18/08/15.
 */
public class CrossView extends ImageView {
    private double angle = 0;

    public CrossView(Context context) {
        super(context);
    }

    public CrossView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CrossView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAngle(double angle) {
        this.angle = angle;
        this.invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
         canvas.save();
         canvas.rotate((float) angle,getWidth()/2,getHeight()/2);
         super.onDraw(canvas);
         canvas.restore();

    }

}
