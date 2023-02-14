import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class FtpClient {
    public Socket clientSocket = null;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;
    public String userInputCommand;
    private String currentDirectory;
    File folder = null;
    File[] listOfFiles = null;
    private boolean isConnected = false;

    public static void main(String[] args) {
        FtpClient ftpClient = new FtpClient();
        ftpClient.runClient();
    }

    /**
     * function that processes user input and manages client functionality
     */
    void runClient() {
        try {
            //initialisation
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            currentDirectory = new java.io.File(".").getCanonicalPath();
            folder = new File(currentDirectory);

            //processing user input
            while (true) {
                try {
                    //receiving user input command
                    System.out.print("Enter your FTP command here:");
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
                                        System.out.println("ERROR: Unable to connect to server!");
                                        //todo - log this
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
                                        getFile(splitCommand[1]);
                                    } catch (Exception e) {
                                        System.out.println("Unable to get file from server");
                                        //todo - log
                                        e.printStackTrace();
                                    }
                                    break;
                                } else {
                                    System.out.println("CAUTION: Server not connected. Try connecting to the server using the command \"ftpclient <port_number>\"");
                                }
                                break;

                            case "upload":
                                if (isConnected) {
                                    try {
                                        uploadFile(splitCommand[1]);
                                    } catch (Exception e) {
                                        System.out.println("Unable to upload file to server");
                                        //todo - log
                                        e.printStackTrace();
                                    }
                                    break;
                                } else {
                                    System.out.println("CAUTION: Server not connected. Try connecting to the server using the command \"ftpclient <port_number>\"");
                                }
                                break;

                            default:
                                System.out.println("Command does not exist, please retry.");
                                break;
                        }
                    } else {
                        System.out.println("Invalid number of arguments encountered, please retry.");
                    }
                } catch (Exception e) {
                    System.out.println("Command does not exist, please retry");
                    //todo - log this
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Unexpected error occurred: client shutting down.");
            //todo - log this
            e.printStackTrace();
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
     * connects to the server at localhost:portNumber
     * initializes object streams
     *
     * @param portNumber
     * @throws IOException
     */
    private void connectToServer(int portNumber) throws IOException {
        clientSocket = new Socket("127.0.0.1", portNumber);

        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(clientSocket.getInputStream());

        System.out.println("Now connected to server at: " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
    }

    /**
     * sends message to the output stream
     *
     * @param message
     * @throws IOException
     */
    void sendMessage(String message) throws IOException {
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
    private void getFile(String fileName) throws IOException, ClassNotFoundException {
        sendMessage(fileName);
        if (inputStream.read() == 0) {
            System.out.println("CAUTION: File does not exist in server!");
        } else {
            File file = new File("./" + fileName);
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
    private void uploadFile(String fileName) throws IOException, ClassNotFoundException {
        sendMessage(fileName);

        //flag to keep track of whether the file is found in the server or not
        boolean flag = false;

        String response = (String) inputStream.readObject();
        if (response.equals("Ready")) {
            listOfFiles = folder.listFiles();
            for (File file : listOfFiles) {
                if (file.getName().equals(fileName)) {
                    System.out.println("Found file " + file.getName() + "in server; commencing download.");
                    flag = true;
                    sendMessage("found");

                    //todo - divide by 100 bytes
                    byte[] content = Files.readAllBytes(file.toPath());
                    outputStream.writeObject(content);
                    outputStream.flush();
                    System.out.println("File " + file.getName() + " uploaded to server successfully!");
                }
            }
            if (flag == false) {
                System.out.println("ERROR - File not found in server");
                sendMessage("notfound");
            }
        }
    }
}
