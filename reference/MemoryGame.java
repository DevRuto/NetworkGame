import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;

/**
 * A memory game where two players compete
 * to match as many cards as possible
 *
 * @author Kajal Nagrani & Winston Chang
 * @since 2017-10-15
 */
public class MemoryGame extends JFrame {
    //Attributes
    private List<Card> cards;
    private Card selectedCard;
    private Card card1;
    private Card card2;
    private Timer timer;
    private java.util.Timer cardTimer = new java.util.Timer();
    private JLabel turn;
    private int player = 1;
    private int player1Score = 0;
    private int player2Score = 0;

    private JButton jbReset  = new JButton("Reset");
    private JButton jbExit  = new JButton("Exit");
    private JButton jbHelp  = new JButton("Help");
    private JButton[] jbList = new JButton[64];

    /**
     * Start the game
     */
    public static void main(String[] args) {
        new MemoryGame();
    }

    /**
     * Set up the game
     */
    public MemoryGame() {
        setSize(700, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List<Card> cardsList = new ArrayList<Card>();
        List<Integer> cardValues = new ArrayList<Integer>();

        //Displays an instruction menu of how to play the memory card game
        JOptionPane.showMessageDialog(this, "Instructions:  " +
                                      "Click on two of the buttons and if they have the same value you gain a point. " +
                                      "Clear the board and the player with the most matches wins.");

        //Creates a for loop that adds the cards randomly on the board
        int digit = 32;
        for (int i = 0; i < digit; i++) {
            cardValues.add(i);
            cardValues.add(i);
        }
        Collections.shuffle(cardValues);

        for (int val : cardValues) {
            Card c = new Card(cardTimer);
            c.setId(val);
            c.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    selectedCard = c;
                    doTurn();
                }
            });
            cardsList.add(c);
        }
        this.cards = cardsList;

        //set up the timer
        timer = new Timer(750,
        new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                checkCards();
            }
        });

        timer.setRepeats(false);

        // create a main new JPanel(jpNorth)
        JPanel jpNorth = new JPanel(new BorderLayout());

        /* create a JPanel(subnorth) inside the other JPanel(jpNorth).
         * add the buttons to the jpSubnorth, then add it to the jpNorth
         */
        JPanel jpSubnorth = new JPanel();
        jpSubnorth.add(jbReset);
        jpSubnorth.add(jbExit);
        jpSubnorth.add(jbHelp);
        jpNorth.add(jpSubnorth, BorderLayout.NORTH);
        /* create a JPanel(subsouth) inside the jpNorth
        * add the JLabel to subsouth, then add it to the jpNorth
        */
        JPanel jpSubsouth = new JPanel();
        jpSubsouth.add(new JLabel("Start to play the game", JLabel.CENTER));
        jpNorth.add(jpSubsouth, BorderLayout.SOUTH);
        // add the main JPanel to JFrame
        add(jpNorth, BorderLayout.NORTH);

        /* create other main JPanel call jpCenter, set the GridLayout 8x8 size
        * add the cards in the jpCenter, add the JPanel to JFrame
        * set the background to red
        */
        JPanel jpCenter = new JPanel(new GridLayout(8, 8));

        for (Card c : cards) {
            jpCenter.add(c);
        }
        add(jpCenter, BorderLayout.CENTER);
        jpCenter.setBackground(Color.red);

        // Exit button
        jbExit.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
        // reset button
        jbReset.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new MemoryGame();
                setVisible(false);
                Collections.shuffle(cardValues);
            }
        });
        // help button
        jbHelp.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               JOptionPane.showMessageDialog(MemoryGame.this, "You have to find a set of matching cards. Written by: Kajal Nagrani & Winston Chang \n",
                                              "Help", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        setVisible(true);
    }

    /**
     * A method that checks if the cards match
     */
    public void doTurn() {
        if (card1 == null && card2 == null) {
            card1 = selectedCard;
            card1.setText(String.valueOf(card1.getId()));
        }
        if (card1 != null && card1 != selectedCard && card2 == null) {
            card2 = selectedCard;
            card2.setText(String.valueOf(card2.getId()));
            timer.start();
            new Thread() {
                @Override
                public void run() {
                    // Delay for animation
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e){}
                    changePlayer();
                }
            }.start();
        }
    }

    /**
     * A method that checks whose turn it is to play the game.
     */
    public void changePlayer() {
        card2.setText(String.valueOf(card2.getId()));
        if (card1.getId() == card2.getId()) {
            if (player == 1) {
                player1Score++;
            } else {
                player2Score++;
            }
        } else if (card1.getId() != card2.getId() && player == 1) {
            player = 2;
            card1.flip();
            card2.flip();
            setTitle("Memory Game (Player 2)");
            //JOptionPane.showMessageDialog(this, "It's Player 2's turn.");
        } else {
            player = 1;
            card1.flip();
            card2.flip();
            setTitle("Memory Game (Player 1)");
            //JOptionPane.showMessageDialog(this, "It's Player 1's turn.");
        }
    }

    /**
     * A method that checks which player won by comparing the scores.
     */
    public void checkCards() {
        if (card1.getId() == card2.getId()) {
            card1.setEnabled(false);
            card2.setEnabled(false);
            card1.setMatched(true);
            card2.setMatched(true);
            if (this.isGameWon()) {
                if (player1Score > player2Score) {
                    JOptionPane.showMessageDialog(this, "Player 1 wins with a score of " + player1Score + "! " + "Click start to play again.");
                    System.exit(0);
                } else if (player2Score > player1Score) {
                    JOptionPane.showMessageDialog(this, "Player 2 wins with a score of " + player2Score + "! " + "Click start to play again.");
                    System.exit(0);
                } else {
                    JOptionPane.showMessageDialog(this, "Its a tie with both players at" + player1Score + "matches! " + "Click start to play again.");
                    System.exit(0);
                }
            }
        }
        else {
            card1.setText("");
            card2.setText("");
        }
        card1 = null;
        card2 = null;
    }

    /**
     * A method that checks who won the game
     */
    public boolean isGameWon() {
        for (Card c : this.cards) {
            if (c.getMatched() == false) {
                return false;
            }
        }
        return true;
    }

}