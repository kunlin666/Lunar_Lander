import javax.swing.*;
import javax.vecmath.Point2d;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.Observable;
import java.util.Observer;

// the actual game view
public class PlayView extends JPanel implements Observer {
    private GameModel model;
    
    public PlayView(GameModel Model) {
        super();
        this.model = Model;
        model.addObserver(this);
        
        this.layoutView();
        // needs to be focusable for keylistener
        setFocusable(true);
        
        // want the background to be black
        setBackground(Color.BLACK);
        this.playViewControllers();
        
    }
    
    public void layoutView() {
    }
    
    public void playViewControllers() {
        this.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if(model.isShipCrashed) {
                        model.ship.reset(model.ship.startPosition);
                        model.ship.setPaused(false);
                    }
                    else if(model.isShipLanding) {
                        model.ship.reset(model.ship.startPosition);
                        model.ship.setPaused(false);
                    }
                    else {
//                        System.out.println("Testing space not crashed");
                        model.ship.setPaused(!model.ship.isPaused());
                    }
                }
                if(!model.ship.isPaused()) {
                    if(e.getKeyCode() == KeyEvent.VK_W) {
                        model.ship.thrustUp();
                    }
                    else if(e.getKeyCode() == KeyEvent.VK_A) {
                        model.ship.thrustLeft();
                    }
                    else if(e.getKeyCode() == KeyEvent.VK_S) {
                        model.ship.thrustDown();
                    }
                    else if(e.getKeyCode() == KeyEvent.VK_D) {
                        model.ship.thrustRight();
                    }
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // draw the rectangle here(landing pad)
        Graphics2D g2 = (Graphics2D) g;
        // save the current g2 transform matrix
        AffineTransform M = g2.getTransform();
        g2.translate(this.getWidth()/2,this.getHeight()/2);
        g2.scale(3,3);
        g2.translate(-model.ship.getPosition().x,-model.ship.getPosition().y);
        
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(0, 0, (int)this.model.getWorldBounds().getWidth(), (int)this.model.getWorldBounds().getHeight());
        this.model.peaks.PlayViewdraw(g2);
        this.model.rect.PlayViewdraw(g2);
        this.model.ship.PlayViewdraw(g2);
        
        // reset the transform to what it was before we drew the shape
        g2.setTransform(M);
    }
    
    @Override
    public void update(Observable o, Object arg) {
        // ship is running and is crashed
        this.model.checkShipCrashed();
        this.model.checkShipLanding();
        if(model.isShipCrashed && !model.ship.isPaused()) {
            this.model.ship.setPaused(true);
        }
        else if(model.isShipLanding && !model.ship.isPaused()) {
            this.model.ship.setPaused(true);
        }
        revalidate();
        repaint();
    }
}
