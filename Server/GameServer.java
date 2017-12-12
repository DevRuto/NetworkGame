/*
 * GameServer.java
 *
 * @author Kajal Nagrani, Winston Chang, Alex Kramer, Caleb Maynard, Aiden Lin
 * @since 2017-12-08
 * @version 1.0
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The game server that connects MemoryGame clients
 */
class GameServer {
    private final ArrayList<Client> clients = new ArrayList<Client>();
    private final ArrayList<String> ids = new ArrayList<String>();
    private final ArrayList<Integer> boardNums = new ArrayList<Integer>();

    private ServerSocket server;
    private final int boardNum = 36;
    private Client currentTurn = null;

    /**
     * The main method
     */
    public static void main(String[] args) throws UnknownHostException {
        GameServer server = new GameServer(12345);
        System.out.println("Server started on port: " + server.getPort());
        server.start();
    }

    /**
     * Construct the game server
     * @param port the port to listen on
     */
    public GameServer(int port) {
        // Generate board number at start
        for (int i = 0; i < boardNum; i++) {
            boardNums.add(i);
        }
        // Shuffle numbers
        Collections.shuffle(boardNums);
        // Try to start the server
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error creating server socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the listening port
     * @return the listneing port
     */
    public int getPort() {
        return server.getLocalPort();
    }

    /**
     * Starts listening for clients
     */
    public void start() {
        Socket s;
        Client c;
        try {
            // Continue to listen for clients
            while ((s = server.accept()) != null) {
                System.out.println("Accepted client");
                // Handle clients in new thread
                c = new Client(s);
                c.start();
            }
        } catch (IOException e) {
            System.out.println("Error accepting socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the next client in order
     * @param client the current client
     * @return the next client
     */
    private Client GetNext(Client client) {
        if (0 == clients.size())
            return null;
        // Get the index of the current client
        int index = clients.indexOf(client);

        // Return the next client
        return clients.get((index + 1) % clients.size());
    }

    /**
     * Handle the connection of a client
     * @param client the client that connected
     */
    private void handleConnected (Client client) {
        // Stay thread safe
        synchronized (clients) {
            if (null == currentTurn)
                currentTurn = client;

            clients.add(client);
            client.setPlayerNum(clients.size() - 1);
            client.setIdentifier("Player#" + clients.size());

            // Annouce new player
            broadcast(Protocol.MESSAGE, client.getIdentifier() + " has connected");

            if (currentTurn == client)
                client.writeInt(Protocol.MY_TURN);

            // Broadcast scores
            SendScores();
        }
    }

    /**
     * Handle the disconnection of a client
     * @param client the client that disconnected
     */
    private void handleDisconnected(Client client) {
        // Stay thread safe
        synchronized (clients) {
            if (clients.contains(client)) {
                if (client == currentTurn) {
                    currentTurn = GetNext(currentTurn);
                    currentTurn.writeInt(Protocol.MY_TURN);
                }

                clients.remove(client);
                ids.remove(client.getIdentifier());

                if (client == currentTurn)
                    currentTurn = null;
                // Broadcast scores
                SendScores();
            }
            // Annouce player has left
            broadcast(Protocol.MESSAGE, client.getIdentifier() + " has disconnected");
        }
    }

    /**
     * Broadcast scores to all clients
     */
    private void SendScores() {
        // Stay thread safe
        synchronized (clients) {
            for (Client c : clients) {
                // Tell client we are about to send score
                c.writeInt(Protocol.SCORES);

                // Send scores of all players
                for (int i = 0; i < 4; i++) {
                    if (clients.size() > i) {
                        c.writeInt(clients.get(i).score);
                        c.writeUTF(clients.get(i).getIdentifier());
                    } else {
                        c.writeInt(-1);
                        c.writeUTF("");
                    }
                }
            }
        }
    }

    /**
     * Perform the turn of a player
     * @param moveIndex1 the first move of the player
     * @param moveIndex2 the second move of the player
     * @param client the client that moved
     */
    private boolean MakeTurn(int moveIndex1, int moveIndex2, Client client) {
        synchronized (clients) {
            //client found the right pair
            if (boardNums.get(moveIndex1) == boardNums.get(moveIndex2)) {
                client.score++;

                broadcastInt(Protocol.SHOW_PAIR);
                broadcastInt(moveIndex1);
                broadcastInt(moveIndex2);

                ///mark numbers as already open
                boardNums.set(moveIndex1, -1);
                boardNums.set(moveIndex2, -1);
            } else {
                //bad pair

                try {///so wait some time and tell client to hide pair
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}

                client.writeInt(Protocol.HIDE_PAIR);
                client.writeInt(moveIndex1);
                client.writeInt(moveIndex2);
            }

            //go to next player
            currentTurn = GetNext(currentTurn);
            currentTurn.writeInt(Protocol.MY_TURN);
        }

        // Broadcast scores
        SendScores();

        // Check if game is done
        boolean gameOver = true;

        for (int i = 0; i < boardNums.size(); i++) {
            if (boardNums.get(i) != -1)
                gameOver = false;
        }

        if (gameOver) {
            String winners = "";
            int maxScore = 0;
            boolean draw = false;
            for (Client c : clients) {

                if (c.score > maxScore) {
                    winners = c.getIdentifier();
                    maxScore = c.score;
                    draw = false;
                } else if (c.score == maxScore) {
                    winners = winners + ", " + c.getIdentifier();
                    draw = true;
                }
            }

            if (draw)
                broadcast(Protocol.STATE, "Draw between " + winners);
            else
                broadcast(Protocol.STATE, "Winner " + winners);

        } else {
            broadcast(Protocol.STATE, "Current Turn " + currentTurn.getIdentifier());
        }

        return true;
    }

    /**
     * Broadcast a message to all clients
     * @param code the code of the message
     * @param text the message itself
     */
    private void broadcast(int code, String text) {
        System.out.printf("BROADCASTING: %s%n", text);
        synchronized (clients) {
            for (Client c : clients) {
                c.write(code, text);
            }
        }
    }

    /**
     * Broadcast a number to all clients
     * @param code the code of the number
     * @param the number itself
     */
    private void broadcastInt(int code, int i) {
        synchronized (clients) {
            broadcastInt(code);
            for (Client c : clients) {
                c.writeInt(i);
            }
        }
    }

    /**
     * Broadcast a code to all clients
     * @param i the code
     */
    private void broadcastInt(int i) {
        synchronized (clients) {
            for (Client c : clients) {
                c.writeInt(i);
            }
        }
    }

    /**
     * Start the game with a player
     * @param startPlayer the playeer to start with
     */
    private void initGame(int startPlayer) {
        synchronized (clients) {
            for (Client c : clients) {
                // IF index of c inside clients == startPlayer, send 1, otherwise send 0
                c.writeInt(clients.indexOf(c) == startPlayer ? 1 : 0);
            }
        }
    }

    /**
     * The thread that handles a client
     */
    class Client extends Thread {
        private Socket socket;
        private DataOutputStream writer;
        private String identifier;
        private int playerNum;
        public int score;

        /**
         * Constructs the client thread
         * @param socket the socket of the client
         * @throws IOException when there is an error creating a stream
         */
        public Client(Socket socket) throws IOException {
            this.socket = socket;
            this.score = 0;
            writer = new DataOutputStream(socket.getOutputStream());
            handleConnected(this);
        }

        /**
         * Set the name of the player
         * @param identifier the name
         */
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Get the name of the player
         * @return the name
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Set the number of the player
         * @param num the number of the player
         */
        public void setPlayerNum(int num) {
            this.playerNum = num;
        }

        /**
         * Get the number of the player
         * @return the number of the player
         */
        public int getPlayerNum() {
            return playerNum;
        }

        /**
         * Handle the reading of messages in a thread
         */
        @Override
        public void run() {
            System.out.println("Thread started");
            try {
                DataInputStream reader = new DataInputStream(socket.getInputStream());

                // Switch between the code we read
                int code;
                while ((code = reader.readInt()) != -1) {
                    System.out.printf("%s: CODE: %d%n", identifier, code);
                    switch (code) {
                    case Protocol.BOARD_NUMBERS:

                        broadcast(Protocol.MESSAGE, String.format("[%s RECEIVED BOARD NUMBERS]", identifier));
                        // Change boardNum to change size of board
                        int size = boardNum; //reader.readInt();
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
                            write(Protocol.MESSAGE, "Name " + newidentifier + " in use");
                        } else {
                            // other ids are synchronized in clients
                            // so we continue to sync on it
                            synchronized (clients) {
                                ids.add(newidentifier);
                            }

                            broadcast(Protocol.MESSAGE, "Name " + identifier + " renamed to " + newidentifier);
                            synchronized (clients) {
                                ids.remove(identifier);
                            }
                            identifier = newidentifier;

                            write(Protocol.STATE, identifier);

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
                        int moveIndex1 = reader.readInt();
                        int moveIndex2 = reader.readInt();
                        MakeTurn(moveIndex1, moveIndex2, this);
                        break;
                    case Protocol.MESSAGE:
                        broadcast(Protocol.MESSAGE, getIdentifier() + ": " + reader.readUTF());
                        break;
                    }
                }
            } catch (IOException e) {
                // Error with reading
            } finally {
                // Finally disconnect client
                close();
                handleDisconnected(this);
            }
        }

        /**
         * Thread safely write a coded message to the client
         * @param type the code of the message
         * @param text the message itself
         */
        public synchronized void write(int type, String text) {
            try {
                writer.writeInt(type);
                writer.writeUTF(text);
            } catch (Exception ex) {
                System.out.println("Error occured for " + identifier);
                ex.printStackTrace();
            }
        }

        /**
         * Thread safely write to the client
         * @param i the code to send
         */
        public synchronized void writeInt(int i) {
            try {
                writer.writeInt(i);
            } catch (Exception ex) {
                System.out.println("Error occured for " + identifier);
                ex.printStackTrace();
            }
        }

        /**
         * Thread safely write to the client
         * @param i the message to send
         */
        public synchronized void writeUTF(String i) {
            try {
                writer.writeUTF(i);
            } catch (Exception ex) {
                System.out.println("Error occured for " + identifier);
                ex.printStackTrace();
            }
        }

        /**
         * Close the client
         */
        public void close() {
            try {
                writer.close();
                socket.close();
            } catch (IOException e) {

            }
        }
    }
}
