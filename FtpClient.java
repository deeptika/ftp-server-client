import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

/**
 * A FTP Client is implemented in this class
 */
public class FtpClient {
    public Socket clientSocket = null;  //socket that listens to server
    ObjectOutputStream outputStream = null; // stream to write to the socket
    ObjectInputStream inputStream = null; //stream to read from socket
    public String userInputCommand; //the FTP command that the user inputs
    File folder = null; //represents the working directory of the client
    private boolean isConnected = false; //represents if client is connected to server or not
    String currentDirectory = "./";

    public static void main(String[] args) {
        FtpClient ftpClient = new FtpClient();
        ftpClient.runClient();
    }

    /**
     * function that processes user input and manages client functionality
     */
    void runClient() {
        try {
            //initialization
            System.out.println("The client is running now!");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            folder = new File(currentDirectory);

            //processing user input
            outerloop:
            while (true) {
                try {
                    System.out.println("Available client commands:\n1.\tftpclient <server port number>: To connect to the server\n2.\tget <fileName>: To download a file from the server\n3.\tupload <fileName>: To upload a file to the server\n4.\texit <server port number>: Ends the client program");
                    //receiving user input command
                    System.out.print("Enter your FTP command here: ");
                    userInputCommand = bufferedReader.readLine();
                    String[] splitCommand = userInputCommand.split("\\s+");

                    //processing command and rerouting to the proper functionality
                    if (splitCommand.length == 2) {
                        switch (splitCommand[0]) {
                            //for command "ftpclient <port_number>"
                            case "ftpclient":
                                if (!isConnected()) {
                                    int portNumber = Integer.parseInt(splitCommand[1]);
                                    try {
                                        connectToServer(portNumber);
                                    } catch (Exception e) {
                                        System.err.println("ERROR: Unable to connect to server!");
                                        e.printStackTrace();
                                    }
                                } else {
                                    System.out.println("Already connected to server!");
                                }
                                break;

                            //for command "get <file_name>"
                            case "get":
                                if (isConnected) {
                                    try {
                                        getFile(userInputCommand, splitCommand[1]);
                                    } catch (Exception e) {
                                        System.err.println("Unable to get file from server");
                                        e.printStackTrace();
                                    }
                                    break;
                                } else {
                                    System.err.println("CAUTION: Server not connected. Try connecting to the server using the command \"ftpclient <port_number>\"");
                                }
                                break;

                            //for command "upload <file_name>"
                            case "upload":
                                if (isConnected) {
                                    try {
                                        uploadFile(userInputCommand, splitCommand[1]);
                                    } catch (Exception e) {
                                        System.err.println("Unable to upload file to server");
                                        e.printStackTrace();
                                    }
                                    break;
                                } else {
                                    System.err.println("CAUTION: Server not connected. Try connecting to the server using the command \"ftpclient <port_number>\"");
                                }
                                break;

                            //for command "exit <port_number>"
                            case "exit":
                                break outerloop;

                            default:
                                System.err.println("Command does not exist, please retry.");
                                break;
                        }
                    } else {
                        System.out.println("Invalid number of arguments encountered, please retry.");
                    }
                } catch (Exception e) {
                    System.out.println("Error encountered when reading command, please retry");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Unexpected error occurred: client shutting down.");
            setConnected(false);
            e.printStackTrace();
        } finally {
            System.out.println("Client shutting down...");
        }
    }

    /**
     * checks if the client is connected to the server or not
     *
     * @return
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * sets connected value
     * @param connected
     */
    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    /**
     * connects to the server at localhost:portNumber
     * initializes object streams
     *
     * @param portNumber
     * @throws IOException
     */
    private void connectToServer(int portNumber) throws IOException {
        clientSocket = new Socket("127.0.0.1", portNumber);

        //initializing input and output streams
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        setConnected(true);

        System.out.println("Now connected to server at: " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
    }

    /**
     * sends message to the output stream
     *
     * @param message
     * @throws IOException
     */
    void sendMessage(String message) throws IOException {
        System.out.println("Sending message to server: " + message);
        outputStream.writeObject(message);
        outputStream.flush();
    }

    /**
     * downloads/gets file from server
     *
     * @param fileName
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void getFile(String command, String fileName) throws IOException, ClassNotFoundException {
        sendMessage(command);
        if (inputStream.read() == 0) {
            System.out.println("CAUTION: File does not exist in server!");
        } else {
            File file = new File(currentDirectory + "new" + fileName);
            byte[] content = (byte[]) inputStream.readObject();
            Files.write(file.toPath(), content);
            System.out.println("File " + fileName + ": get is successful.");
        }
    }

    /**
     * uploads file to server
     *
     * @param fileName
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void uploadFile(String command, String fileName) throws IOException, ClassNotFoundException {
        sendMessage(command);

        //flag to keep track of whether the file is found in the server or not
        boolean flag = false;

        String response = (String) inputStream.readObject();

        if (response.equals("Ready")) {
            System.out.println("Prepping for file upload...");
            File [] listOfFiles = folder.listFiles();
            for (File file : listOfFiles) {
                if (file.getName().equals(fileName)) {
                    System.out.println("Found file " + file.getName() + " in working directory, commencing upload.");
                    sendMessage("found");
                    flag = true;
                    byte[] content = Files.readAllBytes(file.toPath());
                    outputStream.writeObject(content);
                    System.out.println("File " + file.getName() + " uploaded to server successfully!");
                }
            }
            if (!flag) {
                System.out.println("ERROR - File not found in server");
                sendMessage("notfound");
            }
        }
    }
}
