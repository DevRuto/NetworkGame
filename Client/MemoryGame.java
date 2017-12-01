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
import java.io.*;
import java.net.Socket;
import javafx.scene.input.Mnemonic;
import javax.swing.*;



import java.awt.event.*;

/**
 * A memory game where two players compete
 * to match as many cards as possible
 *
 * @author Kajal Nagrani & Winston Chang
 * @since 2017-10-15
 */
public class MemoryGame extends JFrame implements ActionListener {
   //Attributes
   private List<Card> cards = new ArrayList<Card>();
   private Card selectedCard;
   private Card card1;
   private Card card2;
   private Timer timer;
   private java.util.Timer cardTimer = new java.util.Timer();
   private JLabel turn;
   private int player = 1;
   private int player1Score = 0;
   private int player2Score = 0;
   private int player3Score = 0;
   private int player4Score = 0;
   
   private JTextField player1S = new JTextField(3);
   private JTextField player2S = new JTextField(3);
   private JTextField player3S = new JTextField(3);
   private JTextField player4S = new JTextField(3);
   
   
   
   private JLabel player1 = new JLabel("Player 1: ");
   private JLabel player2 = new JLabel("Player 2: ");
   private JLabel player3 = new JLabel("Player 3: ");
   private JLabel player4 = new JLabel("Player 4: ");
   



   private JTextArea jta = new JTextArea(20,20);
   private JTextField jtf = new JTextField(20);
   private JPanel jpCenter;
   private JTextArea chatArea;
   private JTextField messageBox;
   private JButton sendBtn;
   private Socket socket;
   private PrintWriter writer;
   private ReadThread readerThread;
   private DataOutputStream dos;
   public static final int NAME = 0;
   public static final int BOARD_NUMBERS = 1;
   public static final int JOIN_LOBBY = 2;
   public static final int MOVE_INT = 3;
   public static final int MSG = 4;
   
   private JButton jbReset  = new JButton("Reset");
   private JButton jbExit  = new JButton("Exit");
   private JButton jbHelp  = new JButton("Help");
   private JButton[] jbList = new JButton[64];
   private int[] setUp;
   private  List<Integer> cardValues = new ArrayList<Integer>();
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
   
      setSize(800, 500);
      setLocationRelativeTo(null);
      setResizable(false);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   
      List<Card> cardsList = new ArrayList<Card>();
     
   
      //Displays an instruction menu of how to play the memory card game
      JOptionPane.showMessageDialog(this, "Instructions:  " +
                                   "Click on two of the buttons and if they have the same value you gain a point. " +
                                   "Clear the board and the player with the most matches wins.");
   
      //Creates a for loop that adds the cards randomly on the board
   
      timer = new Timer(5,this);
   
      timer.setRepeats(false);
   
      // create a main new JPanel(jpNorth)
      JPanel jpNorth = new JPanel(new BorderLayout());
      
              /*
        * Create another JPanel inside the jpNorth
        * This JPanel will keep track of the scores and display it on top of the GUI
        */
      JPanel textNorth = new JPanel();
      
      player1S.setEnabled(false);
   player2S.setEnabled(false);
   player3S.setEnabled(false);
   player4S.setEnabled(false);
        
      textNorth.add(player1);
      textNorth.add(player1S);
      textNorth.add(player2);
      textNorth.add(player2S);
      textNorth.add(player3);
      textNorth.add(player3S);
      textNorth.add(player4);
      textNorth.add(player4S);
      
      
   
      /* create a JPanel(subnorth) inside the other JPanel(jpNorth).
       * add the buttons to the jpSubnorth, then add it to the jpNorth
       */
      JPanel jpSubnorth = new JPanel();
      jpSubnorth.add(jbReset);
      jpSubnorth.add(jbExit);
      jpSubnorth.add(jbHelp);
      jpNorth.add(textNorth);
      jpNorth.setEnabled(false);
        
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
      jpCenter = new JPanel(new GridLayout(8, 8));
   
      
      JPanel chat = new JPanel();
      chat.setLayout(new GridBagLayout());
      
