import java.io.*;
import java.net.*;
import java.sql.*;

public class server {
    private static final int PORT = 5000;
    private static final String FILE_DIRECTORY = "C:\\Users\\yahia\\Downloads";  // Path to the server's files directory
    private static final String DB_URL = "jdbc:mysql://localhost:3306/file_transfer_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "yahia.123";  // Replace with your MySQL password

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🔌 Server started. Waiting for client...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("✅ Client connected: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            // Step 1: Receive the request for a file
            String requestedFile = dis.readUTF();
            System.out.println("📂 Server received file request for: " + requestedFile);  // Log the requested file name

            File fileToSend = new File(FILE_DIRECTORY, requestedFile);

            if (!fileToSend.exists()) {
                dos.writeUTF("NOT_FOUND");
                System.out.println("❌ File not found: " + requestedFile);
                return;
            }

            // Step 2: Send file information
            dos.writeUTF("FOUND");
            dos.writeUTF(fileToSend.getName());
            dos.writeLong(fileToSend.length());  // Send the file size

            // Step 3: Send the file content
            try (FileInputStream fis = new FileInputStream(fileToSend)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                System.out.println("✅ File sent: " + fileToSend.getName());
                
                // Log file transfer to the database
                logTransferToDatabase(fileToSend.getName(), "image");  // Assuming genre is "image" for now
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logTransferToDatabase(String fileName, String genre) {
        String insertQuery = "INSERT INTO file_transfers (file_name, genre, transfer_date) VALUES (?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(insertQuery)) {
            ps.setString(1, fileName);
            ps.setString(2, genre);
            ps.executeUpdate();
            System.out.println("✅ File transfer logged to database: " + fileName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
