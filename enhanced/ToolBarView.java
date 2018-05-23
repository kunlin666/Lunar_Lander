import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.KeyStroke;



// the edit toolbar
public class ToolBarView extends JPanel implements Observer {
    
    private JButton undo = new JButton("Undo");
    private JButton redo = new JButton("Redo");
    // the model that this view is showing
    private GameModel model;
    
    public ToolBarView(GameModel _model) {
        super();
        this.model = _model;
        this.model.addObserver(this);
        
        this.layoutView();
        this.ToolBarControllers();
    }
    
    private void layoutView() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        // prevent buttons from stealing focus
        this.undo.setFocusable(false);
        this.redo.setFocusable(false);
        
        this.add(undo);
        this.add(redo);
    }
    
    private void ToolBarControllers() {
        // controller for undo button
        this.undo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                model.undo();
            }
        });
        // controller for redo button
        this.redo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                model.redo();
            }
        });
    }
    
    
    @Override
    public void update(Observable o, Object arg) {
        undo.setEnabled(model.canUndo());
        redo.setEnabled(model.canRedo());	
    }
}
