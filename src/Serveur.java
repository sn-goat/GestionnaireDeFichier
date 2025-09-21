import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Serveur
{
	private static ServerSocket listener;
	
	private static  boolean isValidPort = false;
	private  static boolean isValidIP = false;
	private static String address = null;
	private static int port = 0;
	
	public static void main(String[] args) throws Exception
	{
		int clientNumber = 0;
		
		checkParamsServer();

		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(address);

		listener.bind(new InetSocketAddress(serverIP, port));
		System.out.format("The server is running %s:%d\n", address, port);

		try
		{
			while(true)
			{ 
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		finally 
		{ 
			listener.close();
		}
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
		
		private void receiveFile(String fileName) throws Exception {
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
			in.close();
			
			System.out.println("File is received");
			
			
		}
		
		public void run()
		{
			try
			{
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("Hello from server - you are client#" + clientNumber);
				try {
					receiveFile("./utils/server/NewFile.txt");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
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
