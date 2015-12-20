package example.com.virtualpet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import example.com.virtualpet.Util.ResourceManager;
import example.com.virtualpet.Util.SpriteSheet;


/**
 * Created by reneb_000 on 3-12-2015.
 */
public class DogView extends SurfaceView implements SurfaceHolder.Callback {

    //private Bitmap test;
    private int x, y;
    private SurfaceHolder holder;
    private Paint paint = new Paint();
    private SpriteSheet test;


    public DogView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        holder = getHolder();
        holder.addCallback(this);
        //test = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        test = new SpriteSheet(ResourceManager.INSTANCE.testSheet, 36, false);
        x = y = 0;

    }

    public void onDraw(){
        if(holder.getSurface().isValid()){
            Canvas c = holder.lockCanvas();
            if(c!=null) {
                c.drawARGB(255, 0,0,0);
                //c.drawBitmap(test, x, y, paint);
                test.draw(c);
                holder.unlockCanvasAndPost(c);
            }
        }

    }

    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
        test.setXY(x, y);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
