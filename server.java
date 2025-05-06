import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class server {
    private static final int PORT = 5000;
    private static final String SHARED_DIRECTORY = "C:/Users/yahia/Downloads\r\n" ;
    // Replace with your actual DB info
    private static final String DB_URL = "jdbc:mysql://localhost:3306/file_transfer_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "yahia.123";

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
            while (true) {
                String command = dis.readUTF();
                if (command.equals("LIST")) {
                    File dir = new File(SHARED_DIRECTORY);
                    String[] files = dir.list();
                    if (files != null) {
                        for (String file : files) {
                            dos.writeUTF(file);
                        }
                    }
                    dos.writeUTF("END");
                } else if (command.equals("GET")) {
                    String fileName = dis.readUTF();
                    File file = new File(SHARED_DIRECTORY, fileName);
                    System.out.println("📁 Requested file: " + file.getName());

                    if (!file.exists() || file.isDirectory()) {
                        dos.writeUTF("NOT_FOUND");
                        System.out.println("❌ File not found.");
                        continue;
                    }

                    dos.writeUTF("FOUND");
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            dos.write(buffer, 0, bytesRead);
                        }
                    }

                    logTransferToDatabase(file.getName(), "unknown");
                    System.out.println("✅ File sent and logged.");
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Client disconnected.");
        }
    }

    private static void logTransferToDatabase(String fileName, String genre) {
        String sql = "INSERT INTO file_transfers (file_name, genre, transfer_date) VALUES (?, ?, ?)";

        try {
            // Explicitly load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                pstmt.setString(1, fileName);
                pstmt.setString(2, genre);
                pstmt.setString(3, now);
                pstmt.executeUpdate();
            }

        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Failed to log to MySQL DB.");
            e.printStackTrace();
        }
    }
}
