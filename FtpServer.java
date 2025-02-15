import java.net.*;
import java.io.*;
import java.nio.file.Files;

/**
 * The FTp Server is implemented in this class
 */
public class FtpServer {

    //the port at which the server will be listening
    private static final int serverPort = 4007;

    public static void main(String[] args) throws Exception {
        int clientNumber = 0;   // variable that counts and assigns IDs to clients
        System.out.println("The server is now up and running!");
        ServerSocket serverSocket = new ServerSocket(serverPort);

        //connecting to the client
        try {
            while (true) {
                clientNumber++;
                new ClientThread(serverSocket.accept(), clientNumber).start();
                System.out.println("Client " + clientNumber + " is connected to the server!");
            }
        } finally {
            serverSocket.close();
        }
    }

    /**
     * The ClientThread handles the requests from the FTP Client
     */
    private static class ClientThread extends Thread {
        private String msgFromClient;    //message received from the client
        private final Socket serverSocket;    //socket that listens to client
        ObjectOutputStream outputStream = null; // stream to write to the socket
        ObjectInputStream inputStream = null; //stream to read from socket
        int clientNumber;

        String currentDirectory = "./testFiles/";

        public ClientThread(Socket serverSocket, int clientNumber) {
            this.serverSocket = serverSocket;
            this.clientNumber = clientNumber;
        }

        /**
         * main running method of client thread of the server
         */
        public void run() {
            try {
                //initializing input and output streams
                outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(serverSocket.getInputStream());

                //initializing server working directory
                File folder = new File(currentDirectory);

                try {
                    while (true) {
                        //receiving the message from client and processing input
                        msgFromClient = (String) inputStream.readObject();
                        System.out.println("Message from client: " + msgFromClient);
                        if(msgFromClient.equals("exit")) {
                            System.out.println("Client "+ clientNumber + " has closed connection.");
                            break;
                        }
                        String[] splitCommand = msgFromClient.split("\\s+");

                        switch (splitCommand[0]) {
                            //download file to server
                            case "get" -> getFile(splitCommand[1], folder);
                            //receive file uploaded to server
                            case "upload" -> uploadFile(splitCommand[1]);
                            default -> System.out.println("ERROR - Command not found");
                        }
                    }
                } catch (Exception exception) {
                    System.err.println("ERROR - failed to receive information from client");
                }
            } catch (Exception exception) {
                System.out.println("ERROR - Disconnecting from all clients...");
            } finally {
                //closing all connections
                try {
                    inputStream.close();
                    outputStream.close();
                    serverSocket.close();
                } catch (IOException ioException) {
                    System.out.println("ERROR - Couldn't close all connections!");
                }
            }
        }

        /**
         * sends message to client via socket output stream
         * @param message String message to be sent to client
         * @throws IOException upon invalid message parsing
         */
        public void sendMessage(String message) throws IOException {
            System.out.println("Sending message to client: " + message);
            outputStream.writeObject(message);
            outputStream.flush();
        }

        /**
         * sends file to the socket output stream
         * @param file the file from server that is sent to client
         * @throws IOException upon invalid output stream activity
         */
        public void sendFile(File file) throws IOException {
            byte[] content = Files.readAllBytes(file.toPath());
            outputStream.writeObject(content);
            outputStream.flush();
            System.out.println("Sending file " + file.getName() + "to client");
        }

        /**
         * sends file queried by client using the "get <file_name>" command to client
         * @param fileName the name of the file requested by client
         * @param folder the directory where server files are stored
         * @throws IOException upon invalid output stream activity
         */
        public void getFile(String fileName, File folder) throws IOException {
            boolean fileFoundFlag = false;
            File[] listOfFiles = folder.listFiles();
            if(listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.getName().equals(fileName)) {
                        System.out.println("File " + file.getName() + " found!");
                        fileFoundFlag = true;
                        sendFile(file);
                    }
                }
            }
            if (!fileFoundFlag) {
                System.out.println("ERROR - File not found");
                outputStream.write(0);
                outputStream.flush();
            }
        }

        /**
         * receive file uploaded by client and store it in working directory
         * @param fileName name under which uploaded file must be saved in server
         * @throws IOException upon invalid output stream activity
         * @throws ClassNotFoundException upon unknown object class being read
         */
        public void uploadFile(String fileName) throws IOException, ClassNotFoundException {
            //signals client that server is ready for file upload
            sendMessage("Ready");

            //receives file found message from client
            msgFromClient = (String) inputStream.readObject();
            if(msgFromClient.equals("found"))   {
                //reads file from server input stream
                byte[] content = (byte[]) inputStream.readObject();
                File file = new File(currentDirectory + "new" + fileName);
                Files.write(file.toPath(), content);
                System.out.println("Received file " + file.getName() + " successfully!");
            } else {
                System.out.println("ERROR - failed to receive file from client");
            }
        }
    }
}
