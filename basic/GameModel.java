import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.undo.*;
import javax.vecmath.*;

import java.util.Random;
import javax.swing.undo.*;

public class GameModel extends Observable {
    public boolean isMouseDraging = false;
    public boolean isRectSelected = false;
    public boolean isPeakSelected = false;
    public RectShape rect = new RectShape(new Point2d(330,100),new Point2d(370,110));
    public PolyShape peaks = new PolyShape(700,200,20); // width, height, peaks
    
    public boolean isGameOver = false;
    public boolean isShipCrashed = false;
    public boolean isShipLanding = false;
    
    // Undo manager
    private UndoManager undoManager;
    
    public GameModel(int G_fps, int G_width, int G_height, int G_peaks) {
        
        ship = new Ship(60, G_width/2, 50);
        
        worldBounds = new Rectangle2D.Double(0, 0, G_width, G_height);
        
        undoManager = new UndoManager();
        
        // anonymous class to monitor ship updates
        ship.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                setChangedAndNotify();
            }
        });
    }
    
    // EditView methods
    // set new point for landing pad
    public boolean isRectSelect(double x, double y) {
        return rect.hittest(x, y);
    }
    public void setRectSelect(boolean _isSelect) {
        this.rect.isSelected = _isSelect;
        setChangedAndNotify();
    }
    public void setRectBasePoint(double x, double y) {
        rect.setBasePoint(x,y);
        setChangedAndNotify();
    }
    public void setRectMovePoint(double x, double y) {
        rect.setMovePoint(x,y);
        setChangedAndNotify();
    }
    public void setRectClickedPoint(double x, double y) {
        rect.setClickedPoint(x, y);
        setChangedAndNotify();
    }
    public void setRectUndoPoint() {
        rect.setUndoPoint();
    }
    // set(modify) points for selecting peaks
    public boolean isPeakSelect(double x, double y) {
        return peaks.hittest(x,y);
    }
    public void setPeakSelect(boolean _isSelect) {
        if(!_isSelect) {
            peaks.selectedIndex = -1;
        }
        setChangedAndNotify();
    }
    public void setPeakBasePoint(double y) {
        peaks.setBasePoint(y);
        setChangedAndNotify();
    }
    public void setPeakMovePoint(double y) {
        peaks.setMovePoint(y);
        setChangedAndNotify();
    }
    public void setPeakUndoPoint() {
        peaks.setUndoPoint();
    }
    public void setRectAndPeakUndoPoint() {
        rect.setUndoPoint();
        peaks.setUndoPoint();
    }
    public void setRectAndPeaks(Point2d RectPos, Point2d PeakPos, int PeakIndex) {
        rect.topleftPoint = RectPos;
        if(PeakIndex != -1) {
            peaks.points.set(PeakIndex, PeakPos);
            peaks.pointsChanged = true;
        }
    }
    // World
    // - - - - - - - - - - -
    public final Rectangle2D getWorldBounds() {
        return worldBounds;
    }
    
    Rectangle2D.Double worldBounds;
    
    // Ship
    // - - - - - - - - - - -
    
    public Ship ship;
    public void checkShipCrashed() {
        this.isShipCrashed =  (this.peaks.peaksPoly.intersects(this.ship.getShape()) || !this.worldBounds.contains(this.ship.getShape()));
    }
    
    public void checkShipLanding() {
        Rectangle2D.Double landingPadRect = new Rectangle2D.Double(this.rect.topleftPoint.x,this.rect.topleftPoint.y,40,10);
        this.isShipLanding =  landingPadRect.intersects(this.ship.getShape());
        if(this.isShipLanding) {
            this.isShipCrashed = (this.ship.getSpeed() >= this.ship.getSafeLandingSpeed());
        }
    }
    
    public boolean getIsShipSpeedSafe() {
//        System.out.println("ship Speed: " + this.ship.getSpeed());
//        System.out.println("Safe Speed: " + this.ship.getSafeLandingSpeed());
        return (this.ship.getSpeed() <  this.ship.getSafeLandingSpeed());
    }
    
    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }
    
    // Observerable
    // - - - - - - - - - - -
    
    // helper function to do both
    void setChangedAndNotify() {
        setChanged();
        notifyObservers();
    }
    
    // undo & redo method
    public void setPos(double RectX, double RectY, double PeakX, double PeakY, int PeakUndoIndex) {
        
        // create undoable edit
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
            
            // capture variables for closure
            final Point2d oldRectPos = new Point2d(rect.undoTopleftPoint.x, rect.undoTopleftPoint.y);
            final Point2d newRectPos = new Point2d(RectX, RectY);
            final Point2d oldPeakPos = new Point2d(peaks.undoPoint.x, peaks.undoPoint.y);
            final Point2d newPeakPos = new Point2d(PeakX, PeakY);
            final int oldIndex = peaks.undoIndex;
            final int newIndex = PeakUndoIndex;
            
            // Method that is called when we must redo the undone action
            public void redo() throws CannotRedoException {
                super.redo();
                setRectAndPeaks(newRectPos, newPeakPos, newIndex);
                setChangedAndNotify();
            }
            
            public void undo() throws CannotUndoException {
                super.undo();
                setRectAndPeaks(oldRectPos, oldPeakPos, oldIndex);
                setChangedAndNotify();
            }
        };
        
        // Add this undoable edit to the undo manager
        undoManager.addEdit(undoableEdit);
        
        // finally, set the value and notify views
        rect.topleftPoint = new Point2d(RectX,RectY);
        peaks.points.set(PeakUndoIndex, new Point2d(PeakX, PeakY));
        setChangedAndNotify();
    }
    
    // undo and redo methods
    // - - - - - - - - - - - - - -
    
    public void undo() {
        if (canUndo())
            undoManager.undo();
    }
    
    public void redo() {
        if (canRedo())
            undoManager.redo();
    }
    
    public boolean canUndo() {
        return undoManager.canUndo();
    }
    
    public boolean canRedo() {
        return undoManager.canRedo();
    }
    
}

