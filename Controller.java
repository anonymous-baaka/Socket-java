import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Queue;
import java.io.*;

class Request
{
    int clientID;
    String timestamp;
    Request(int _clientID,String _timestamp)
    {
        clientID=_clientID;
        timestamp=_timestamp;
    }
}
class ControllerThread extends Thread
{
    int nThreads;
    ArrayDeque<Request>requestQueue=new ArrayDeque<Request>();;
    

    int clientID;
	int result;
	Socket cl_coSocket;     //client-->controller socket
    Socket co_svSocket;
	ControllerThread(Socket _clientSocket, int _clientID){
		cl_coSocket=_clientSocket;
		clientID=_clientID;

        try{
        co_svSocket=new Socket("127.0.0.1",8889);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    @Override
    public void run()
    {
        try
        {
            //read input data from client;
            DataInputStream cl_inStream = new DataInputStream(cl_coSocket.getInputStream());
            DataInputStream sv_inStream = new DataInputStream(co_svSocket.getInputStream());
            String clientMessage="";
            String serverMessage="";
            while(!clientMessage.equals("bye")){
                clientMessage=cl_inStream.readUTF();
                System.out.println("From Client-" +clientID+ ": Number is :"+clientMessage);

                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                requestQueue.addLast(new Request(clientID,timeStamp));
                //requestQueue.add(new Request(clientID,timeStamp));
                
                //System.out.println("line 51 ");
                DataOutputStream outStream = new DataOutputStream(co_svSocket.getOutputStream());
                //System.out.println("line 53 ");
                outStream.writeUTF(clientMessage);
                //System.out.println("line 55 ");
                outStream.flush();
                //System.out.println("line 57 ");
                DataInputStream inStream=new DataInputStream(co_svSocket.getInputStream());
                String serverResultReply=inStream.readUTF(); //read server result

                if(serverResultReply!=null)
                {
                    System.out.println("result recieved from Server: "+serverResultReply+". Sending to client...");
                    DataOutputStream co_clOutStream=new DataOutputStream(cl_coSocket.getOutputStream());
                    co_clOutStream.writeUTF(serverResultReply);
                    co_clOutStream.flush();
                    System.out.println("result sent to client successfully!");
                }

            }
            cl_inStream.close();
			cl_coSocket.close();
            co_svSocket.close();
        }catch(Exception ex){
            System.out.println("hehe "+ex);
        }finally{
            System.out.println("Client -" + clientID + " exit!! ");
        }
    }
}

public class Controller
{
    public static void main(String[] args) throws Exception
    {
        try
        {
            ServerSocket controllersocket=new ServerSocket(8888);
            int clientID=0;
      		System.out.println("Controller Started ....");

			while(true){
			Socket clientController=controllersocket.accept();  //server accept the client connection request
			System.out.println(" >> " + "Client No:" + clientID + " connected to controller!");
			ControllerThread sct = new ControllerThread(clientController,clientID); //send  the request to a separate thread
			sct.start();
			clientID++;
            }
        }catch(Exception e){
			System.out.println(e);
		}
    }
}