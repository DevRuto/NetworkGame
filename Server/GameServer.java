import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

class GameServer {
    private ServerSocket server;
    private final ArrayList<Client> clients = new ArrayList<Client>();
    private final ArrayList<String> ids = new ArrayList<String>();
    private final ArrayList<Integer> boardNums = new ArrayList<Integer>();

    public static void main(String[] args) throws UnknownHostException {
        GameServer server = new GameServer(12345);
        System.out.println("Server started on port: " + server.getPort());
        server.start();
    }

    Client currentTurn=null;

    public boolean MakeTurn(int board, Client client)
    {
		return false;
    }
    

    public GameServer(int port) {
    	
    	int debug =0;
    	
        for (int i = 0; i < 32-debug; i++){
            boardNums.add(i);
            boardNums.add(i);
        }
        for (int i = 0; i < debug; i++){
            boardNums.add(-1);
            boardNums.add(-1);
        }
        // Shuffle numbers
        Collections.shuffle(boardNums);
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error creating server socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getPort() {
        return server.getLocalPort();
    }

    public void start() {
        Socket s;
        Client c;
        try {
            while ((s = server.accept()) != null) {
                System.out.println("Accepted client");
                c = new Client(s);
                c.start();
            }
        } catch (IOException e) {
            System.out.println("Error accepting socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Client GetNext(Client client)
    {
        if(0==clients.size())
        	return null;

        int index = clients.indexOf(client);
        
        return clients.get((index+1)%clients.size());
    }
    
    private void handleConnected (Client client) {
        synchronized (clients) {
        	
        	if(null==currentTurn)
        		currentTurn=client;
        	
            clients.add(client);
            client.setIdentifier("Player#" + clients.size());
            broadcast(Protocol.MESSAGE,client.getIdentifier() + " has connected");
            
            
            if(currentTurn==client)
            	client.writeInt(Protocol.MY_TURN);

    	    SendScores();

        }
    }

    private void handleDisconnected(Client client) {
        synchronized (clients) {
            if (clients.contains(client)) {
            	
            	if(client==currentTurn)
            	{
            		currentTurn=GetNext(currentTurn);

            		currentTurn.writeInt(Protocol.MY_TURN);
            	}
            	
                clients.remove(client);
                ids.remove(client.getIdentifier());

                if(client==currentTurn)
                	currentTurn=null;
                
        	    SendScores();
            }
            broadcast(Protocol.MESSAGE,client.getIdentifier() + " has disconnected");
        }
    }


    private void SendScores()
    {
		
	    synchronized (clients) {
            for (Client c : clients) {

            	c.writeInt(Protocol.SCORES);

            	for(int i=0;i<4;i++)
				{
					if(clients.size()>i)
					{
						c.writeInt(clients.get(i).score);
						c.writeUTF(clients.get(i).getIdentifier());
					}    
					else
					{
						c.writeInt(-1);
						c.writeUTF("");
					} 
				}
				
            }
	    }
    }

    private boolean MakeTurn(int moveIndex1, int moveIndex2,Client client)
    {
	    synchronized (clients) {
	    	
	    	//client found the right pair
	    	if(
	    		boardNums.get(moveIndex1)==boardNums.get(moveIndex2)
	    	)
	    	{
	    		client.score++;
	
	    		broadcastInt(Protocol.SHOW_PAIR);
	    		broadcastInt(moveIndex1);
	            broadcastInt(moveIndex2);
	
	
	            ///mark numbers as already open
	            boardNums.set(moveIndex1,-1);
	            boardNums.set(moveIndex2,-1);
	    	}
	    	else
	    	{//bad pair
	
	            try {///so wait some time and tell client to hide pair
	                Thread.sleep(1000);
	            } catch (InterruptedException e){}
	            
	            client.writeInt(Protocol.HIDE_PAIR);
	            client.writeInt(moveIndex1);
	            client.writeInt(moveIndex2);    		
	    	}
	    	
	        
	        //go to next player
			currentTurn=GetNext(currentTurn);
			currentTurn.writeInt(Protocol.MY_TURN);
	
	    }
	    
	    SendScores();
	    
        boolean gameOver=true;
	    
        for(int i=0;i<boardNums.size();i++)
        {
        	if(boardNums.get(i)!=-1)
        		gameOver=false;
        }
        
        if(gameOver)
        {
	        String winners="";
	        int maxScore=0;
	        boolean draw=false;
            for (Client c : clients) {
            	
            	if(c.score>maxScore)
            	{
            		winners=c.getIdentifier();
            		maxScore=c.score;
            		draw=false;
            	}
            	else
            	if(c.score==maxScore)
            	{
            		winners=winners+", "+c.getIdentifier();
            		draw=true;
            	}
            }

	        if(draw)
	        	broadcast(Protocol.STATE,"Draw between "+winners);
	        else
	        	broadcast(Protocol.STATE,"Winner "+winners);
	        
        }
        else
        {
        	broadcast(Protocol.STATE,"Current Turn "+currentTurn.getIdentifier());
        }
        
	    
	    return true;
    }
    
    private void broadcast(int type,String text) {
        System.out.printf("BROADCASTING: %s%n", text);
        synchronized (clients) {
            for (Client c : clients) {
                c.write(type,text);
            }
        }
    }

    private void broadcastInt(int i) {
        synchronized (clients) {
            for (Client c : clients) {
                c.writeInt(i);
            }
        }
    }

    private void initGame(int startPlayer) {
        synchronized (clients) {
            for (Client c : clients) {
                // IF index of c inside clients == startPlayer, send 1, otherwise send 0
                c.writeInt(clients.indexOf(c) == startPlayer ? 1 : 0);
            }
        }
    }

    class Client extends Thread {
        private Socket socket;
        private DataOutputStream writer;
        private String identifier;
        
        public int score;

        public Client(Socket socket) throws IOException {
            this.socket = socket;
            this.score=0;
            writer = new DataOutputStream(socket.getOutputStream());
            handleConnected(this);
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public void run() {
            System.out.println("Thread started");
            try {
                DataInputStream reader = new DataInputStream(socket.getInputStream());

                // maybe add switch
                // to read between line or object
                int code;
                while ((code = reader.readInt()) != -1) {
                    System.out.printf("%s: CODE: %d%n", identifier, code);
                    switch (code) {
                    case Protocol.BOARD_NUMBERS:

                    	broadcast(Protocol.MESSAGE,String.format("[%s RECEIVED BOARD NUMBERS]", identifier));
                        // TODO: CHECK LOBBY
                        // DEFUALT SIZE 64
                        int size = 64; //reader.readInt();
                        // Warns clients that they will receive board numbers
                        writeInt(Protocol.BOARD_NUMBERS);
                        
                        // Broadcast new board numbers
                        for (int i : boardNums) {
                            writeInt(i);
                        }
                        break;
                    case Protocol.NAME:
                        String newidentifier = reader.readUTF();
                        if (ids.contains(newidentifier)) {
                            // Name exists, find new name
                            write(Protocol.MESSAGE,"Name "+newidentifier+" in use");
                        } else {
                            // other ids are synchronized in clients
                            // so we continue to sync on it
                            synchronized (clients) {
                                ids.add(newidentifier);
                            }

                            
                        	broadcast(Protocol.MESSAGE,"Name "+identifier+" renamed to "+newidentifier);

                            identifier=newidentifier;                            
                            
                            write(Protocol.STATE,identifier);
                        	
                            SendScores();
                        }
                        break;
                    case Protocol.JOIN_LOBBY:
                        broadcastInt(Protocol.JOIN_LOBBY);
                        broadcastInt(clients.size());
                        if (clients.size() == 4) {
                            // Choose a player that starts the game
                            int start = ThreadLocalRandom.current().nextInt(0, clients.size());
                            // Check initGame doc
                            initGame(start);
                        } else {
                            // GAME NOT STARTING
                            broadcastInt(-1);
                        }
                        break;
                        

                    case Protocol.MOVE_PAIR:
                    {
                        int moveIndex1 = reader.readInt();
                        int moveIndex2 = reader.readInt();
                        MakeTurn(moveIndex1,moveIndex2,this);
                    }
                    break;


                    case Protocol.MESSAGE:
                        broadcast(Protocol.MESSAGE,getIdentifier() + ": " + reader.readUTF());
                        break;
                    }
                }
            } catch (IOException e) {
                // Error with reading
            } finally {
                close();
                handleDisconnected(this);
            }
        }

        public synchronized void write(int type,String text) {
            try {
                writer.writeInt(type);
                writer.writeUTF(text);
            } catch (Exception ex) {
                System.out.println("Error occured for " + identifier);
                ex.printStackTrace();
            }
        }

        public synchronized void writeInt(int i) {
            try {
                writer.writeInt(i);
            } catch (Exception ex) {
                System.out.println("Error occured for " + identifier);
                ex.printStackTrace();
            }
        }

        public synchronized void writeUTF(String i) {
            try {
                writer.writeUTF(i);
            } catch (Exception ex) {
                System.out.println("Error occured for " + identifier);
                ex.printStackTrace();
            }
        }

        public void close() {
            try {
                writer.close();
                socket.close();
            } catch (IOException e) {

            }
        }
    }
}
