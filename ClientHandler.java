import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String username = in.readLine();
            System.out.println(username + " joined the chat.");

            Server.broadcastMessage(username + " joined the chat.", this);

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                if (clientMessage.startsWith("/")) {
                    processCommand(username, clientMessage);
                } else {
                    Server.broadcastMessage(username + ": " + clientMessage, this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void processCommand(String username, String command) {
        // Implement command processing logic here
        // For now, just broadcast the command to all clients
        Server.broadcastMessage(username + ": " + command, this);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void disconnect() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            Server.removeClient(this);
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
