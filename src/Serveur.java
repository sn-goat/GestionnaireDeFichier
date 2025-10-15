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
		System.out.format("The server is running at %s:%d\n", address, port);

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
		private SessionManager sessionManager;

		public ClientHandler(Socket socket, int clientNumber) 
		{ 
			this.socket = socket;
			this.clientNumber = clientNumber;
			this.sessionManager =  new SessionManager(socket);

			System.out.println("New connection with client#" + clientNumber + " at " + socket);
		}
		
		
		
		public void run()
		{
			try
			{
					communicateWithClient();
			}
			catch (IOException e)
			{
				System.out.println("Error handling client#" + clientNumber + " : " + e);
			}
		}
		
		
		    
		    
		private void communicateWithClient() throws IOException {
		    boolean running = true;
		    String fileRootServer = "./utils/server/"; // temporary 
		    
		    sessionManager.sendText("Welcome to the file server - you are client#" + clientNumber + ". Type exit to quit.");
		    
		    while (running) {
		        try {
		            String input = sessionManager.receiveText();
		            
		            String command = "";
		            String argument = "";
		            
		            int spaceIndex = input.indexOf(' ');
		            if (spaceIndex > 0) {
		                command = input.substring(0, spaceIndex);
		                argument = input.substring(spaceIndex + 1).trim();
		            } else {
		                command = input.trim();
		            }
		            
		            System.out.println("Client#" + clientNumber + " sent command: " + command.toLowerCase() + 
		                               (argument.isEmpty() ? "" : " with argument: " + argument));
		            
		            try {
		                Command cmd = Command.fromString(command);
		                switch (cmd) {
		                    case CD:
		                        sessionManager.sendText("Changed directory to: " + argument);
		                        break;
		                    case LS:
		                        sessionManager.sendText("Directory listing would be shown here");
		                        break;
		                    case MKDIR:
		                        sessionManager.sendText("Created directory: " + argument);
		                        break;
		                    case UPLOAD:
		                        if (argument.isEmpty()) {
		                            sessionManager.sendText("Error: No file specified for upload");
		                            continue;
		                        }
		                        sessionManager.downloadFile(fileRootServer + argument);
		                        sessionManager.sendText("File " + argument + " has been successfully uploaded");
		                        break;
		                    case DOWNLOAD:
		                        if (argument.isEmpty()) {
		                            sessionManager.sendText("Error: No file specified for download");
		                            continue;
		                        }
		                        sessionManager.uploadFile(fileRootServer + argument);
		                        sessionManager.sendText("File " + argument + " has been successfully downloaded");
		                        break;
		                    case DELETE:
		                    	if (argument.isEmpty()) {
		                    		sessionManager.sendText("Error: No file or directory specified for deletion");
		                    		break;
		                    	}
		                    	
		                    	File fileRef = new File(fileRootServer + argument);
		                    	try {
		                    		if (!fileRef.exists()) { throw new IOException("File not Found!"); }
		                    		if (!fileRef.delete()) { throw new IOException("Unable to delete File"); }
		                    		sessionManager.sendText("Deleted file: " + argument);
		                    	} catch (IOException e) {
		                    		sessionManager.sendText("Server error : " + e.getMessage());
		                    	}
		                        break;
		                    case EXIT:
		                        sessionManager.sendText("Goodbye from the server");
		                        running = false;
		                        break;
		                }
		            } catch (IllegalArgumentException e) {
		                sessionManager.sendText("Error: Unknown command: " + command);
		            }
		        } catch (IOException e) {
		            System.out.println(e.getMessage());
		        }
		    }
		    
		    try {
		        socket.close();
		    } catch (IOException e) {
		        System.out.println("Could not close socket for client#" + clientNumber);
		    }
		    System.out.println("Connection with client#" + clientNumber + " closed");
		}
		
		
	}
}
