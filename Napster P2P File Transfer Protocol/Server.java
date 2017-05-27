/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 
/**
 *
 * @author Phani
 */
 
import java.io.*;
import java.net.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Integer;
public class Server
{
    public static ArrayList<FileInfo> globalArray = new ArrayList<FileInfo>();
	public static void main(String args[])
	{
	ServerSocket serverSocket = null;
	Socket socket = null;
	try{
	 serverSocket = new ServerSocket(3339);
	}
	catch(IOException e)
	{
	e.printStackTrace();
	}
	while(true)
	{
	try{
	socket = serverSocket.accept();
	}
	catch(IOException e)
	{
	System.out.println("I/O error: " +e);
	}
	new ServerTestClass(socket,globalArray).start();
	}

	// ServerTestClass st = new ServerTestClass();
	// st.run();
	}
}
class ServerTestClass extends Thread
{
	protected Socket socket;
	ArrayList<FileInfo> globalArray;
	public ServerTestClass(Socket clientSocket,ArrayList<FileInfo> globalArray)
	{
		this.socket=clientSocket;
		this.globalArray=globalArray;
	}
//   Socket socket;
//   ServerSocket serverSocket;
	ArrayList<FileInfo> filesList=new ArrayList<FileInfo>();
   
	ObjectOutputStream oos;
	ObjectInputStream ois;
	String str;
	int index;

    public void run()
    {
/*    try{
       serverSocket = new ServerSocket(3339);
       System.out.println("Connected");   
    }
    catch(IOException ex)
    {
      System.out.println("Invalid port"); 
    }
    while(true){  
    try {
       socket = serverSocket.accept();
        }
     catch(IOException ex)
     {
       System.out.println("socket couldn't connect");
     } */
    try
    {  
    InputStream is=socket.getInputStream();
    oos = new ObjectOutputStream(socket.getOutputStream());
    ois = new ObjectInputStream(is);     
    filesList=(ArrayList<FileInfo>)ois.readObject();
    System.out.println("files recieved");      
    for(int i=0;i<filesList.size() ;i++)
    {
        globalArray.add(filesList.get(i));
    }
    System.out.println("Files in server from all connected clients: " +globalArray.size());
    }
    catch(IndexOutOfBoundsException e){
    System.out.println("Index out of bounds exception");
    }
    catch(IOException e){
    System.out.println("I/O exception");
    }
    catch(ClassNotFoundException e){
    System.out.println("Class not found exception");
    }           
    System.out.println("Client has send filname to be downloaded");
       try {
            str = (String) ois.readObject();
            System.out.println("object stored in str");
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServerTestClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<FileInfo> sendingPeers = new ArrayList<FileInfo>();
         System.out.println("Searching files...!!!"); 
           
       // int result;
       for(int j=0;j<globalArray.size();j++)
       {
           FileInfo fileInfo=globalArray.get(j);
           Boolean tf=fileInfo.fileName.equals(str);
       if(tf)
        {
        index = j;
        sendingPeers.add(fileInfo);
        }
        }
     try {
         oos.writeObject(sendingPeers);
     } catch (IOException ex) {
         Logger.getLogger(ServerTestClass.class.getName()).log(Level.SEVERE, null, ex);
     }
    
    }
}
    

 

