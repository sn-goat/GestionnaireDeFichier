import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client {
    private static Socket socket;
    private static SessionManager sessionManager;
    private static boolean isValidPort = false;
    private static boolean isValidIP = false;
    private static String address = null;
    private static int port = 0;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            checkParamsServer(scanner);
            
            socket = new Socket(address, port); 
            sessionManager = new SessionManager(socket);
            
            String helloMessageFromServer = sessionManager.receiveText();
            System.out.println(helloMessageFromServer);
            
            communicateWithServer(scanner);
            
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            scanner.close();
        }
    }
    
    private static void checkParamsServer(Scanner scanner) {
        while(!isValidIP) {
            System.out.print("Enter a valid IP address: ");
            address = scanner.nextLine();
        
            String[] bytes = address.split("\\.");
            int number = 0;
            
            try {
                if(bytes.length == 4) {
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
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Please enter a valid IP format.");
            }
        }
        
        while(!isValidPort) {
            System.out.print("Enter a valid listening port number: ");
            try {
                port = scanner.nextInt();
                scanner.nextLine();
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
    }
    
    private static void communicateWithServer(Scanner scanner) throws IOException {
        System.out.print(getInfo());
        
        String command = "";
        String argument = "";
        String fileRootClient = "./utils/client/";  // temporary 
        while (true) {
            String input = scanner.nextLine();
            Scanner lineScanner = new Scanner(input);
            
            command = lineScanner.hasNext() ? lineScanner.next() : "";
            argument = lineScanner.hasNext() ? lineScanner.nextLine().trim() : "";
            lineScanner.close();
            
            try {
                Command cmd = Command.fromString(command);
                switch (cmd) {
                    case CD:
                        System.out.println("Change directory");
                        break;
                    case LS:
                        System.out.println("List directory");
                        break;
                    case MKDIR:
                        System.out.println("Make directory");
                        break;
                    case UPLOAD:
                        try {
                            if (argument.isEmpty()) {
                                System.out.println("You must enter a file to upload.");
                                break;
                            }
                            sessionManager.sendText(cmd.name() + " " + argument);
                            sessionManager.uploadFile(fileRootClient + argument);
                            System.out.println(sessionManager.receiveText());
                        } catch (IOException e) {
                            System.out.println("Error uploading file: " + e.getMessage());
                        }
                        break;
                    case DOWNLOAD:
                        try {
                            if (argument.isEmpty()) {
                                System.out.println("You must enter a file to download.");
                                break;
                            }
                            sessionManager.sendText(cmd.name() + " " + argument);
                            sessionManager.downloadFile(fileRootClient + argument);
                            System.out.println(sessionManager.receiveText());
                        } catch (IOException e) {
                            System.out.println("Error downloading file: " + e.getMessage());
                        }
                        break;
                    case DELETE:
                        System.out.println("Delete file");
                        break;
                    case EXIT:
                        try {
                            sessionManager.sendText(cmd.name());
                            System.out.println(sessionManager.receiveText());
                        } catch (IOException e) {
                            System.out.println("Error sending exit command: " + e.getMessage());
                        }
                        return;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown command: " + command);
            }
            
            System.out.print(getInfo());
        }
    }
    
    private static String getDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
    
    private static String getInfo() {
        return "[" + socket.getLocalAddress().getHostAddress()+ ":" + socket.getLocalPort() + " - " + getDate() + "] : ";
    }
}