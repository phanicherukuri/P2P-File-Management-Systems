/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnutella;
 
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.String;
import java.lang.Object;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashSet;
/**
 *
 * @author Phani
 */
public class Client {
    ArrayList<Integer> myArray = new ArrayList<Integer>();
    public static void main(String args[]) throws ClassNotFoundException
    {
    InputStream input = null;
    String directoryPath=null;
    //File fileNameToDownload;
    Properties properties = new Properties();   //creates Properties object
    int peerid;
    int searchCount=0;
    ArrayList<ConnectionEstablishment> threadListCE = new ArrayList<ConnectionEstablishment>();
    ArrayList<Integer> searchResults = new ArrayList<Integer>();
    ArrayList<Thread> threadList = new ArrayList<Thread>();            
    //FileInfo is a serializable class which is also used to store unique messageid
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); // creating bufferreader to take inputs
        try {
            input = new FileInputStream("configFile.properties"); //used to obtain input from the file
            //File file = new File("configFile.properties");
            //FileInputStream fileInput = new FileInputStream(file);
            properties.load(input); //load properties from ConfigFile
            System.out.println("Gnutella style peer-to-peer star and mesh topologies");
            System.out.print("Enter peerid:");
            peerid=Integer.parseInt(br.readLine()); //stores peerid
            System.out.println("peer "+peerid+" neighbours and their respective port numbers are: ");
            int curtPeerPortid=Integer.parseInt(properties.getProperty("peer"+peerid+".port")); 
            //retrieves port-number for the peerid entered by the user from ConfigFile
            String n=properties.getProperty("peer"+peerid+".neighbours"); //neighbours are stored
            String[] peers=null;
            if(n!=null){
            peers=n.split(","); //saperating neighbours using "," and stored in an array
            for(int i=0;i<peers.length;i++)
            {
                System.out.print(peers[i]);
                System.out.print(" ");
                int id=Integer.parseInt(peers[i]);
                int parseInt = Integer.parseInt(properties.getProperty("peer"+id+".port"));
                //properties.getProperty is useed to retrieve values from configFile
                System.out.println(parseInt); //displaying neighbours peerid and port-numbers
            }
            }
            //System.out.println(" "); 
            System.out.println("Enter the directory that contain files");
            directoryPath=br.readLine(); //stores the directory that contain files 
            new ClientAsServer(curtPeerPortid,directoryPath,peerid).start(); 
            //Starting ClientAsServer thread so that it will serve other peer requests while requesting from this peer       
            System.out.println("Enter the file name to be downloaded");
            ++searchCount;
            String msgid=peerid+"."+searchCount; 
            //Generating msgid using peerid and messagesequence, message sequence is considered as searchcount
            String fileNameToDownload=br.readLine();  //Requested file is stored
            System.out.println("Enter TTL for this particular file:");
            int ttl = Integer.parseInt(br.readLine()); //TimeToLeave is stored 
            System.out.println("Searching files.....");
            for(int i=0;i<peers.length;i++)
            {
             int peer=Integer.parseInt(peers[i]);
             int nebrPortNumber = Integer.parseInt(properties.getProperty("peer"+peer+".port")); 
             //Storing neighbour peer port-number

             //System.out.println("Connection Establishing to "+peer+" at portnumber:"+nebrPortNumber);
             //System.out.println(nebrPortNumber);
             ConnectionEstablishment connectionEstablishment = new ConnectionEstablishment(nebrPortNumber,fileNameToDownload,msgid,peerid);
             //Establishing connection to ConnectionEstablishment class
             Thread thread = new Thread(connectionEstablishment); //Creating single thread for each neighbour
             thread.start(); //Starting thread for each neighbour
             threadList.add(thread);
             //Storing thread instances in threadList
             threadListCE.add(connectionEstablishment);
             //Storing connectionEstablishment instances in threadListCE
            }
            
            for(int i=0;i<threadList.size();i++) 
            {
                ((Thread)threadList.get(i)).join(); //The join method allows one thread to wait for the completion of another
                //join method is used to stop execution of next lines
                
            }
            for(int i=0;i<threadListCE.size();i++) 
            {
                ArrayList<Integer> temp=threadListCE.get(i).getArray(); //Stores getArray result from ConnectionEstablishment
                searchResults.addAll(temp);                
            }
            LinkedHashSet<Integer> lhs = new LinkedHashSet<Integer>();
            lhs.addAll(searchResults);   // Storing all search results in lhs
            searchResults.clear();
            searchResults.addAll(lhs); 
            for(int i=0;i<searchResults.size();i++) 
            {
                System.out.println("Found at"+searchResults.get(i)); //Displays search results 
            }
            System.out.println("Enter desired peerid from which you want to download");
            int desiredPeerid = Integer.parseInt(br.readLine()); //Stores the desired peerid from which user wants to download
            System.out.println("Downloading file...");
            int portNumber=Integer.parseInt(properties.getProperty("peer"+desiredPeerid+".port"));
            download(desiredPeerid,portNumber,fileNameToDownload,directoryPath); //Method call
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
  
    public static void download(int clientAsServerPeerid, int clientAsServerPortNumber, String fileNameTobeDownload, String directoryPath) throws ClassNotFoundException
    {   
    try {
        Socket clientAsServersocket = new Socket("localhost",clientAsServerPortNumber);
        ObjectOutputStream clientAsServeroos = new ObjectOutputStream(clientAsServersocket.getOutputStream());
        ObjectInputStream clientAsServerois = new ObjectInputStream(clientAsServersocket.getInputStream());
        //String msg="2,"+fileNameTobeDownload;
        FileInfo msg=new FileInfo();
        msg.fileName=fileNameTobeDownload;
        msg.msgid="";
        msg.option=2; 
        clientAsServeroos.writeObject(msg);
        int readBytes=(int) clientAsServerois.readObject(); //Reading the length of requested file
        System.out.println("bytes transferred are "+readBytes);
        byte[] b=new byte[readBytes]; //Creates an instane of byte
        clientAsServerois.readFully(b); //Bytes are read from input stream and stores in b
        OutputStream  fileOPstream = new FileOutputStream(directoryPath+"//"+fileNameTobeDownload);
	BufferedOutputStream buffOPS = new BufferedOutputStream(fileOPstream);
	buffOPS.write(b, 0,(int) readBytes); //Writing data to file
        System.out.println(fileNameTobeDownload+" file has be downloaded to your directory "+directoryPath);
	buffOPS.flush(); //Used to flush bytes to destination
				
    } catch (IOException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
}
}  



class ConnectionEstablishment extends Thread
{
    int nebrPortNumber;    
    ArrayList<Integer> myArray = new ArrayList<Integer>();    
    String fileNameToDownload;
    String msgid;
    int fromPeerid;
    public ConnectionEstablishment(int nebrPortNumber, String fileNameToDownload, String msgid,int fromPeerid)
    {
        this.nebrPortNumber=nebrPortNumber;
        this.fileNameToDownload=fileNameToDownload;
        this.msgid=msgid;
        this.fromPeerid=fromPeerid;
    }
    public void run()
    {
        Socket socket;
        ObjectOutputStream oos;
        ObjectInputStream ois;    
        try {
            socket = new Socket("localhost",nebrPortNumber); 
            //ConnectionEstablishment and ClientAsServer are connected
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            FileInfo msg=new FileInfo();
            msg.fileName=fileNameToDownload;
            msg.msgid=msgid;
            msg.option=1;
            msg.fromPeerId=fromPeerid;
            //Storing details in msg
            oos.writeObject(msg); //Writing msg object to ClientAsServer
            myArray= (ArrayList<Integer>) ois.readObject(); //Reading object from ClientAsServer
            //socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionEstablishment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionEstablishment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public ArrayList<Integer> getArray()
    {
        return myArray;    //returns myArray with details of all peers that contains requested file 
    }    
}

class ClientAsServer extends Thread
{
    ArrayList<Integer> myArray = new ArrayList<Integer>();
    int curtPeerPortid;
    String directoryPath;
    int peerid;
    static ArrayList<String> processedMsgs;
    public ClientAsServer(int curtPeerPortid, String directoryPath, int peerid)
    {
        this.curtPeerPortid=curtPeerPortid;
        this.directoryPath=directoryPath;
        this.peerid=peerid;
        processedMsgs=new ArrayList<String>();
    }
    public void run()
    {
        ServerSocket serverSocket=null;
        Socket socket=null;
        ObjectOutputStream oos;
        ObjectInputStream ois;
        String fileName;
        Properties properties = new Properties();
        ArrayList<ConnectionEstablishment> threadListCE = new ArrayList<ConnectionEstablishment>();
            
        try {
        serverSocket = new ServerSocket(curtPeerPortid);
        } catch (IOException ex) {
            Logger.getLogger(ClientAsServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(true)
        {
            try {
                FileInputStream input = new FileInputStream("configFile.properties");
                properties.load(input);
                //Loading configFile
                int process=0;
                socket = serverSocket.accept(); //Connection establishment between neighbour client and peerid server
                HandleServer h=new HandleServer(socket, directoryPath, peerid,processedMsgs,properties);
                h.start();
                //Starting handleServer class thread where search and downloading of file operations are performed
              
            }catch (IOException ex) {
                Logger.getLogger(ClientAsServer.class.getName()).log(Level.SEVERE, null, ex);
            }         
        }
    }
    
}

class HandleServer extends Thread
{
    ObjectOutputStream oos;
    ObjectInputStream ois;
    Socket socket;
    int process=0;
    String fileName;
    String directoryPath;
    int peerid;
    ArrayList<Integer> myArray = new ArrayList<Integer>();
    Properties properties = new Properties();
    ArrayList<ConnectionEstablishment> threadListCE = new ArrayList<ConnectionEstablishment>();
    ArrayList<String> processedMsgs;
    public HandleServer(Socket socket,String directoryPath,int peerid,ArrayList<String> processedMsgs,Properties properties)
    {
        this.socket=socket;
        this.directoryPath=directoryPath;
        this.peerid=peerid;
        this.processedMsgs=processedMsgs;
        this.properties=properties;
        //Storing data
    }
    public void run(){
      try {
         boolean bDuplicate=false;
         FileInfo msg;
         String msgid;
         oos = new ObjectOutputStream(socket.getOutputStream());
         ois = new ObjectInputStream(socket.getInputStream());
         synchronized(this) //Only one thread can execute inside a Java code block synchronized
         {
           msg=(FileInfo) ois.readObject(); 
           //Reads data from ConnectionEstablishment class which contains details like messageid, downloading filename, option
           process=msg.option;
           fileName=msg.fileName;
           msgid=msg.msgid; 
           //Retrieving details from msg
           if (processedMsgs.size()>0)
           {
               System.out.println(processedMsgs.get(0));
           }
           if(this.processedMsgs.contains(msgid)) //Checking wheather request has already recieved
           {
                bDuplicate=true;                            
           }
           else{
               this.processedMsgs.add(msgid); //Adds msgid to processedMsgs if msgid is new to that peer
           }
           }
         switch(process)
         {
           case 1: //Searching functionality
             if(bDuplicate==false)
             {
             System.out.println("Searching file for msg id"+msgid);
             File folder=new File(directoryPath); //Directorypath is stored
             File[] listofFiles=folder.listFiles(); //Each file of directory is stored in listofFiles in File[] type
             File file;
             for(int j=0;j<listofFiles.length;j++)
             {
               file=listofFiles[j];
               Boolean tf=file.getName().equals(fileName); //Performing search operation
               if(tf)
                 {
                   System.out.println("Found");
                   myArray.add(peerid); 
                   //If requested file is found then adding that peerid to myArray
                 }
             }
             ArrayList<Thread> threadList = new ArrayList<Thread>();
             String n=properties.getProperty("peer"+peerid+".neighbours"); //Retrieving neighbours of neighbour peers
             String[] peers=null;
             if(n!=null)
             {
              peers=n.split(",");
              for(int i=0;i<peers.length;i++)
                {
                   if(msg.fromPeerId==Integer.parseInt(peers[i]))
                   {
                   continue;
                   }
                   int port=Integer.parseInt(properties.getProperty("peer"+peers[i]+".port"));
                   //System.out.println("Connection Establishing to "+peers[i]+" at portnumber:"+port);
                   ConnectionEstablishment connectionEstablishment = new ConnectionEstablishment(port,fileName,msgid,peerid);
                   Thread thread = new Thread(connectionEstablishment);
                   thread.start();
                   threadList.add(thread); //Creating and adding ConnectionEstablishment thread for the neighbours     
                   threadListCE.add(connectionEstablishment);//Adding ConnectionEstablishment instances
                }
             }
             for(int i=0;i<threadList.size();i++)
             {
              ((Thread)threadList.get(i)).join(); //Stops the execution of further statements until child threads terminates
             }
             for(int i=0;i<threadListCE.size();i++)
             {
               ArrayList<Integer> temp=threadListCE.get(i).getArray(); //Stores result in temp ArrayList
               myArray.addAll(temp); //Storing temp results myArray 
             }
            }
             else
             {
              System.out.println("Duplicate message request:"+msgid);
             }
             oos.writeObject(myArray);
             break;
             case 2: //downloading code
               while(true) //By using this loop data is send in in the form of chunks
                 {
                  File myFile = new File(directoryPath+"//"+fileName); //Filename is located in the directory
                  long length = myFile.length(); //Determines the length of the file
                  byte [] mybytearray = new byte[(int)length]; //Length of the file is stored in the form of bytes
                  oos.writeObject((int)myFile.length()); //Sending file length
                  oos.flush(); //Flushing all data(if remained)
                  FileInputStream fileInSt=new FileInputStream(myFile);
                  BufferedInputStream objBufInStream = new BufferedInputStream(fileInSt);
                  objBufInStream.read(mybytearray,0,(int)myFile.length()); //Reads data i.e., chunks of bytes
                  System.out.println("sending file of " +mybytearray.length+ " bytes"); //Prints sending file length
                  oos.write(mybytearray,0,mybytearray.length); //Transfering data in the form of chunks
                  oos.flush(); //Flushing data(if remained)
               
               }
            }
        } catch (IOException ex) {
            Logger.getLogger(HandleServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HandleServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(HandleServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    
}
}