import java.net.*;
import java.io.*;
import java.nio.file.Files;

/**
 * The FTp Server is implemented in this class
 */
public class FtpServer {

    //the port at which the server will be listening
    private static final int serverPort = 5106;

    public static void main(String[] args) throws Exception {
        System.out.println("The server is now up and running!");
        ServerSocket serverSocket = new ServerSocket(serverPort);

        //connecting to the client
        try {
            while (true) {
                new ClientThread(serverSocket.accept()).start();
                System.out.println("Client is connected to the server!");
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
        private Socket serverSocket;    //socket that listens to client
        ObjectOutputStream outputStream = null; // stream to write to the socket
        ObjectInputStream inputStream = null; //stream to read from socket

        public ClientThread(Socket serverSocket) {
            this.serverSocket = serverSocket;
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

                //initializing server directory properties
                String currentDirectory = new java.io.File(".").getCanonicalPath();
                File folder = new File(currentDirectory);

                try {
                    while (true) {
                        //receiving the message from client and processing input
                        msgFromClient = (String) inputStream.readObject();
                        System.out.println("Message from client: " + msgFromClient);
                        String[] splitCommand = msgFromClient.split("\\s+");

                        switch (splitCommand[0])   {
                            //download file to server
                            case "get":
                                getFile(splitCommand[1], folder);
                                break;

                            //receive file uploaded to server
                            case "upload":
                                uploadFile(splitCommand[1]);
                                break;

                            default:
                                System.out.println("ERROR - Command not found");
                                break;
                        }
                    }
                } catch (Exception exception) {
                    System.err.println("ERROR - failed to receive information from client");
                }
            } catch (Exception exception) {
                System.out.println("Disconnecting from client...");
            } finally {
                //closing all connections
                try {
                    inputStream.close();
                    outputStream.close();
                    serverSocket.close();
                } catch (IOException ioException) {
                    System.out.println("Disconnected from client!");
                }
            }
        }

        /**
         * sends message to client via socket output stream
         * @param message
         * @throws IOException
         */
        public void sendMessage(String message) throws IOException {
            System.out.println("Sending message to client: " + message);
            outputStream.writeObject(message);
            outputStream.flush();
        }

        /**
         * sends file to the socket output stream
         * @param file
         * @throws IOException
         */
        public void sendFile(File file) throws IOException {
            byte[] content = Files.readAllBytes(file.toPath());
            outputStream.writeObject(content);
            outputStream.flush();
            System.out.println("Sending file " + file.getName() + "to client");
        }

        /**
         * sends file queried by client using the "get <file_name>" command to client
         *
         * @param fileName
         * @param folder
         * @throws IOException
         */
        public void getFile(String fileName, File folder) throws IOException {
            boolean fileFoundFlag = false;
            File[] listOfFiles = folder.listFiles();
            for (File file : listOfFiles) {
                if (file.getName().equals(fileName)) {
                    System.out.println("File " + file.getName() + " found!");
                    fileFoundFlag = true;
                    sendFile(file);
                }
            }
            if (fileFoundFlag == false) {
                System.out.println("ERROR - File not found");
                outputStream.write(0);
                outputStream.flush();
            }
        }

        public void uploadFile(String fileName) throws IOException, ClassNotFoundException {
            //signals client that server is ready for file upload
            sendMessage("Ready");

            //receives message from client
            msgFromClient = (String) inputStream.readObject();
            if(msgFromClient.equals("found"))   {
                //reads file from server input stream
                byte[] content = (byte[]) inputStream.readObject();
                File file = new File("./" + fileName);
                Files.write(file.toPath(), content);
                System.out.println("Received file " + file.getName() + " successfully!");
            } else {
                System.out.println("ERROR - failed to receive file from client");
            }
        }
    }
}
