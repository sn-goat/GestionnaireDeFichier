import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Serveur
{
	private static ServerSocket listener;
	
	private static  boolean isValidPort = false;
	private  static boolean isValidIP = false;
	

	public static void main(String[] args) throws Exception
	{
		int clientNumber = 0;
		Scanner scanner = new Scanner(System.in);
		String serverAddress = null;
		int serverPort = 0;

		
		while(!isValidIP) {
			System.out.print("Enter a valid IP address: ");
			serverAddress = scanner.nextLine();
		
			String[] bytes = serverAddress.split("\\.");
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
				serverPort = scanner.nextInt();
				if(serverPort >= 5000 && serverPort <= 5050) {
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
		

		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);

		listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running %s:%d\n", serverAddress, serverPort);
		scanner.close();

		try
		{
			while(true)
			{ 
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		}
		finally 
		{ 
			listener.close();
		}
	}

	private static class ClientHandler extends Thread
	{
		private Socket socket;
		private int clientNumber;

		public ClientHandler(Socket socket, int clientNumber) 
		{ 
			this.socket = socket;
			this.clientNumber = clientNumber;

			System.out.println("New connection with client#" + clientNumber + " at " + socket);
		}
		
		public void run()
		{
			try
			{
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("Hello from server - you are client#" + clientNumber);
			}
			catch (IOException e)
			{
				System.out.println("Error handling client#" + clientNumber + " : " + e);
			}
			finally
			{
				try
				{
					socket.close();
				}
				catch (IOException e)
				{
					System.out.println("Could not close a socket");
				}
				System.out.println("Connection with client#" + clientNumber + " closed");
			}
		}
	}
}
