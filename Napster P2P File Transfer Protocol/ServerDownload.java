 
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
   
/* 
 * 
 * @author Phani
 */
public class ServerDownload extends Thread
{
    int peerServerPort;
    String  directoryPath=null;
    ServerSocket dldServSocket;
    Socket dldSocket=null;
    public ServerDownload()
    {
       // this.portid=portid;
    }
      
    ServerDownload(int peerServerPort,String directoryPath) {
        
    this.peerServerPort=peerServerPort;
    this.directoryPath=directoryPath;    
    }
	public void run(){
    try {
        dldServSocket = new ServerSocket(peerServerPort);
        dldSocket = dldServSocket.accept();
        new ServerDownloadThread(dldSocket,directoryPath).start();
         
    } catch (IOException ex) {
        Logger.getLogger(ServerDownload.class.getName()).log(Level.SEVERE, null, ex);
    }
    
}    
  
} 
class ServerDownloadThread extends Thread
{
    Socket dldTdSocket;
    String directoryPath;
    public ServerDownloadThread(Socket dldTdSocket,String directoryPath)
    {
        this.dldTdSocket=dldTdSocket;       
        this.directoryPath=directoryPath;
    }
    public void run()
    {
        try
        {
            ObjectOutputStream objClientServerOutStream = new ObjectOutputStream(dldTdSocket.getOutputStream());
            ObjectInputStream objClientServerInStream = new ObjectInputStream(dldTdSocket.getInputStream());
            String fileName = (String)objClientServerInStream.readObject();
            String fileLocation;// stores directory name
            while(true)
            {
                File myFile = new File(directoryPath+"//"+fileName);
                long length = myFile.length();
                byte [] mybytearray = new byte[(int)length];
                objClientServerOutStream.writeObject((int)myFile.length());
                objClientServerOutStream.flush();
                FileInputStream fileInSt=new FileInputStream(myFile);
                BufferedInputStream objBufInStream = new BufferedInputStream(fileInSt);
                objBufInStream.read(mybytearray,0,(int)myFile.length());
                System.out.println("sending file of " +mybytearray.length+ " bytes");
                objClientServerOutStream.write(mybytearray,0,mybytearray.length);
                objClientServerOutStream.flush();                
            }
        }catch(Exception e)
        {
            
        }
    }
}