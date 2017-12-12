/*
 * Card.java
 *
 * @author Kajal Nagrani, Winston Chang, Alex Kramer, Caleb Maynard, Aiden Lin
 * @since 2017-12-08
 * @version 1.0
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * A component containing information
 * and animations for a card
 */
public class Card extends JComponent implements MouseListener {
    private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
    private int id;
    private boolean matched = false;
    private boolean flip = false;
    private java.util.Timer timer;
    private boolean textVisible = false;
    private JLabel label;
    private int x, y, width, height;
    private boolean halfPoint = false;
    private boolean running = false;
    private ActionEvent ae = new ActionEvent(this,3,"string");
    private Color downColor = Color.red;
    private Color upColor = Color.blue;

    /**
     * Set up the card layout
     * @param timer The timer for the flip animation to base on
     */
    public Card(java.util.Timer timer) {
        this.timer = timer;
        label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        addMouseListener(this);
    }

public void setUpColor(Color c){
   upColor = c;


}
    /**
     * The identifier of the card
     * @param id The identifier of the card
     */
    public void setId(int id) {
        this.id = id;
        label.setText(id + "");
    }

    /**
     * SGet the id of this card
     */
    public int getId() {
        return this.id;
    }

    /**
     * Set the text of the card
     * @param text the text of the card to set
     */
    public void setText(String text) {
        label.setText(id + "");
    }

    /**
     * Set the determined status
     * @param matched Determines if this card has been matched
     */
    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    /**
     * Returns true if this card has been matched
     */
    public boolean getMatched() {
        return this.matched;
    }

    /**
     * Get the width thats drawable
     */
    private int getDrawWidth() {
        return getWidth() - 1;
    }

    /**
     * Get the height thats drawable
     */
    private int getDrawHeight() {
        return getHeight() - 2;
    }

    /**
     * Starts the flip animation
     */
    public void flip(boolean flip) {
        this.flip = flip;
        width = getDrawWidth();
        height = getDrawHeight();
        x = 2;
        y = 0;
        halfPoint = false;
        timer.schedule(new FlipTask(), 0, 2);
    }

    /**
     * Draws the card
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        label.setVisible(flip);
        //label.setVisible(true);
        label.setForeground(flip?Color.BLACK:Color.GRAY);
        // Draw background
        g.setColor(label.isVisible() ? upColor : downColor);
        //g.fillRect(x, y, width, height);

        // Set border color
        if (flip) {
            g.setColor(Color.black);
            g.drawRect(x, y, width - 4, height);
        } else {
            g.setColor(Color.black);
            g.drawRect(0, 0, getDrawWidth(), getDrawHeight());
        }
    }

    /**
     * Custom onclick implementation
     */
    public void addActionListener(ActionListener listener) {
        if (!actionListeners.contains(listener))
            actionListeners.add(listener);
    }

    /**
     * Perform onclick
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        for (ActionListener listener : actionListeners)
            listener.actionPerformed(ae);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    /**
     * A task that performs the flip animation
     */
    class FlipTask extends TimerTask {

        /**
         * Perform the flip animation
         */
        @Override
        public void run() {
            if (running) return;
            if (!halfPoint) {
                x += 1;
                width -= 2;
            } else {
                x -= 1;
                width += 2;
            }

            if (width >= getWidth()) {
                running = false;
                repaint();
                cancel();
            }
            if (width <= 0) {
                halfPoint = true;
                textVisible = !textVisible;
            }

            repaint();
        }
        
    }
}