class RectShape {
    Point2d topleftPoint;
    Point2d undoTopleftPoint = new Point2d();
    Point2d bottomrightPoint;
    Point2d movePoint = new Point2d();
    Point2d basePoint = new Point2d(350,105);
    Boolean isSelected = false;
    
    public RectShape(Point2d topleft, Point2d buttomright) {
        this.topleftPoint = topleft;
        this.bottomrightPoint = buttomright;
    }
    
    public Point2d getRectPos() {
        return topleftPoint;
    }
    public void setUndoPoint() {
        undoTopleftPoint = new Point2d(topleftPoint.x, topleftPoint.y);
    }
    
    public void setBasePoint(double x, double y) {
        basePoint.x = x;
        basePoint.y = y;
    }
    
    public void setMovePoint(double x, double y) {
        movePoint.x = x - basePoint.x;
        movePoint.y = y - basePoint.y;
        basePoint.x = x;
        basePoint.y = y;
        topleftPoint.x += movePoint.x;
        topleftPoint.y += movePoint.y;
        
        // outside of world bound
        if(	(topleftPoint.x < 0) || (topleftPoint.x > 660) ||
           (topleftPoint.y < 0) || (topleftPoint.y > 190)) {
            topleftPoint.x -= movePoint.x;
            topleftPoint.y -= movePoint.y;
        }
    }
    
    public void setClickedPoint(double x, double y) {
        if((x-20 >= 0) && (x-20 <= 660) && (y-5 >= 0) && (y-5 <= 190)) {
            topleftPoint.x = x - 20;
            topleftPoint.y = y - 5;
        }
    }
    
    public void draw(Graphics2D g2) {
        g2.setColor(Color.RED);
        g2.fillRect((int)topleftPoint.x, (int)topleftPoint.y, 40, 10); // may change here for a cleaner code
        //  	g.fillRect(RECT_X, RECT_Y, RECT_WIDTH, RECT_HEIGHT);
        if(isSelected) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2 / 1.0f));
            g2.drawRect((int)topleftPoint.x, (int)topleftPoint.y, 40, 10);
        }
    }
    
    public void PlayViewdraw(Graphics2D g2) {
        g2.setColor(Color.RED);
        g2.fillRect((int)topleftPoint.x, (int)topleftPoint.y, 40, 10); // may change here for a cleaner code
        //  	g.fillRect(RECT_X, RECT_Y, RECT_WIDTH, RECT_HEIGHT);
    }
    
    public boolean hittest(double x, double y) {
        if ((x >= this.topleftPoint.x) && (y >= this.topleftPoint.y) &&
            (x <= this.topleftPoint.x + 40) && (y <= this.topleftPoint.y + 10)) {
            this.isSelected = true;
        }
        else {
            this.isSelected = false;
        }
        return isSelected;
    }
}

class PolyShape {
    ArrayList<Point2d> points;
    Point2d undoPoint = new Point2d();
    int undoIndex = -1;
    private int[] xpoints, ypoints;
    private int npoints = 0;
    private int numOfPeaks = 0;
    private int radius = 15;
    public int selectedIndex = -1;
    Boolean pointsChanged = false; // dirty bit
    
    double basePointY = 0;
    double movePointY = 0;
    
    Polygon peaksPoly = new Polygon();
    
    public int getUndoIndex() {
        return undoIndex;
    }
    
