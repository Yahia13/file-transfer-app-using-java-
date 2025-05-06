import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            Scanner scanner = new Scanner(System.in);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.print("📄 Enter file name to download: ");
            String fileName = scanner.nextLine();

            System.out.print("🎵 Enter file genre (e.g., image, video, doc): ");
            String genre = scanner.nextLine();

            dos.writeUTF(fileName);
            dos.writeUTF(genre); // Send genre info to server

            String response = dis.readUTF();
            if (response.equals("ERROR")) {
                System.out.println("❌ File not found on server.");
                return;
            }

            FileOutputStream fos = new FileOutputStream("downloads/" + fileName);
            byte[] buffer = new byte[4096];
            int bytesRead;

            System.out.println("⬇️ Downloading...");

            while ((bytesRead = dis.read(buffer)) > 0) {
                fos.write(buffer, 0, bytesRead);
                if (bytesRead < buffer.length) break;
            }

            fos.close();
            System.out.println("✅ File downloaded to 'downloads/" + fileName + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
