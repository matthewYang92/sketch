package yang.sketch.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends SurfaceView implements SurfaceHolder.Callback {

    // SurfaceHolder实例  
    private SurfaceHolder mSurfaceHolder;  
    // Canvas对象
    private Bitmap cacheBitmap;
    private Canvas cacheCanvas;
    private Canvas mCanvas;

    private float mLastX, mLastY;

    private List<DrawingInfo> mDrawingList = new ArrayList<>();
    private List<DrawingInfo> mRemoveList = new ArrayList<>();

    private DrawMode curMode = DrawMode.DRAW;
    private Paint curPaint;
    private Paint curEraser;
    private DrawingInfo curDrawingInfo;

    private Thread drawThread;
  
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);  
        initView(); // 初始化
    }  
  
    private void initView() {
        curEraser = createDefaultEraser();
        curPaint = createDefaultPaint();
        curDrawingInfo = createDefaultDrawingInfo(curPaint, curEraser);
        mSurfaceHolder = getHolder();  
        mSurfaceHolder.addCallback(this);
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        // 设置可获得焦点  
        setFocusable(true);  
        setFocusableInTouchMode(true);  
        // 设置常亮  
        setKeepScreenOn(true);
    }

    private DrawingInfo createDefaultDrawingInfo(Paint paint, Paint eraser) {
        Path mPath = new Path();
        return new DrawingInfo(curMode, paint, eraser, mPath);
    }

    private Paint createDefaultPaint() {
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setFilterBitmap(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(20);
        mPaint.setColor(Color.BLACK);
        return mPaint;
    }

    private Paint createDefaultEraser() {
        Paint mEraser = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mEraser.setStyle(Paint.Style.STROKE);
        mEraser.setFilterBitmap(true);
        mEraser.setStrokeJoin(Paint.Join.ROUND);
        mEraser.setStrokeCap(Paint.Cap.ROUND);
        mEraser.setStrokeWidth(20);
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        return mEraser;
    }
  
    /* 
     * 创建 
     */  
    @Override  
    public void surfaceCreated(SurfaceHolder holder) {
        cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas(cacheBitmap);
    }  
  
    /* 
     * 改变 
     */  
    @Override  
    public void surfaceChanged(SurfaceHolder holder, int format, int width,  
            int height) {  
    }  
  
    /* 
     * 销毁 
     */  
    @Override  
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
  
    private void draw() {
        try {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (DrawingInfo drawingInfo : mDrawingList) {
                drawingInfo.draw(mCanvas);
                drawingInfo.draw(cacheCanvas);
            }
        } catch (Exception e) {  
            e.printStackTrace();
        } finally {  
            // 对画布内容进行提交  
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }  
    }

    private void startDrawThread() {
        if (drawThread == null) {
            drawThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        draw();
                    }
                }
            });
        }
        drawThread.start();
    }

    private void stopDrawThread() {
        drawThread.interrupt();
        drawThread = null;
    }
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();    //获取手指移动的x坐标
        float y = event.getY();    //获取手指移动的y坐标
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:
            mDrawingList.add(curDrawingInfo);
            mLastX = x;
            mLastY = y;
            curDrawingInfo.pathMoveTo(x, y);
            curDrawingInfo.addPoint(x, y);
            startDrawThread();
            break;  
  
        case MotionEvent.ACTION_MOVE:
            curDrawingInfo.pathQuadTo(mLastX, mLastY, (x + mLastX) / 2, (y + mLastY) / 2);
            mLastX = x;
            mLastY = y;
            break;  
  
        case MotionEvent.ACTION_UP:
            curPaint = new Paint(curPaint);
            curDrawingInfo = createDefaultDrawingInfo(curPaint, curEraser);
            stopDrawThread();
            break;  
        }  
        return true;  
    }

    public void setPaintStrokeWidth(float width) {
        curPaint.setStrokeWidth(width);
    }

    public void setEraserStrokeWidth(float width) {
        curEraser.setStrokeWidth(width);
    }

    public void setPaintColor(int color) {
        curPaint.setColor(color);
    }

    public void setMode(DrawMode mode) {
        curMode = mode;
        curDrawingInfo.setMode(mode);
    }

    public void undo() {
        int size = mDrawingList.size();
        if (size > 0) {
            DrawingInfo removeDrawInfo = mDrawingList.remove(size - 1);
            mRemoveList.add(removeDrawInfo);
            if (size == 1) {
                createDefaultDrawingInfo(curPaint, curEraser);
            }
        }
        draw();
    }

    public void redo() {
        int size = mRemoveList.size();
        if (size > 0) {
            DrawingInfo drawingInfo = mRemoveList.remove(size - 1);
            mDrawingList.add(drawingInfo);
        }
        draw();
    }

    public void clear() {
        createDefaultDrawingInfo(curPaint, curEraser);
        mRemoveList.clear();
        mDrawingList.clear();
        draw();
    }

    public Bitmap getBitmap() {
        return cacheBitmap;
    }
}  