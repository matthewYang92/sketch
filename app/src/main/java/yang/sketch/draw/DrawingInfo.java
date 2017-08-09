package yang.sketch.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;


class DrawingInfo {

    private DrawMode mode;
    private Paint eraser;
    private Paint paint;
    private Path path;
    private float x = -1, y = -1;

    DrawingInfo(DrawMode mode, Paint paint, Paint eraser, Path path) {
        this.paint = paint;
        this.eraser = eraser;
        this.path = path;
        this.mode = mode;
    }

    void reset() {
        path.reset();
        x = -1;
        y = -1;
    }

    void draw(Canvas canvas) {
        Paint tmp = mode == DrawMode.DRAW ? paint : eraser;
        canvas.drawPath(path, tmp);
        if (x >= 0 && y >= 0) {
            canvas.drawPoint(x, y, tmp);
        }
    }

    void pathQuadTo(float x1, float y1, float x2, float y2) {
        path.quadTo(x1, y1, x2, y2);
    }

    void pathMoveTo(float x, float y) {
        path.moveTo(x, y);
    }

    void addPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    void setMode(DrawMode mode) {
        this.mode = mode;
    }
}