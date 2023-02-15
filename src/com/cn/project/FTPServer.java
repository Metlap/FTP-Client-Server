package com.cn.project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer {
	
	int serverPort = 8000; // The server will be listening on this port number
	ServerSocket serverSocket; // serversocket used to lisen on port number 8000
	Socket connection = null; // socket for the connection with the client
	ObjectOutputStream out; // stream write to the socket
	ObjectInputStream in; // stream read from the socket
	DataInputStream dis;
	DataOutputStream dos;
	
	private static String FILE = "./resources/";
	
	public FTPServer() {
	}
	
	void run() {
		try {

			serverSocket = new ServerSocket(serverPort, 10);

			System.out.println("Waiting for connection");

			connection = serverSocket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());

			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());

			while (true) {
				String message = (String) in.readObject();

				String[] splitStr = message.trim().split("\\s+");

				if (splitStr.length == 2 && (splitStr[0]).equals("upload") && splitStr[1].length() > 0) {
					receiveFile("new" + splitStr[1]);
				}

				else if (splitStr.length == 2 && (splitStr[0]).equals("get") && splitStr[1].length() > 0) {

					sendFile(splitStr[1]);
				}

				else if (splitStr.length == 2 && (splitStr[0]).equals("exit") && (splitStr[1]).equals("ftpclient")) {
					break;

				} else {

					System.out.println("Please enter a valid command");
					sendMessage("wrong command");

				}

			}


		} catch (Exception e) {
			System.err.println("Data received in unknown format");
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
				if(dos != null )dos.close();
				if (dis != null)dis.close();
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("Sent message: " + msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void receiveFile(String fileName) {

		try {

			String filePath = FILE + fileName;
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
				
				System.out.println("======File Receive success======");
			

		}  
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	void sendFile(String fileName) {

		String filePath = FILE + fileName;

		System.out.println("Sending file to the client");

		
		int totalBytesTransferred = 0;

		try {
			File file = new File(filePath);
			dos = new DataOutputStream(connection.getOutputStream());
			if (file.exists()) {
				FileInputStream fileInputStream = new FileInputStream(file);
			
				
				dos.writeLong(file.length());

				System.out.println("Length is ::" + file.length());
				dos.flush();

				
				byte[] buffer = new byte[1024];
				
				while ((totalBytesTransferred = fileInputStream.read(buffer)) != -1) {
					dos.write(buffer, 0, totalBytesTransferred);
					dos.flush();
				}
				System.out.println("======File Send success======");
				
			} else {
				dos.writeLong(0L);
				dos.flush();
				System.out.println("File not found at the given directory");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		FTPServer s = new FTPServer();
		s.run();
	}
}