           // Constraints for layout
      GridBagConstraints c = new GridBagConstraints();
   
        // Add message field
      messageBox = new JTextField(10);
      messageBox.addActionListener(this);
      messageBox.setToolTipText("Message to send");
      messageBox.setEnabled(false);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(0, 10, 10, 10);
      c.ipady = 0;
      c.gridwidth = GridBagConstraints.RELATIVE;
      c.gridheight = GridBagConstraints.REMAINDER;
      c.weightx = 0.9;
      c.weighty = 0.3;
      c.gridx = 0;
      c.gridy = 1;
      c.anchor = GridBagConstraints.PAGE_END;
      chat.add(messageBox, c);
   
        // Add send button
      sendBtn = new JButton("Send");
      sendBtn.addActionListener(this);
      sendBtn.setEnabled(false);
      c.fill = GridBagConstraints.NONE;
      c.insets = new Insets(0, 10, 10, 10);
      c.ipady = 0;
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.gridheight = GridBagConstraints.REMAINDER;
      c.weightx = 0.1;
      c.weighty = 0.3;
      c.gridx = 1;
      c.gridy = 1;
      c.anchor = GridBagConstraints.PAGE_END;
      chat.add(sendBtn, c);
        
        // Add chat box
      chatArea = new JTextArea();
      chatArea.setEditable(false);
      JScrollPane spane = new JScrollPane(chatArea);
      chatArea.setBackground(Color.GRAY);
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(10, 10, -45, 10);
      c.ipady = 50;
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.gridheight = GridBagConstraints.RELATIVE;
      c.weightx = 1.0;
      c.weighty = .9;
      c.gridx = 0;
      c.gridy = 0;
      chat.add(spane, c);
         
        // Set up default behaviors
        
      chat.setSize(400, 400);
      chat.setMinimumSize(new Dimension(300, 300));
      
