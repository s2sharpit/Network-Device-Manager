import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ClientGUI {

    private static final String SERVER_IP = "192.168.35.222";
    private static final int SERVER_PORT = 9999;

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static OutputStream outputStream;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Application");
        JTextField messageInput = new JTextField();
        JTextArea chatArea = new JTextArea();
        JButton sendButton = new JButton("Send");
        JButton sendFileButton = new JButton("Send File");

        String username = JOptionPane.showInputDialog("Enter your username:");
        frame.setTitle("Chat Application - " + username);

        try {
            // Connect to the server
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = socket.getOutputStream();

            // Send the username to the server
            out.println(username);

            // Start a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        chatArea.append(serverMessage + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageInput.getText();
                sendMessage(message);
                messageInput.setText("");
            }
        });

        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    sendFile(selectedFile.getAbsolutePath());
                }
            }
        });

        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.getContentPane().add(chatArea);
        frame.getContentPane().add(messageInput);
        frame.getContentPane().add(sendButton);
        frame.getContentPane().add(sendFileButton);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void sendMessage(String message) {
        out.println(message);
    }

    private static void sendFile(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
    
            String fileName = new File(filePath).getName();
            out.println("/sendFile " + fileName);
    
            // Create a directory to store received files if it doesn't exist
            File receivedFilesDir = new File("received_files");
            if (!receivedFilesDir.exists()) {
                receivedFilesDir.mkdir();
            }
    
            // Specify the file path where the server will save the received file
            String serverFilePath = "received_files/" + fileName;
    
            try (FileOutputStream fileOutputStream = new FileOutputStream(serverFilePath);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
    
                byte[] buffer = new byte[8192];
                int bytesRead;
    
                while ((bytesRead = bufferedInputStream.read(buffer)) > 0) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                    bufferedOutputStream.flush();
                }
    
                System.out.println("File saved on server: " + serverFilePath);
    
            } catch (IOException e) {
                e.printStackTrace();
            }
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
