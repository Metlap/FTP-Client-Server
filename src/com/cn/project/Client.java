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
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	Socket requestSocket; // socket connect to the server
	ObjectOutputStream out; // stream write to the socket
	ObjectInputStream in; // stream read from the socket
	private DataOutputStream dos;
	private DataInputStream dis;
	String message;
	String MESSAGE;
	
	public static String SERVER_PORT = "8000";
	private static String FILE_PATH = "./resources/";

	public Client() {
	}

	void run(String serverPort) {

		int serverPortNumber = Integer.parseInt(serverPort);
		try {
			// create a socket to connect to the server
			requestSocket = new Socket("localhost", serverPortNumber);
			System.out.println("Connected to localhost in port :" + SERVER_PORT);
			// initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());

			System.out.println("Please input one of the following commands: ");
			System.out.println("get <filename>");
			System.out.println("upload <filename> ");
			System.out.println("exit ftpclient");

			try (// Reading commands from user input
					Scanner scanner = new Scanner(System.in)) {
				while (true) {

					String input = scanner.nextLine();

					// Send this input to server
					sendMessage(input);

					String[] splitStr = input.trim().split("\\s+");

					if (splitStr.length == 2 && (splitStr[0]).equals("get") && splitStr[1].length() > 0) {

						// Appending new to requested file name
						getFile("new" + splitStr[1]);
					}

					else if (splitStr.length == 2 && (splitStr[0]).equals("upload") && splitStr[1].length() > 0) {


						sendFile(splitStr[1]);
					}

					else if (splitStr.length == 2 && (splitStr[0]).equals("exit")
							&& (splitStr[1]).equals("ftpclient")) {

						// Exiting and closing by quitting the while loop
						break;

					} else {

						MESSAGE = (String) in.readObject();
						System.out.println("Please enter a valid command");

					}
				}
			}

		}

		catch (ClassNotFoundException e) {
			System.err.println("Class not found");
		} catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// Close connections
			try {
				in.close();
				out.close();
				if (dos != null)
					dos.close();
				if (dis != null)
					dis.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	// send a message to the output stream -> to let the server know of the command
	// input by user in client
	void sendMessage(String msg) {
		try {
			// stream write the message
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message: " + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	void sendFile(String fileName) {

		String filePath = FILE_PATH + fileName;

		System.out.println("Sending file to the server");

		// Make file chunks of size 1kb and send to the server
		int totalBytesTransferred = 0;
		File file = new File(filePath);
		try {
			
			dos = new DataOutputStream(requestSocket.getOutputStream());
			
			if (file.exists()) {
				FileInputStream fileInputStream = new FileInputStream(file);
				
				// Getting the size of the file and sending it to server
				dos.writeLong(file.length());
				
				dos.flush();
				
				System.out.println("Length is ::" + file.length());

				// Read data into buffer array
				byte[] buffer = new byte[1024];
				// -1 -> EOF
				while ((totalBytesTransferred = fileInputStream.read(buffer)) != -1) {

					// Writing sub arrays to include case of last chunk where size could be less
					// than 1kb
					dos.write(buffer, 0, totalBytesTransferred);
					dos.flush();
				}
				fileInputStream.close();

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
			System.out.println("Other than File not found");
		}

	}

	void getFile(String fileName) {

		try {

			String filePath = FILE_PATH + fileName;
			File file = new File(filePath);

			dis = new DataInputStream(requestSocket.getInputStream());

			long size = dis.readLong();
			if (size == 0L) {
				System.out.println("File not found at the given directory");
				return;
			}
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			int bytesRead = 0;
			byte[] buffer = new byte[1024];

//			while (size >= 0 && (bytesRead = dis.read(buffer)) != -1) {

			while (size > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
				// Here we write the file using write method
				fileOutputStream.write(buffer, 0, bytesRead);
				size -= bytesRead; // read upto file size
				fileOutputStream.flush();
			}

//			fileOutputStream.close();

			System.out.println("======File Receive success======");
		}

		catch (

		FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found");
			return;
		} catch (IOException e) {
			e.printStackTrace();

			System.out.println("Exception other than File not found");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

	// main method
	public static void main(String args[]) {

		String serverPort, ftpClient;

		System.out.println("Enter command to start the client socket");
		System.out.println("Command : ftpclient <portnumber>");

		while (true) {

			Scanner scanner = new Scanner(System.in);
			String input = scanner.nextLine();
			String[] splitStr = input.trim().split("\\s+");

			if (splitStr.length != 2) {
				System.out.println("Enter valid command to start the client socket");
				continue;
			}

			ftpClient = splitStr[0];
			serverPort = splitStr[1];

			// Break loop in case of valid input to connect to server
			if (ftpClient.equals("ftpclient") && serverPort.equals(SERVER_PORT))
				break;

			else {
				System.out.println("Enter valid port number - 8000");
				continue;
			}
		}

		Client client = new Client();
		client.run(serverPort);
	}

}
