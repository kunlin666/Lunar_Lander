import javax.swing.*;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Observable;

class Ship extends Observable {
    public boolean isShipCrashed = false;
    public boolean isShipLanding = false;
    
    
    // FPS and initial position for ship to appear
    public Ship(int fps, int x, int y) {
        
        startPosition = new Point2d(x, y);
        
        // set the dynamics based on 60 fps
        double scale = 60.0 / fps;
        thrust = 0.25 * scale;
        gravity = 0.01 * scale;
        decay = 0.005 * scale;
        
        // the ship has it's own animation timer
        timer = new Timer((int)(1000/fps), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                update();
                setChangedAndNotify();
            }
        });
        
        reset(startPosition);
    }
    
    // animation timer
    Timer timer;
    
    // get the ship shape model
    public final Rectangle2D.Double getShape() {
        return new Rectangle2D.Double(position.x, position.y, 10, 10);
    }
    
    // resets the ship state to a start position and full tank
    public void reset(Point2d position) {
        this.position = (Point2d)position.clone();
        velocity  = new Vector2d(0, 0);
        fuel = 50;
        setPaused(true);
        setChangedAndNotify();
    }
    
    // stop the ship
    public void stop() {
        velocity  = new Vector2d(0, 0);
        setPaused(true);
        setChangedAndNotify();
    }
    
    public boolean isPaused() {
        return !timer.isRunning();
    }
    
    public void setPaused(boolean paused) {
        if (paused)
            timer.stop();
        else
            timer.start();
        setChangedAndNotify();
    }
    
    public Point2d getPosition() {
        return position;
    }
    
    // where to put ship at start of game
    Point2d startPosition;
    
    // current postion of ship
    Point2d position;
    
    // safe landing speed
    public double getSafeLandingSpeed() { return thrust; }
    
    // direction and magnitude of ship movement
    public Vector2d getVelocity() {
        return velocity;
    }
    
    // magnitude of the ship's movement
    public double getSpeed() {
        return velocity.length();
    }
    
    Vector2d velocity = new Vector2d(0, 0);
    
    // track how much fuel was used
    public double getFuel() {
        return fuel;
    }
    public void setFuel(int fuel) { this.fuel = fuel; }
    double fuel;
    
    // movement dynamics
    double thrust;
    double gravity;
    double decay;
    
    
    // animation update, call once per "frame"
    public void update() {
        
        // decay thrust with deadband
        if (velocity.x > decay)
            velocity.x -= decay;
        else if (velocity.x < decay)
            velocity.x += decay;
        else
            velocity.x = 0;
        
        if (velocity.y > decay)
            velocity.y -= decay;
        else if (velocity.y < decay)
            velocity.y += decay;
        else
            velocity.y = 0;
        
        // keep it falling
        velocity.y += gravity;
        
        // finally update the position
        position.add(velocity);
    }
    
    // methods to control the ship
    
    // convienience methods for cardinal thrust directions
    public void thrustUp() {
        thrustVector(new Vector2d(0, -thrust));
    }
    
    public void thrustDown() {
        thrustVector(new Vector2d(0, thrust));
    }
    
    public void thrustLeft() {
        thrustVector(new Vector2d(-thrust, 0));
    }
    
    public void thrustRight() {
        thrustVector(new Vector2d(thrust, 0));
    }
    
    // apply a vector of thrust
    void thrustVector(Vector2d v) {
        if (!isPaused() && fuel > 0) {
            velocity.add(v);
            fuel = Math.max(fuel - 1, 0);
        }
    }
    
    private int[] xFlagPoints, yFlagPoints;
    //draw the ship
    public void PlayViewdraw(Graphics2D g2) {
        // when a ship is crashed
        if(this.isShipCrashed) {
            //    			System.out.println("ship! is crashed");
            g2.setColor(Color.YELLOW);
            g2.drawOval((int)position.x-3, (int)position.y-3, 15, 15);
        }
        else if(this.isShipLanding) {
            xFlagPoints = new int[3];
            yFlagPoints = new int[3];
            xFlagPoints[0] = (int)position.x;
            yFlagPoints[0] = (int)position.y - 10;
            xFlagPoints[1] = (int)position.x + 5;
            yFlagPoints[1] = (int)position.y - 5;
            xFlagPoints[2] = (int)position.x;
            yFlagPoints[2] = (int)position.y - 5;
            g2.setColor(Color.YELLOW);
            g2.fillPolygon(xFlagPoints, yFlagPoints, 3);
            g2.setColor(Color.BLACK);
            g2.drawLine(xFlagPoints[0], yFlagPoints[0], (int)position.x, (int)position.y);
        }
        g2.setColor(Color.BLUE);
        g2.fillRect((int)position.x, (int)position.y, 10, 10); // need to change in the future, set to global variables
    }
    
    // observable interface helper (often call these two together)
    public void setChangedAndNotify() {
        setChanged();
        notifyObservers();
    }
}
