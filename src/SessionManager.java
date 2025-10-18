import java.io.*;
import java.net.*;

public class SessionManager {
    private Socket socket;
    
    public SessionManager(Socket sessionSocket) {
        this.socket = sessionSocket;
    }
   
	public void uploadFile(String path) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        
        int bytes = 0;
        
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        
        
        out.writeLong(file.length());
        
        byte[] buffer = new byte[4*1024];
        
        while ((bytes=fileInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytes);
            out.flush();
        }
        fileInputStream.close();
        
    }
	
	public void uploadEmptyFile() throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        int bytes = 0;
        byte[] buffer = new byte[4*1024];
        out.write(buffer, 0, bytes);
        out.flush();
      
    }
    
	public void downloadFile(String fileName) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        
        
        int bytes = 0;
        long size = in.readLong();
        
        byte[] buffer = new byte [4 * 1024];
    
        
        while (size > 0 && 
        (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
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