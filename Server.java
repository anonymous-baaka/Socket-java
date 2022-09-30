// A Java program for a Server
import java.net.*;
import java.io.*;

class MyServerClientThread extends Thread{
	int clientID;
	int result;
	Socket s_clientSocket;
	private DataOutputStream out=null;
	MyServerClientThread(Socket _clientSocket, int _clientID){
		s_clientSocket=_clientSocket;
		clientID=_clientID;
	}
	@Override
	public void run(){
		try
		{
			DataInputStream inStream = new DataInputStream(s_clientSocket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(s_clientSocket.getOutputStream());
			String clientMessage="", serverMessage="";
			while(!clientMessage.equals("bye")){
				clientMessage=inStream.readUTF();
				System.out.println("From Client-" +clientID+ ": Number is :"+clientMessage);
				result = Integer.parseInt(clientMessage.split(" ")[0]) + Integer.parseInt(clientMessage.split(" ")[1]);
				serverMessage="From Server to Client-" + clientID + " sum is " +result;
				outStream.writeUTF(serverMessage);
				outStream.flush();
			}
			inStream.close();
			outStream.close();
			s_clientSocket.close();
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			System.out.println("Client -" + clientID + " exit!! ");
		}
	}
}
public class Server
{
	public static void main(String[] args) throws Exception
	{
		try{
			ServerSocket server=new ServerSocket(8888);
			int clientID=0;
      		System.out.println("Server Started ....");

			while(true){
			Socket serverClient=server.accept();  //server accept the client connection request
			System.out.println(" >> " + "Client No:" + clientID + " started!");
			MyServerClientThread sct = new MyServerClientThread(serverClient,clientID); //send  the request to a separate thread
			sct.start();
			clientID++;
			}
		}catch(Exception e){
			System.out.println(e);
		  }
	}
}
