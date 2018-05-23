import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;

public class MessageView extends JPanel implements Observer {
    private GameModel model;
    
    // status messages for game
    JLabel fuel = new JLabel("fuel");
    JLabel speed = new JLabel("speed");
    JLabel message = new JLabel("message");
    
    private NumberFormat formatter = NumberFormat.getNumberInstance();
    
    
    public MessageView(GameModel Model) {
        super();
        this.model = Model;
        model.addObserver(this);
        
        this.layoutView();
        
    }
    
    public void layoutView() {
        // want the background to be black
        setBackground(Color.BLACK);
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        this.add(fuel);
        this.add(speed);
        this.add(message);
        for (Component c: this.getComponents()) {
            c.setForeground(Color.WHITE);
            c.setPreferredSize(new Dimension(100, 20));
        }
        
    }
    
    
    @Override
    public void update(Observable o, Object arg) {
        fuel.setForeground(Color.WHITE);
        if((int)this.model.ship.getFuel() < 10) {
            fuel.setForeground(Color.RED);
        }
        fuel.setText("fuel: " + formatter.format((int)this.model.ship.getFuel()));
        speed.setForeground(Color.WHITE);
        if(this.model.getIsShipSpeedSafe()) {
            speed.setForeground(Color.GREEN);
        }
        speed.setText("speed: " + String.format("%.2f",this.model.ship.getSpeed()));
        if(this.model.ship.isShipCrashed) {
            message.setText("CRASH");
        }
        else if(this.model.ship.isShipLanding) {
            message.setText("LANDED!");
        }
        else if(this.model.ship.isPaused()) {
            message.setText("(Paused)");
        }
        else{
            message.setText("");
        }
    }
}