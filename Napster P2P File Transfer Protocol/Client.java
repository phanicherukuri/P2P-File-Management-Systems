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
import java.util.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client
{
public static void main(String args[]) throws Exception
{  
  Socket socket;
  ArrayList al;  
  ArrayList<FileInfo> my = new ArrayList<FileInfo>();  
  Scanner scanner = new Scanner(System.in);  
  ObjectInputStream ois;
  ObjectOutputStream oos;
  String string;
  Object o,b;
  String directoryPath=null;
  int peerServerPort=0;
  Client objClient = new Client();
   
  try
  {
  BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
  System.out.println("Enter the directory that contain files");
  directoryPath=br.readLine();
  System.out.println("Enter the port number on which peer should act as server");
  peerServerPort=Integer.parseInt(br.readLine());
  
  ServerDownload objServerDownload = new ServerDownload(peerServerPort,directoryPath);
  objServerDownload.start();
  //int portnumber = Integer.parseInt(br.readLine());
  Socket clientThread = new Socket("localhost",3339);
  ObjectOutputStream objOutStream = new ObjectOutputStream(clientThread.getOutputStream());
  ObjectInputStream objInStream = new ObjectInputStream(clientThread.getInputStream());
  al = new ArrayList();  
  socket = new Socket("localhost",3339);
  System.out.println("client connected");
  ois = new ObjectInputStream(socket.getInputStream());
  oos = new ObjectOutputStream(socket.getOutputStream());  
  System.out.println("Enter the peerid for this directory");
  int readpid=Integer.parseInt(br.readLine());
  
  File folder = new File(directoryPath); 
  File[] listofFiles = folder.listFiles();
  FileInfo currentFile;
  File file;
  for (int i = 0; i < listofFiles.length; i++) {
  currentFile= new FileInfo();
  file = listofFiles[i];
  currentFile.fileName=file.getName();  
  currentFile.peerid=readpid;
  currentFile.portNumber=peerServerPort;
  my.add(currentFile);
  
  }
  oos.writeObject(my);
  System.out.println("Enter the file name to be downloaded");
  String fileNameToDownload = br.readLine();
  oos.writeObject(fileNameToDownload);
  System.out.println("waiting for the reply from server");
  ArrayList<FileInfo> peers= new ArrayList<FileInfo>();
  peers = (ArrayList<FileInfo>)ois.readObject(); 
  for(int i=0;i<peers.size();i++)
  {  
   	int result = peers.get(i).peerid;
   	int port = peers.get(i).portNumber;
   	System.out.println("the file is stored at peer id " +result+" on port "+port);
  }
  System.out.println("Enter the desired peer from which you want to download");
  int clientAsServerPeerid = Integer.parseInt(br.readLine());
  System.out.println("Enter the respective port number");
  int clientAsServerPortNumber = Integer.parseInt(br.readLine());
  clientAsServer(clientAsServerPeerid,clientAsServerPortNumber,fileNameToDownload,directoryPath);  
  /* System.out.println("Please enter from which client you are willing to download from the given options");
  int desiredClient = Integer.parseInt(br.readLine());
  System.out.println("Please enter the desired portnumber on which you want to download the file");
  int desiredPort = Integer.parseInt(br.readLine());
  System.out.println("Connecting to "+desiredClient+"...at port number "+desiredPort);
  ServerSocket clientSocket = new ServerSocket(desiredPort);
  */
  
  }
  catch(Exception e)
  {
   System.out.println("localhost and port-22 doesn't exist");
  }
}
public static void clientAsServer(int clientAsServerPeerid, int clientAsServerPortNumber, String fileNameTobeDownload, String directoryPath) throws ClassNotFoundException
{   
    try {
        Socket clientAsServersocket = new Socket("localhost",clientAsServerPortNumber);
        ObjectOutputStream clientAsServeroos = new ObjectOutputStream(clientAsServersocket.getOutputStream());
        ObjectInputStream clientAsServerois = new ObjectInputStream(clientAsServersocket.getInputStream());
        clientAsServeroos.writeObject(fileNameTobeDownload);
        int readBytes=(int) clientAsServerois.readObject();
        System.out.println("bytes transferred are "+readBytes);
        byte[] b=new byte[readBytes];
        clientAsServerois.readFully(b);
        OutputStream  fileOPstream = new FileOutputStream(directoryPath+"//"+fileNameTobeDownload);
		BufferedOutputStream buffOPS = new BufferedOutputStream(fileOPstream);
		buffOPS.write(b, 0,(int) readBytes);
        System.out.println(fileNameTobeDownload+" file has be downloaded to your directory "+directoryPath);
		buffOPS.flush();
				
        // ServerSocket clientAsServerServerSocket = new ServerSocket(clientAsServerPortNumber);
        // Socket clientAsServerSocket = clientAsServerServerSocket.accept();
    } catch (IOException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
}
}