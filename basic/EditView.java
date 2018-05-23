import javax.swing.*;
import javax.vecmath.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// the editable view of the terrain and landing pad
public class EditView extends JPanel implements Observer {
    private GameModel model;
    
    
    public EditView(GameModel Model) {
        super();
        this.model = Model;
        model.addObserver(this);
        
        //this.layoutView();
        // want the background to be black
        setBackground(Color.LIGHT_GRAY);
        this.EditViewControllers();
        
    }
    
    private void EditViewControllers() {
        // drag the landing pad
        this.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                // landing pad drag has the priority
                if(model.isRectSelected) {
                    // first click to drag, add base point
                    if(!model.isMouseDraging) {
                        model.setRectBasePoint(e.getX(), e.getY());
                        model.setRectAndPeakUndoPoint();
                        model.isMouseDraging = true;
                    }
                    else {
                        model.setRectMovePoint(e.getX(), e.getY());
                    }
                }
                // handle peaks dragging
                else if(model.isPeakSelected) {
                    // first click to drag, modify y point
                    if(!model.isMouseDraging) {
                        model.setPeakBasePoint(e.getY());
                        model.setRectAndPeakUndoPoint();
                        //						System.out.println("testing");
                        model.isMouseDraging = true;
                    }
                    else {
                        model.setPeakMovePoint(e.getY());
                    }
                }
            }
        });
        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                model.isRectSelected = model.isRectSelect(e.getX(), e.getY());
                if(!model.isRectSelected) {
                    model.isPeakSelected = model.isPeakSelect(e.getX(), e.getY());
                }
            }
        });
        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(model.isMouseDraging){
                    model.isMouseDraging = false;
                    model.setRectSelect(false);
                    model.setPeakSelect(false);
                    model.setPos(model.rect.getRectPos().x, model.rect.getRectPos().y, model.peaks.getCurPeakPos().x, model.peaks.getCurPeakPos().y, model.peaks.getUndoIndex());
                    //                    System.out.println("Index is: " + model.peaks.getUndoIndex());
                    //                    System.out.println("testing undo, model.peaks.getCurPeakPos() is: " + model.peaks.getCurPeakPos().x + model.peaks.getCurPeakPos().y);
                }
            }
        });
        
        // double click the landing pad
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    model.setRectClickedPoint(e.getX(), e.getY());
                }
            }
        });
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // draw the rectangle here(landing pad)
        Graphics2D g2 = (Graphics2D) g;
        this.model.peaks.draw(g2);
        this.model.rect.draw(g2);
    }
    
    
    //observable interface
    @Override
    public void update(Observable o, Object arg) {
        //    		System.out.print("Edit View update.");
        repaint();
        //    		System.out.println("Model: undo Rect Pos to " + model.rect.undoTopleftPoint.x + model.rect.undoTopleftPoint.y);
    }
    
}
