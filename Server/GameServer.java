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

    public GameServer(int port) {
                        for (int i = 0; i < 64; i++)
                            boardNums.add(i);
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

    private void handleConnected (Client client) {
        synchronized (clients) {
            clients.add(client);
            client.setIdentifier("Player#" + clients.size());
            broadcast(client.getIdentifier() + " has connected");
        }
    }

    private void handleDisconnected(Client client) {
        synchronized (clients) {
            if (clients.contains(client)) {
                clients.remove(client);
                ids.remove(client.getIdentifier());
            }
            broadcast(client.getIdentifier() + " has disconnected");
        }
    }

    private void broadcast(String text) {
        broadcastInt(Protocol.MESSAGE);
        System.out.printf("BROADCASTING: %s%n", text);
        synchronized (clients) {
            for (Client c : clients) {
                c.write(text);
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

        public Client(Socket socket) throws IOException {
            this.socket = socket;
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
                        broadcast(String.format("[%s RECEIVED BOARD NUMBERS]", identifier));
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
                        identifier = reader.readUTF();
                        if (ids.contains(identifier)) {
                            // Name exists, find new name
                            identifier = null;
                            writeInt(Protocol.NAME);
                            write("Name in use");
                        } else {
                            // other ids are synchronized in clients
                            // so we continue to sync on it
                            synchronized (clients) {
                                ids.add(identifier);
                            }
                            writeInt(Protocol.NAME);
                            write("Name registered");
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
                    case Protocol.MOVE_TURN:
                        broadcastInt(Protocol.MOVE_TURN);
                        int moveindex = reader.readInt();
                        broadcastInt(moveindex);
                        broadcast(String.format("[%s SENT MOVE, RECEIVED INDEX: %d]", identifier, moveindex));
                        break;
                    case Protocol.MESSAGE:
                        broadcast(identifier + ": " + reader.readUTF());
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

        public synchronized void write(String text) {
            try {
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

        public void close() {
            try {
                writer.close();
                socket.close();
            } catch (IOException e) {

            }
        }
    }
}
