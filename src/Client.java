//import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	private static SessionManager sessionManager;
	private static  boolean isValidPort = false;
	private  static boolean isValidIP = false;
	private static String address = null;
	private static int port = 0;
	
	public static void main(String[] args) throws Exception {
	    
		checkParamsServer();
		
		socket = new Socket(address, port);
		System.out.format("Serveur lancé sur [%s:%d]\n", address, port);
		
		sessionManager =  new SessionManager(socket);
		
		
		String helloMessageFromServer = sessionManager.receiveText();
		System.out.println(helloMessageFromServer);
		
		sessionManager.sendFile("./utils/client/file.txt");
		sessionManager.receiveFile("./utils/client/NewFile.txt");
		
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
				System.out.println("Please enter a valid port number.");
				scanner.nextLine();
			} 
		}
		
		scanner.close();
		
	}
	
	
	
}