    public Point2d getCurPeakPos(){
        if(undoIndex != -1) {
            return new Point2d(points.get(undoIndex).x, points.get(undoIndex).y);
        }
        else {
            //hardcode to index 0
            undoIndex = 0;
            return new Point2d(points.get(0).x, points.get(0).y);
        }
    }
    
    public void setUndoPoint() {
        // selectedIndex was set from hittest
        undoIndex = selectedIndex;
        //		System.out.println("select index: " + selectedIndex);
        if(undoIndex == -1) {
            //hardcode undoPoint to value in index 0
            undoPoint.x = points.get(0).x;
            undoPoint.y = points.get(0).y;
            return;
        }
        undoPoint.x = points.get(undoIndex).x;
        undoPoint.y = points.get(undoIndex).y;
        //		System.out.println("peaks undo point: " + undoPoint.y);
    }
    
    // change y points
    public void setBasePoint(double y) {
        basePointY = y;
    }
    public void setMovePoint(double y) {
        movePointY = y - basePointY;
        basePointY = y;
        // selectedIndex was set from hittest
        if(selectedIndex == -1) return;
        points.get(selectedIndex).y += movePointY;
        // outside of world y bound
        if((points.get(selectedIndex).y < 0) || (points.get(selectedIndex).y > 200)) {
            points.get(selectedIndex).y -= movePointY;
        }
        pointsChanged = true;
    }
    
    //default points(randomly generated)
    public PolyShape(double width, double height, int peaks) {
        this.numOfPeaks = peaks;
        Random rand = new Random();
        double heightOfPeak; // not need this, delete later
        double widthOfPeak = width / (peaks - 1);
        double X_Peak;
        for(int i = 0; i < numOfPeaks; i++) {
            rand = new Random();
            //rangeMin + (rangeMax - rangeMin) * r.nextDouble()
            heightOfPeak = (height / 2) + (height / 2) * rand.nextDouble();
            X_Peak = widthOfPeak * i;
            addPoint(X_Peak,heightOfPeak);
        }
        addPoint(width,height);
        addPoint(0,height);
    }
    
    private void clearPoints() {
        points = new ArrayList<Point2d>();
        pointsChanged = true;
    }
    
    // add a point to end of shape
    private void addPoint(Point2d p) {
        if (points == null) clearPoints();
        points.add(p);
        pointsChanged = true;
    }
    // add a point to end of shape
    private void addPoint(double x, double y) {
        addPoint(new Point2d(x, y));
    }
    
    // quick hack, get and set would be better
    void cachePointsArray() {
        xpoints = new int[points.size()];
        ypoints = new int[points.size()];
        for (int i=0; i < points.size(); i++) {
            xpoints[i] = (int)points.get(i).x;
            ypoints[i] = (int)points.get(i).y;
        }
        npoints = points.size();
        peaksPoly = new Polygon(xpoints, ypoints, npoints);
        pointsChanged = false;
    }
    
    public void draw(Graphics2D g2) {
        // don't draw if points are empty (not shape)
        if(points == null) return;
        
        // see if we need to update the cache
        if (pointsChanged) cachePointsArray();
        
        g2.setColor(Color.DARK_GRAY);
        //    		System.out.println("xpoints: ");
        //    		for(int i = 0 ; i < 22; i++) {
        //    			System.out.println(xpoints[i]);
        //    		}
        //    		System.out.println("ypoints: " + ypoints);
        //    		for(int i = 0 ; i < 22; i++) {
        //    			System.out.println(ypoints[i]);
        //    		}
        //    		System.out.println("npoints: " + npoints);
        g2.fillPolygon(xpoints, ypoints, npoints);
        g2.setColor(Color.GRAY);
        for(int i = 0; i < numOfPeaks; i++) {
            g2.drawOval(xpoints[i]-radius, ypoints[i]-radius, radius * 2, radius * 2);
        }
        if(this.selectedIndex != -1) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2 / 1.0f));
            g2.drawOval(xpoints[selectedIndex]-radius, ypoints[selectedIndex]-radius, radius * 2, radius * 2);
        }
    }
    
    public void PlayViewdraw(Graphics2D g2) {
        // don't draw if points are empty (not shape)
        if(points == null) return;
        
        // see if we need to update the cache
        if (pointsChanged) cachePointsArray();
        g2.setColor(Color.DARK_GRAY);
        g2.fillPolygon(xpoints, ypoints, npoints);
        
    }
    
    public boolean hittest(double x, double y) {
        for(int i = 0; i < this.numOfPeaks; i++) {
            if( (x >= (xpoints[i]-radius)) && (x <= (xpoints[i]+radius)) &&
               (y >= (ypoints[i]-radius)) && (y <= (ypoints[i]+radius))) {
                selectedIndex = i;
                return true;
            }
        }
        return false;
    }
    
}



