								CN Project - 1
							Venkata Sai Karthik Metlapalli
								UFID - 65764476
								
								
Description:

• This project involves java-based implementation of FTP client-server software using TCP sockets.
• The zipped folder includes the source codes, and the input and transferred files in the resources folder.
• The implementation consists of 1 server and 1 client running on the same host machine for file transfer. This could be extended to work when both client and server are running on different hosts. And the server code could be modified to impliment client handler for multiple concurrent client connections.

Steps to run:

- Open the src folder of CN-Project-1 folder, and start the server first by using the below commands to complie and start respectively:

•javac Server.java 
•java Server

- Then start the client by using the below commands to complie and start respectively:

•javac Client.java 
•java Client

- Then we need to connect this client socket to the server, using the below command as user input:

•ftpclient <port number> 

Portnumber is hardcoded as 8000 as a class level variable.

After this, once the serversocket accepts the incoming client connection, we could use one of the below commands as user input to client program to perform file transfer and terminate operations:

1. get <filename>
2. upload <filename>
3. exit ftpclient

The path for file download and search is the resources folder of the project.

Once a user input is done, a check is done if it is a valid command. If it is valid, we use the objectoutput stream to write the same command as message to the output stream of the client socket (sendMessage() method does this). This would be read from the input stream at the server socket's end to identify the user input. Then corresponding methods for sending and reading are performed at both client and server's end.

Once the chunked file transfer is complete, then the client waits for next user input for subsequent file transfers.

We also send a FILE NOT FOUND message if the specified file is not found in the given directory(resourses folder) here.

The file is sent and read as chunks using the DataOutput and DataInput Streams. Closing these would close the socket's input and output streams. And hence, these are closed only when the exit ftpclient command is entered. Additionally, the chunks of 1Kb (buffered outputbytes) are written to output stream as soon as they are received using the flush() command.

Both the server and client programs are terminated when user inputs the exit command.