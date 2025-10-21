import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Server {
    private static ServerSocket listener;
    private static boolean isValidPort = false;
    private static boolean isValidIP = false;
    private static String address = null;
    private static int port = 0;
    private final static String fileRootServer = "./utils/server/";

    public static void main(String[] args) throws Exception {
        int clientNumber = 0;

        checkParamsServer();

        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(address);

        listener.bind(new InetSocketAddress(serverIP, port));
        System.out.format("The server is running at %s:%d\n", address, port);

        try {
            while (true) {
                new ClientHandler(listener.accept(), clientNumber++).start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            listener.close();
        }
    }

    private static void checkParamsServer() {
        Scanner scanner = new Scanner(System.in);
        int maximumBytesIPAddress = 4;
        int highestIntegerValueOneByte = 255;
        int minimumPortNumber = 5000;
        int maximumPortNumber = 5050;

        while (!isValidIP) {
            System.out.print("Enter a valid IP address: ");
            address = scanner.nextLine();

            String[] bytes = address.split("\\.");
            int number = 0;

            try {
                if (bytes.length == maximumBytesIPAddress) {
                    for (int i = 0; i < maximumBytesIPAddress; ++i) {
                        number = Integer.parseInt(bytes[i]);
                        if (number < 0 || number > highestIntegerValueOneByte) {
                            throw new Exception("Each byte must be within 0 and 255 inclusively");
                        }
                    }
                    isValidIP = true;
                } else {
                    throw new Exception("The IP address must be only 4 byte long");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Please enter a valid IP format");
            }
        }

        while (!isValidPort) {
            System.out.print("Enter a valid listenig port number: ");

            try {
                port = scanner.nextInt();
                if (port >= minimumPortNumber && port <= maximumPortNumber) {
                    isValidPort = true;
                } else {
                    throw new Exception("The port number is not within 5000 and 5050");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Please enter a valid port number");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private int clientNumber;
        private SessionManager sessionManager;
        private String currentDirectory = Server.fileRootServer;

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            this.sessionManager = new SessionManager(socket);

            System.out.println("New connection with client#" + clientNumber + " at " + socket);
        }

        private boolean isPathSafe(String path) {
            try {
                String canonicalCurrent = new File(path).getCanonicalPath();
                String canonicalRoot = new File(Server.fileRootServer).getCanonicalPath();
                return canonicalCurrent.startsWith(canonicalRoot);
            } catch (IOException e) {
                return false;
            }
        }

        private String handleCD(String argument) throws IOException {
            if (argument.isEmpty()) {
                currentDirectory = Server.fileRootServer;
                return "Changed to root directory: " + currentDirectory;
            }

            currentDirectory = currentDirectory.replace("./", "");

            File newDir;
            if (argument.equals("..")) {
                File current = new File(currentDirectory);
                File parent = current.getParentFile();
                if (parent != null && isPathSafe(parent.getAbsolutePath())) {
                    currentDirectory = parent.getAbsolutePath() + File.separator;
                    return "Changed to parent directory: " + currentDirectory;
                } else {
                    return "Already at root directory";
                }
            } else {
                newDir = new File(currentDirectory, argument);
                if (newDir.exists() && newDir.isDirectory() && isPathSafe(newDir.getAbsolutePath())) {
                    currentDirectory = newDir.getAbsolutePath() + File.separator;
                    return "Changed directory to: " + currentDirectory;
                } else {
                    throw new IOException("Error: Directory does not exist or access denied");
                }
            }
        }

        private String handleLS() throws IOException {
            File dir = new File(currentDirectory);
            StringBuilder result = new StringBuilder();

            if (!dir.exists() || !dir.isDirectory()) {
                throw new IOException("Error: Directory does not exist");
            }

            File[] files = dir.listFiles();
            if (files == null) {
                throw new IOException("Error: Cannot read directory");
            }

            if (!currentDirectory.replace("./", "").equals(Server.fileRootServer.substring(2))) {
                currentDirectory = currentDirectory.replace("./", "");
            }

            result.append("Contents of ").append(currentDirectory).append(":\n");
            if (files.length == 0) {
                result.append("(empty directory)\n");
            } else {
                for (File file : files) {
                    if (file.isDirectory()) {
                        result.append("[DIR] ").append(file.getName()).append("/\n");
                    } else {
                        result.append("[FILE] ").append(file.getName())
                                .append(" (").append(file.length()).append(" bytes)\n");
                    }
                }
            }

            return result.toString();
        }

        private String handleMKDIR(String argument) throws IOException {
            if (argument.isEmpty()) {
                throw new IOException("Error: No directory name specified");
            }

            String cleanDirName = argument.replace("..", "").replace("/", "").replace("\\", "");

            File newDir = new File(currentDirectory, cleanDirName);
            if (newDir.exists()) {
                throw new IOException("Error: Directory '" + cleanDirName + "' already exists");
            }

            if (newDir.mkdir()) {
                return "Directory '" + cleanDirName + "' created successfully in " + currentDirectory;
            } else {
                throw new IOException("Error: Could not create directory '" + cleanDirName + "'");
            }
        }

        public void run() {
            try {
                communicateWithClient();
            } catch (IOException e) {
                System.out.println("Error handling client#" + clientNumber + " : " + e);
            }
        }

        private static String getDate() {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            return strDate;
        }

        private String getInfo() {
            return "[" + this.socket.getLocalAddress().getHostAddress() + ":" + this.socket.getLocalPort() + " - " + getDate() + "] : ";
        }

        private void communicateWithClient() throws IOException {
            boolean running = true;

            sessionManager.sendText("Welcome to the file server - you are client#" + clientNumber +
                    "\nCurrent directory: " + this.currentDirectory + "\nType exit to quit");

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

                    System.out.println(getInfo() + "" + command.toLowerCase() + (argument.isEmpty() ? "" : " " + argument));

                    try {
                        Command cmd = Command.fromString(command);
                        switch (cmd) {
                            case CD:
                                try {
                                    String cdResult = handleCD(argument);
                                    sessionManager.sendText(cdResult);
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                    sessionManager.sendText(e.getMessage());
                                }
                                break;
                            case LS:
                                try {
                                    String lsResult = handleLS();
                                    sessionManager.sendText(lsResult);
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                    sessionManager.sendText(e.getMessage());
                                }
                                break;
                            case MKDIR:
                                try {
                                    String mkdirResult = handleMKDIR(argument);
                                    sessionManager.sendText(mkdirResult);
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                    sessionManager.sendText(e.getMessage());
                                }
                                break;
                            case UPLOAD:
                                if (argument.isEmpty()) {
                                    System.out.println("Error: No file specified for upload");
                                    sessionManager.sendText("Error: No file specified for upload");
                                    break;
                                }
                                try {
                                    sessionManager.downloadFile(this.currentDirectory + argument);
                                    sessionManager.sendText("Uploading...");
                                    sessionManager.sendText("File " + argument + " has been successfully uploaded");
                                } catch (IOException e) {
                                    System.out.println("Error" + e.getMessage());
                                    sessionManager.sendText("Error : " + e.getMessage());
                                }
                                break;
                            case DOWNLOAD:
                                if (argument.isEmpty()) {
                                    System.out.println("Error: No file specified for upload");
                                    sessionManager.sendText("Error: No file specified for download");
                                    break;
                                }

                                File fileDownload = new File(this.currentDirectory + argument);
                                if (!fileDownload.exists()) {
                                    System.out.println("File not found");
                                    sessionManager.sendText("File not found");
                                    break;
                                }
                                sessionManager.sendText("Downloading...");

                                try {
                                    sessionManager.uploadFile(this.currentDirectory + argument);
                                    sessionManager.sendText("File " + argument + " has been successfully downloaded");
                                } catch (IOException e) {
                                    System.out.println("Error : " + e.getMessage());
                                    sessionManager.sendText("Error : " + e.getMessage());
                                }
                                break;
                            case DELETE:
                                File fileRef = new File(this.currentDirectory + argument);
                                try {
                                    if (!fileRef.exists()) {
                                        throw new IOException("File not found");
                                    }

                                    if (!fileRef.delete()) {
                                        throw new IOException("Unable to delete file");
                                    }
                                    sessionManager.sendText("Deleted file: " + argument);
                                } catch (IOException e) {
                                    System.out.println("Error : " + e.getMessage());
                                    sessionManager.sendText("Error : " + e.getMessage());
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
                    if (e.getMessage() == null) {
                        running = false;
                    }
                    System.out.println("Error : " + e.getMessage());
                }
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error : Could not close socket for client#" + clientNumber);
            }

            System.out.println("Connection with client#" + clientNumber + " closed");
        }
    }
}
