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
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.*;
/**
 * A memory game where two players compete
 * to match as many cards as possible
 *
 * @author Kajal Nagrani, Winston Chang, Alex Kramer, Caleb Maynard, Aiden Lin
 * @since 2017-12-08
 * @version 1.0
 */

public class MemoryGame extends JFrame implements ActionListener {
   //Attributes
   private List<Card> cards = new ArrayList<Card>();
   private Card card1=null;

   private Timer timer;
   private java.util.Timer cardTimer = new java.util.Timer();
   private JLabel turn;
   private int player = 1;

   
   private JTextField player1S = new JTextField(3);
   private JTextField player2S = new JTextField(3);
   private JTextField player3S = new JTextField(3);
   private JTextField player4S = new JTextField(3);
   
   private JTextField [] scoreField = {player1S, player2S, player3S, player4S};

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
   private String ip;

	public static final int NAME = 0;
	public static final int BOARD_NUMBERS = 1;
	public static final int JOIN_LOBBY = 2;

	
	public static final int MESSAGE = 4;
	
	
	public static final int SCORES= 5;
	public static final int WON= 6;
	public static final int MY_TURN= 7;

	
	public static final int MOVE_PAIR = 10;
	public static final int SHOW_PAIR = 11;
	public static final int HIDE_PAIR = 12;
	public static final int STATE = 13;
  public static final int TURNSTATE = 14;
	
	

	public static final int ERROR= 100;

   
   public static final Color [] playerColors = {Color.BLUE,Color.GREEN,Color.ORANGE,Color.CYAN};
   //private JButton jbReset  = new JButton("Reset");
   private JLabel statelabel;
   private JButton jbExit  = new JButton("Exit");
   private JButton jbHelp  = new JButton("Help");
   private JButton jbName = new JButton("Change Your Name");
   private JButton[] jbList = new JButton[64];
   private int[] setUp;
   private  List<Integer> cardValues = new ArrayList<Integer>();
   private ActionListener acl_Card = 
      new ActionListener() { 
         public void actionPerformed(ActionEvent ae) {
            doTurn((Card) ae.getSource());
            System.out.println("this works");
         }
      };
  private int turnNumber;
  private Color currentColor;
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
                                   
      ip = (String)JOptionPane.showInputDialog(this,
                    "Type server IP",
                    "127.0.0.1");
                    
   
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
      //jpSubnorth.add(jbReset);
      jpSubnorth.add(jbExit);
      jpSubnorth.add(jbName);
      jpSubnorth.add(jbHelp);
      jpNorth.add(textNorth);
      jpNorth.setEnabled(false);
        
      jpNorth.add(jpSubnorth, BorderLayout.NORTH);
      /* create a JPanel(subsouth) inside the jpNorth
      * add the JLabel to subsouth, then add it to the jpNorth
      */
      JPanel jpSubsouth = new JPanel();
      statelabel = new JLabel("Start to play the game", JLabel.CENTER);
      jpSubsouth.add(statelabel);
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
      
         // Auto-scroll to keep updating the chatArea
      DefaultCaret caret = (DefaultCaret)chatArea.getCaret();
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
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
      
      // Name button
      jbName.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

            	String res = JOptionPane.showInputDialog(
                        "Type Name",
                        "");
            	
