package com.cn.project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	int serverPort = 8000; // The server will be listening on this port number
	ServerSocket serverSocket; // serversocket used to lisen on port number 8000
	Socket connection = null; // socket for the connection with the client
	String message; // message received from the client
	String MESSAGE; // uppercase message send to the client
	ObjectOutputStream out; // stream write to the socket
	ObjectInputStream in; // stream read from the socket
	DataInputStream dis;
	DataOutputStream dos;

	private static String FILE_PATH = "./resources/";
	
	public Server() {
	}

	void run() {
		try {
			// create a serversocket
			serverSocket = new ServerSocket(serverPort, 10);
			// Wait for connection
			System.out.println("Waiting for connection");
			// accept a connection from the client
			connection = serverSocket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());
			// initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());

			while (true) {
				
//				connection.setSoTimeout(900000);
				message = (String) in.readObject();

				String[] splitStr = message.trim().split("\\s+");

				if (splitStr.length == 2 && (splitStr[0]).equals("upload") && splitStr[1].length() > 0) {

					// Appending new to requested file name
					receiveFile("new" + splitStr[1]);
				}

				else if (splitStr.length == 2 && (splitStr[0]).equals("get") && splitStr[1].length() > 0) {

					sendFile(splitStr[1]);
				}

				else if (splitStr.length == 2 && (splitStr[0]).equals("exit") && (splitStr[1]).equals("ftpclient")) {

					// Exiting and closing by quitting the while loop
					break;

				} else {

					System.out.println("Please enter a valid command");
					sendMessage("wrong command");

				}

			}


		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Data received in unknown format");
			e.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// Close connections
			try {
				in.close();
				out.close();
				if(dos != null )dos.close();
				if (dis != null)dis.close();
				connection.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	// send a message to the output stream
	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("Sent message: " + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	void receiveFile(String fileName) {

		try {

			String filePath = FILE_PATH + fileName;
			File file = new File(filePath);
			dis = new DataInputStream(connection.getInputStream());
			
			long size = dis.readLong();
			if (size == 0L) {
				System.out.println("File not found at the given directory");
				return;
			}
			
			FileOutputStream fileOutputStream = new FileOutputStream(file);

				int bytesRead = 0;
				byte[] buffer = new byte[1024];

				while (size > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
					fileOutputStream.write(buffer, 0, bytesRead);
					size -= bytesRead; // read upto file size
					fileOutputStream.flush();
				}
//				fileOutputStream.close();
				
				System.out.println("======File Receive success======");
			

		} 

		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exception other than File not found");
			e.printStackTrace();
		}

	}

	void sendFile(String fileName) {

		String filePath = FILE_PATH + fileName;

		System.out.println("Sending file to the client");

		// Make file chunks of size 1kb and send to the server
		int totalBytesTransferred = 0;

		try {
			File file = new File(filePath);
			dos = new DataOutputStream(connection.getOutputStream());
			if (file.exists()) {
				FileInputStream fileInputStream = new FileInputStream(file);
			
				// Getting the size of the file and sending it to server
				dos.writeLong(file.length());

				System.out.println("Length is ::" + file.length());
				dos.flush();

				// Read data into buffer array
				byte[] buffer = new byte[1024];
				// -1 -> EOF
				while ((totalBytesTransferred = fileInputStream.read(buffer)) != -1) {

					// Writing sub arrays to include case of last chunk where size could be less
					// than 1kb
					dos.write(buffer, 0, totalBytesTransferred);
					dos.flush();
				}
//				fileInputStream.close();
				System.out.println("======File Send success======");
				
			} else {
				dos.writeLong(0L);
				dos.flush();
//				dos.close();
				System.out.println("File not found at the given directory");
				return;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("File not found in the specified path");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Other than File not found");
		}

	}

	public static void main(String args[]) {

		Server s = new Server();
		s.run();

	}

}
