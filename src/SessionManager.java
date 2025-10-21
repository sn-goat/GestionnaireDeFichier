import java.io.*;
import java.net.*;

public class SessionManager {
    private Socket socket;

    public SessionManager(Socket sessionSocket) {
        this.socket = sessionSocket;
    }

    public void uploadFile(String path) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        int maximumBytes = 4;
        int bytesOneKilobyte = 1024;
        int bytes = 0;

        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        out.writeLong(file.length());

        byte[] buffer = new byte[maximumBytes * bytesOneKilobyte];

        while ((bytes = fileInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytes);
            out.flush();
        }

        fileInputStream.close();
    }

    public void downloadFile(String fileName) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        int maximumBytes = 4;
        int bytesOneKilobyte = 1024;
        int bytes = 0;
        long size = in.readLong();

        byte[] buffer = new byte[maximumBytes * bytesOneKilobyte];

        while (size > 0 &&
                (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }

        fileOutputStream.close();
    }

    public void sendText(String text) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(text);
    }

    public String receiveText() throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        return in.readUTF();
    }
}
