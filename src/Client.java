import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	private static  boolean isValidPort = false;
	private  static boolean isValidIP = false;
	private static String address = null;
	private static int port = 0;
	
	public static void main(String[] args) throws Exception {
	    
		checkParamsServer();
		
		socket = new Socket(address, port);
		System.out.format("Serveur lancé sur [%s:%d]\n", address, port);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		
		sendFile("./utils/client/file.txt");
		
		socket.close();
	}
	
	private static void checkParamsServer() {
		Scanner scanner = new Scanner(System.in);
		
		while(!isValidIP) {
			System.out.print("Enter a valid IP address: ");
			address = scanner.nextLine();
		
			String[] bytes = address.split("\\.");
			int number = 0;
			
			try {
				if(bytes.length == 4 ) {
						for(int i = 0; i < 4; ++i) {
							number = Integer.parseInt(bytes[i]);
							if (number < 0 || number > 255) {
								throw new Exception("Each byte must be within 0 and 255 inclusively.");
							}
						}
						isValidIP = true;
				} else {
					throw new Exception("The IP address must be only 4 byte long.");
				}
			} catch (Exception e ) {
				System.out.println(e.getMessage());
				System.out.println("Please enter a valid IP format.");
				
			} 
						
				
		}
		
		while(!isValidPort) {
			System.out.print("Enter a valid listenig port number: ");
			try {
				port = scanner.nextInt();
				if(port >= 5000 && port <= 5050) {
					isValidPort = true;
				} else {
					throw new Exception("The port number is not within 5000 and 5050.");
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("Please enter a valid type.");
				scanner.nextLine();
			} 
		}
		
		scanner.close();
		
	}
	
	private static void sendFile(String path) throws Exception {
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
		out.close();
		
		
		
	}
}
