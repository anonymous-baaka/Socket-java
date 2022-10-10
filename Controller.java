import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.io.*;

class Request
{
    int clientID;
    String timestamp;
    String clientIp;
    long serverThreadID;
    public enum Status{
        COMPLETED,PROCESSING,FINISHED,ERROR
    }
    Status status;
    Request(int _clientID,String _timestamp,String _clIp,long _serverThreadID)
    {
        clientID=_clientID;
        timestamp=_timestamp;
        clientIp=_clIp;
        status=Status.PROCESSING;
        serverThreadID=_serverThreadID;
    }
}
class ControllerThread extends Thread
{
    int nThreads;
    //final static ArrayDeque<Request>requestQueue=new ArrayDeque<Request>();
    final static MyRequestQueue myrequestqueue=new MyRequestQueue();
    static int ClassclientID=0;
    int clientID;
	int result;
    long serverThreadID;
	Socket cl_coSocket;     //client-->controller socket
    Socket co_svSocket;
    String cl_ip;

	ControllerThread(Socket _clientSocket){
		cl_coSocket=_clientSocket;
		clientID=ClassclientID;
        cl_ip=_clientSocket.getLocalAddress().toString().substring(1);

        ClassclientID++;

        try{
        co_svSocket=new Socket("127.0.0.1",8889);
        //server sends thread id
        DataInputStream inStream=new DataInputStream(co_svSocket.getInputStream());
        String serverResultReply=inStream.readUTF();
        System.out.println(serverResultReply);

        //Client No:1 assigned to thread: 14
        serverThreadID=Long.parseLong(serverResultReply.split(":")[2].toString().strip());
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
                insertRequest(new Request(clientID,timeStamp,cl_ip,serverThreadID));
                
                DataOutputStream outStream = new DataOutputStream(co_svSocket.getOutputStream());
                outStream.writeUTF(clientMessage);
                outStream.flush();
                DataInputStream inStream=new DataInputStream(co_svSocket.getInputStream());
                String serverResultReply=inStream.readUTF(); //read server result

                if(serverResultReply!=null)
                {
                    System.out.println("result recieved from Server: "+serverResultReply+". Sending to client...");
                    DataOutputStream co_clOutStream=new DataOutputStream(cl_coSocket.getOutputStream());
                    co_clOutStream.writeUTF(serverResultReply);
                    co_clOutStream.flush();
                    System.out.println("result sent to client successfully!");

                    removeRequest(new Request(clientID,timeStamp,cl_ip,serverThreadID));
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

    
    void displayQueue()
    {
        //ArrayList<Request>a=new ArrayList<Request>();

        ArrayList<Request>requestQueueTemp = new ArrayList<Request>();// myrequestqueue.arr.clone();
        requestQueueTemp=myrequestqueue.getArray();

        System.out.println("----------------------------------------------------------------------------");
        System.out.println("Client ID\tClient Ip\t Timestamp\t\tStatus\t\tThread");
        System.out.println("----------------------------------------------------------------------------");
        //requestQueueTemp.
        for(Request request: requestQueueTemp)
        {
            System.out.println(request.clientID+"\t\t"+request.clientIp+"\t"+request.timestamp+"\t"+request.status+"\t"+request.serverThreadID);
        }
        System.out.println("----------------------------------------------------------------------------");
    }

    void removeRequest(Request request)
    {
        System.out.println("pop= "+myrequestqueue.pop(request));
        displayQueue();
    }

    void insertRequest(Request request)
    {
        myrequestqueue.push(request);
        displayQueue();
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
			ControllerThread sct = new ControllerThread(clientController); //send  the request to a separate thread
			sct.start();
			clientID++;
            }
        }catch(Exception e){
			System.out.println(e);
		}
    }
}