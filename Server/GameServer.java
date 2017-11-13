import java.io.*;
import java.net.*;
import java.util.ArrayList;

class GameServer {
	private ServerSocket server;
	private final ArrayList<Client> clients = new ArrayList<Client>();
	private final ArrayList<String> ids = new ArrayList<String>();

	public static void main(String[] args) throws UnknownHostException {
		GameServer server = new GameServer(12345);
		System.out.println("Server started on port: " + server.getPort());
		server.start();
	}

	public GameServer(int port) {
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
		synchronized (clients) {
			for (Client c : clients) { 
				c.writeln(text);
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

	class Client extends Thread {
		private Socket socket;
		private ObjectOutputStream writer;
		private String identifier;

		public Client(Socket socket) throws IOException {
			this.socket = socket;
			writer = new ObjectOutputStream(socket.getOutputStream());
			handleConnected(this);
		}

		public String getIdentifier() {
			return identifier;
		}

		@Override
		public void run() {
			try {
				ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

				// maybe add switch
				// to read between line or object
				int code;
				while ((code = reader.readInt()) != -1) {
					switch(code) {
						case Protocol.BOARD_NUMBERS:
							// TODO: CHECK LOBBY
							int size = reader.readInt();
							// Warns clients that they will receive board numbers
							broadcastInt(Protocol.BOARD_NUMBERS);
							int[] boardNums = new int[size];
							for (int i = 0; i < size; i++)
								boardNums[i] = i;
							// Shuffle numbers
							Collections.shuffle(boardNums);
							// Broadcast new board numbers
							for (int i = 0; i < size; i++) {
								broadcastInt(boardNums[i]);
							}
							break;
						case Protocol.NAME:
							identifier = reader.readUTF();
							if (ids.contains(identifier)) {
								// Name exists, find new name
								identifier = null;
								writeInt(NAME);
								write("Name in use");
							} else {
								// other ids are synchronized in clients
								// so we continue to sync on it
								synchronized(clients) {
									ids.add(identifier);
								}
								writeInt(NAME);
								write("Name registered");
							}
							break;
						case Protocol.JOIN_LOBBY:

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
			writer.writeUTF(text);
			writer.flush();
		}

		public synchronized void writeInt(int i) {
			writer.writeInt(i);
			writer.flush();
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