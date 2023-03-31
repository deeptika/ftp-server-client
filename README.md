# cn-project-1
Implementation of FTP Client and Server in Java

Purpose:
CNT-5106 Computer Networks - Project 2 - University of Florida

Description:
This project contains an implementation of the File Transfer Protocol (FTP) in Java between a client and a server. 
The FTP server can handle multiple clients and client requests simultaneously.
When the FTP client connects to the FTP server, it should be able to upload a file to the server or download a file from the server depending on the user's input to the client.
In this project, the FTP server is hosted at localhost, port 4007. The file directories of both server and client are hosted in /testFiles directory.

Files:

1. FtpClient.java: The FTP client class that obtains user commands and interacts with the server to upload/download files.

2. FtpServer.java: The FTP server class that handles multiple client requests for file transfers.

3. uploadTestFile1.pptx, uploadTestFile2.pptx: Test files for the upload functionality.

4. downloadTestFile1.pptx, downloadTestFile2.pptx: Test files for the download functionality.

Running the project:

1. Compile both the server and client files using the following commands: javac FtpServer.java, javac FtpClient.java
2. Run both server and client using the following commands: java FtpServer, java FtpClient. Multiple instances of the client can be run at the same time.
3. Once the client comes up, it will prompt the user for any FTP command. The first command to be executed should be to connect the client with the server. This can be done using the following statement: ftpclient <port_number> (the port number must correspond to the por where the server is hosted, i.e. 5106).
4. Test upload functionality in the client using the command: upload <file_name>
5. Test download functionality in the client using the command: get <file_name>
6. Exit the client program using the command: exit