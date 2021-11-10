package com.ray3k.liftoff.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class CurvedLine extends Actor {
    private static final int CIRCLE_RADIUS = 10;
    private static final Vector2 tmpVector = new Vector2();
    private static final Vector2 tmpPoint1 = new Vector2();
    private static final Vector2 tmpPoint2 = new Vector2();
    
    private final Vector2 from, to, center1, center2, minPoint, maxPoint;
    private Array<Vector2> points;
    
    private boolean animate = false;
    
    private final ShapeDrawer shapeDrawer;
    private float lineWidth = 4;
    private int curveResolution = 50;
    private float timePassed = 0;
    private int lastFrame;
    private float alpha = 0;
    private float fps = 40;
    
    public CurvedLine (ShapeDrawer shapeDrawer) {
        this(shapeDrawer, 0, 0, 0, 0);
    }
    
    public CurvedLine (ShapeDrawer shapeDrawer, float fromX, float fromY, float toX, float toY) {
        from = new Vector2(fromX, fromY);
        to = new Vector2(toX, toY);
        
        center1 = new Vector2();
        center2 = new Vector2();
        
        minPoint = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
        maxPoint = new Vector2(0, 0);
        
        this.shapeDrawer = shapeDrawer;
        
        points = new Array<>(true, curveResolution + 2, Vector2.class);
        for (int i = 0; i < curveResolution + 2; i++) {
            points.add(new Vector2());
        }
        
        calculatePoints();
        setColor(Color.WHITE);
        getColor().a = 0.5f;
    }
    
    @Override
    public void draw (Batch batch, float parentAlpha) {
        float oldAlpha = getColor().a;
        getColor().a *= parentAlpha;
        shapeDrawer.setColor(getColor());
        shapeDrawer.path(points, lineWidth, JoinType.NONE,true);
        getColor().a = oldAlpha;
        
        if (animate) {
            timePassed += Gdx.graphics.getDeltaTime() * fps;
            int frame = (MathUtils.floor(timePassed)) % (curveResolution + 2);
            int nextFrame = frame >= (curveResolution + 1) ? frame : frame + 1;
            Vector2 point = points.get(frame);
            Vector2 point2 = points.get(nextFrame);
            
            if (lastFrame != frame) {
                lastFrame = frame;
                alpha = 0;
            } else {
                alpha += 1f / (Gdx.graphics.getFramesPerSecond() / fps);
            }
            
            float x = Interpolation.smooth2.apply(point.x, point2.x, alpha);
            float y = Interpolation.smooth2.apply(point.y, point2.y, alpha);
            shapeDrawer.filledCircle(x, y, 5);
        } else {
            timePassed = 0;
        }
    }
    
    private void calculatePoints() {
        float defaultOffset = 200;
        
        float xDistance = to.x - from.x;
        float horizontalOffset = Math.min(defaultOffset, Math.abs(xDistance));
        float verticalOffset = 0;
        float ratioX = 0.5f;
        
        if (xDistance <= 0) {
            float yDistance = to.y - from.y;
            float vector = yDistance < 0 ? -1.0f : 1.0f;
            verticalOffset = Math.min(defaultOffset, Math.abs(yDistance)) * vector;
            ratioX = 1.0f;
        }
        
        horizontalOffset *= ratioX;
        center1.set(from.x + horizontalOffset, from.y + verticalOffset);
        center2.set(to.x - horizontalOffset, to.y - verticalOffset);
        
        CurvedLine.getCurvedLine(from, to, center1, center2, points.size - 2);
        
        minPoint.set(Float.MAX_VALUE, Float.MAX_VALUE);
        maxPoint.set(0, 0);
        for (Vector2 point : points) {
            if (point.x < minPoint.x)
                minPoint.x = point.x;
            if (point.y < minPoint.y)
                minPoint.y = point.y;
            if (point.x > maxPoint.x)
                maxPoint.x = point.x;
            if (point.y > maxPoint.y)
                maxPoint.y = point.y;
        }
        
        setSize(maxPoint.x - minPoint.x, maxPoint.y - minPoint.y + lineWidth);
        setPosition(minPoint.x, minPoint.y - lineWidth * 0.5f);
    }
    
    public void setTo(float x, float y) {
        if (to.x == x && to.y == y)
            return;
        to.set(x - 4, y);
        calculatePoints();
    }
    
    public void setFrom(float x, float y) {
        if (from.x == x && from.y == y)
            return;
        from.set(x, y);
        calculatePoints();
    }
    
    public void setLineWidth (float lineWidth) {
        this.lineWidth = lineWidth;
    }
    
    public void setCurveResolution (int curveResolution) {
        this.curveResolution = curveResolution;
        
        points = new Array<>(true, curveResolution + 2, Vector2.class);
        for (int i = 0; i < curveResolution + 2; i++) {
            points.add(new Vector2());
        }
    }
    
    public void setAnimate (boolean animate) {
        this.animate = animate;
    }
    
    public void setFps (float fps) {
        this.fps = fps;
    }
    
    @Override
    public Actor hit (float x, float y, boolean touchable) {
        if (super.hit(x, y, touchable) == null)
            return null;
        
        tmpVector.set(x, y);
        
        float circleSqr = CIRCLE_RADIUS * CIRCLE_RADIUS;
        for (int i = 0; i < points.size; i+=2) {
            stageToLocalCoordinates(tmpPoint1.set(points.get(i)));
            stageToLocalCoordinates(tmpPoint2.set(points.get(i + 1)));
            
            if (Intersector.intersectSegmentCircle(tmpPoint1, tmpPoint2, tmpVector, circleSqr))
                return this;
        }
        
        return null;
    }
    
    private static final Array<Vector2> tmpResult = new Array<>();
    public static Pool<Vector2> vector2Pool = new Vector2Pool(60);
    public static Array<Vector2> getCurvedLine(Vector2 from, Vector2 to, Vector2 center1, Vector2 center2, int segments) {
        tmpResult.clear();
        
        float subdiv_step = 1f / segments;
        float subdiv_step2 = subdiv_step * subdiv_step;
        float subdiv_step3 = subdiv_step * subdiv_step * subdiv_step;
        
        float pre1 = 3 * subdiv_step;
        float pre2 = 3 * subdiv_step2;
        float pre4 = 6 * subdiv_step2;
        float pre5 = 6 * subdiv_step3;
        
        float tmp1x = from.x - center1.x * 2 + center2.x;
        float tmp1y = from.y - center1.y * 2 + center2.y;
        
        float tmp2x = (center1.x - center2.x) * 3 - from.x + to.x;
        float tmp2y = (center1.y - center2.y) * 3 - from.y + to.y;
        
        float fx = from.x;
        float fy = from.y;
        
        float dfx = (center1.x - from.x) * pre1 + tmp1x * pre2 + tmp2x * subdiv_step3;
        float dfy = (center1.y - from.y) * pre1 + tmp1y * pre2 + tmp2y * subdiv_step3;
        
        float ddfx = tmp1x * pre4 + tmp2x * pre5;
        float ddfy = tmp1y * pre4 + tmp2y * pre5;
        
        float dddfx = tmp2x * pre5;
        float dddfy = tmp2y * pre5;
        
        while (segments-- > 0) {
            tmpResult.add(vector2Pool.obtain().set(fx, fy));
            fx += dfx;
            fy += dfy;
            dfx += ddfx;
            dfy += ddfy;
            ddfx += dddfx;
            ddfy += dddfy;
        }
        tmpResult.add(vector2Pool.obtain().set(fx, fy));
        tmpResult.add(vector2Pool.obtain().set(to.x, to.y));
        
        return tmpResult;
    }
}