            	if(res.length()>0)
            	{
                    try {
						dos.writeInt(NAME);
	                    dos.writeUTF(res);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            }
         });
   
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
         dos.writeInt(MESSAGE);
         dos.writeUTF(messageBox.getText());
      }
      catch(IOException ioe){
         ioe.printStackTrace();
      }
      
      messageBox.setText(null);
      messageBox.requestFocus();
   }

   
   boolean myTurn=false;
   
   /**
    * A method that checks if the cards match
    */
   public void doTurn(Card selectedCard) {
   
	   if(!myTurn)
		   return;
	   
	   ///already matched
	   if(selectedCard.getMatched())
		   return;
       
      if (card1 == null) {
         selectedCard.setText(String.valueOf(selectedCard.getId()));
         card1 = selectedCard;
         selectedCard.setUpColor(currentColor);
         card1.flip(true);

      }
      else
      if ( card1 != selectedCard ) {
    	  selectedCard.setText(String.valueOf(selectedCard.getId()));
          selectedCard.setUpColor(currentColor);
    	  selectedCard.flip(true);

         int index1 = cards.indexOf(card1);
         int index2 = cards.indexOf(selectedCard);
         
         card1=null;
         myTurn=false;

   	  	setTitle("");

         
   	  	System.out.println("Move "+index1+" "+index2);

   	  
         try{
            dos.writeInt(MOVE_PAIR);
            dos.writeInt(index1);
            dos.writeInt(index2);
         }
         catch(IOException ioe){
            ioe.printStackTrace();
         }
         
      }
	   
   }

   /**
   A method that makes initial connection with the server
   Creates Client Thread
   */
   
   private void initConn() {
      try {
         socket = new Socket(ip, 12345);
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
 /*
 * Sends value to server that indicates that the client needs the numbers for the board    
 * 
 */
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
                           if(number==-1)
                           {
                        	   card.setMatched(true);
                        	   card.flip(true);
                           }
                           jpCenter.add(card);
                           cards.add(card);
                           card.addActionListener(acl_Card);
                           System.out.println(number);
                        }
                        break;
                     
                     }
                  case MY_TURN: {
                	  System.out.println("my turn");
                	  myTurn=true;
                	  
                	  setTitle("Your Turn");
                     /*
                	  turnNumber = dis.readInt();
                     turnValidate = dis.readBoolean();
                     jpCenter.setEnabled(turnValidate);
                     currentColor = playerColors[turnNumber];
					*/
                      break;
           
                  }
                  
                  case SHOW_PAIR:
                  {
                      int cardIndex1 = dis.readInt();
                      int cardIndex2 = dis.readInt();
                      Card o1 = cards.get(cardIndex1);
                      Card o2 = cards.get(cardIndex2);
                      
                	  System.out.println("Show "+cardIndex1+" "+cardIndex2);
                	                       

                      o1.flip(true);
                      o1.setMatched(true);
                      o2.flip(true);
                      o2.setMatched(true);



                      break;
                  }
                  
                  case HIDE_PAIR:
                  {
                      int cardIndex1 = dis.readInt();
                      int cardIndex2 = dis.readInt();
                      Card o1 = cards.get(cardIndex1);
                      Card o2 = cards.get(cardIndex2);
                      
                	  System.out.println("Hide "+cardIndex1+" "+cardIndex2);
                      
                	  o1.setUpColor(Color.RED);
                      o1.flip(false);
                      o2.setUpColor(Color.RED);
                      o2.flip(false);


                      break;
                  }

                  case MESSAGE:
                     {
                        String message = dis.readUTF();
                        chatArea.append("\n" + message);
                   	 System.out.println(message);
                        break;
                     }

                  case SCORES:
                  {
                  	 player1S.setText(dis.readInt()+"");
                 	 player1.setText(dis.readUTF());
                 	 
                 	 player2S.setText(dis.readInt()+"");
                 	 player2.setText(dis.readUTF());

                 	 player3S.setText(dis.readInt()+"");
                 	 player3.setText(dis.readUTF());
                 	 
                 	 player4S.setText(dis.readInt()+"");
                 	 player4.setText(dis.readUTF());
                                         	 
                     break;
                  }
                  
                  case STATE:
                  {
                	  statelabel.setText(dis.readUTF());
                                         	 
                     break;
                  }
                  case TURNSTATE:
                  {
                    turnNumber = dis.readInt();
                    currentColor = playerColors[turnNumber];
                  }
                     
                     
                     
                 case ERROR:
                 {
                    String message = dis.readUTF();
                    chatArea.append("\n" + message);
                    break;
                 }
                  
               }
            
            }
            
         } catch(IOException ioe){
            ioe.printStackTrace();
         }

      }
   }
}