      jpCenter.setBackground(Color.red);
      add(chat, BorderLayout.EAST);
      
      
   
      
      add(jpCenter, BorderLayout.CENTER);
      // add(chat, BorderLayout.EAST);
      
   
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
      initConn();
      boardSetup();
   }
   
  
   //send button handler
   
   @Override
    public void actionPerformed(ActionEvent e) {
      if (messageBox.getText().isEmpty()) 
         return;
      
      try{
         dos.writeInt(MSG);
         dos.writeUTF(messageBox.getText());
      }
      catch(IOException ioe){
         ioe.printStackTrace();
      }
      
      messageBox.setText(null);
      messageBox.requestFocus();
   }


   /**
    * A method that checks if the cards match
    */
   public void doTurn() {
   
      int index = cards.indexOf(selectedCard);
      try{
         dos.writeInt(MOVE_INT);
         dos.writeInt(index);
      }
      catch(IOException ioe){
         ioe.printStackTrace();
      }
      
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
                     Thread.sleep(1000);
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
            player1S.setText("" + ++player1Score);
         } 
         else if(player == 2){
            player2S.setText("" + ++player2Score);
         }
         else if(player == 3){
            player3S.setText("" + ++player3Score);
         }
         
         else if(player == 4) {
            player4S.setText("" + ++player4Score);
         }
      } else if (card1.getId() != card2.getId() && player == 1) {
         player = 2;
         card1.flip();
         card2.flip();
         setTitle("Memory Game (Player 2)");
         JOptionPane.showMessageDialog(null,"It's Player 2's turn.");
      }  else if (card1.getId() != card2.getId() && player == 2) {
         player = 3;
         card1.flip();
         card2.flip();
         setTitle("Memory Game (Player 3)");
         JOptionPane.showMessageDialog(null,"It's Player 3's turn.");
      }  else if (card1.getId() != card2.getId() && player == 3) {
         player = 4;
         card1.flip();
         card2.flip();
         setTitle("Memory Game (Player 4)");
         JOptionPane.showMessageDialog(null,"It's Player 4's turn.");
      }
      
      else {
         player = 1;
         card1.flip();
         card2.flip();
         setTitle("Memory Game (Player 1)");
         JOptionPane.showMessageDialog(null,"It's Player 1's turn.");
         
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
            if (player1Score > player2Score && player1Score > player3Score && player1Score > player4Score) {
               JOptionPane.showMessageDialog(this, "Player 1 wins with a score of " + player1Score + "! " + "Click start to play again.");
               System.exit(0);
            } else if (player2Score > player1Score && player2Score > player3Score && player2Score > player4Score) {
               JOptionPane.showMessageDialog(this, "Player 2 wins with a score of " + player2Score + "! " + "Click start to play again.");
               System.exit(0);
            }else if (player3Score > player1Score && player3Score > player2Score && player3Score > player4Score){
               JOptionPane.showMessageDialog(this, "Player 3 wins with a score of " + player3Score + "! " + "Click start to play again.");
               System.exit(0);
            }else if (player4Score > player1Score && player4Score > player2Score && player4Score > player3Score){
               JOptionPane.showMessageDialog(this, "Player 4 wins with a score of " + player4Score + "! " + "Click start to play again.");
               System.exit(0);   
            
            } else {
               JOptionPane.showMessageDialog(this, "Its a tie with four players at" + player1Score + "matches! " + "Click start to play again.");
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
   
   /**
   A method that makes initial connection with the server
   Creates Client Thread
   */
   
   private void initConn() {
      try {
         socket = new Socket("129.21.140.25", 12345);
         readerThread = new ReadThread(socket.getInputStream());
         readerThread.start();
         
         dos = new DataOutputStream(socket.getOutputStream());
      
         chatArea.append("Connected to Server" + System.lineSeparator());
         
         messageBox.setEnabled(true);
         sendBtn.setEnabled(true);
         
      } catch (IOException e) {
         e.printStackTrace();
      
      }
   }
   
   private void boardSetup(){
      try{
      
         dos.writeInt(BOARD_NUMBERS);
      }
      catch(IOException ioe){
         ioe.printStackTrace();
      }
   }
       /**
     * A class to add additional startup behaviors
     */
   class GUIAdapter extends WindowAdapter {
    
        /**
         * Focus on mesasgeBox on startup
         */
      @Override
        public void windowOpened(WindowEvent e) {
         messageBox.requestFocus();
      }
   }
   
  
 /**
 */
   class ReadThread extends Thread {
      private InputStream inputStream;
      
      private DataInputStream dis;
      
      
        /**
         * Initialize thread for reading
         * @param inputStream the stream to read from
         */
      public ReadThread(InputStream inputStream) {
         this.inputStream = inputStream;
      }
   
        /**
         * Start reading from server
         */
      @Override
        public void run() {
        
      
         dis = new DataInputStream(inputStream);
         
         int number;
         
         try{
            while((number =dis.readInt()) != -1){
               switch(number){
                  case BOARD_NUMBERS: 
                     {
                     
                        for(int i=0; i < 64;i++){
                           number = dis.readInt();
                           cardValues.add(number);
                           Card card = new Card(cardTimer);
                           card.setId(number);
                            
                           jpCenter.add(card);
                           cards.add(card);
                          
                           card.addActionListener(
                              new ActionListener() {
                                 public void actionPerformed(ActionEvent ae) {
                                    selectedCard = card;
                                    doTurn();
                                    System.out.println("this works");
                                 }
                              });
                        
                          
                          
                           System.out.println(number);
                        
                        
                        }
                     
                     }
                  case MOVE_INT:
                     {
                        
                        int cardIndex = dis.readInt();
                        Card o = cards.get(cardIndex);
                     
                     }
                     
                  case MSG:
                     {
                        String message = dis.readUTF();
                        chatArea.append("\n" + message);
                     
                     
                     }
                  
               }
            
            }
            
         } catch(IOException ioe){
            ioe.printStackTrace();
         }
      
        
      }
   }
}


