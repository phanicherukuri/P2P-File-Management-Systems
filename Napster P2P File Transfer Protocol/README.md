# Design Document
In this project, we are implementing a simple P2P system that has two components: an Indexing Server and a peer. The project is 
Implemented as: Indexing Server is the main server which maintains the files from all the connected clients, whereas, peer establishes
the connection with the server and asks for a particular file. If server has information of that particular file it enables the peer to 
download the requested file from the client which is maintaining that particular file. I have implemented P2P system in the Java Socket 
Programming.
In my implementation of the project, it contains, 4 files namely: Client.java, Server.java, FileInfo.java, ServerDownload.java. The main
functionalities of our project is registry and lookup (search). The files are registered to server and lookup operation is also performed 
in the server.
## Working:
Firstly, we need to establish the connection between client and the server using Socket, and ServerSocket classes and accept() of 
ServerSocket object. Server and peers can communicate after this connection establishment. First we need to run our server program then any
number of client programs can run at the same time and request data from server. Both client and server are running on the same system so 
instead of specifying specific server hostID we can give it as a “localhost”. 
### Client.java:
It is a peer program particular with the user, each user has their own peer program. Here each peer can establish a connection with 
the server by specific port-number. Peer should enter directory, portnumber, peerid, and file to be searched. All these values are sent to
the server where it stores in the globalArray. Once server reply back peer chooses from the list of options and gets connected to that
particular peer and downloads the file. During this process a separate thread of each peer (in connection with server) is running and this
thread acts as server for the requested peer while downloading the file. This can be implemented as control moves to ServerDownload.java 
class which extends the Thread class at lang.Thread package. Separate Thread start executing by calling the start() on ServerDownload 
constructor for which we should pass directory and port number as a parameters so that peer thread running as server can transfer files
to the requested peer. In the ServerDownoad class, which also extends Thread, requested file size is stored in the form of bytes and send
to requested peer, write() is used to download the file in the requested peer directory. While(true) is used to send data in the form of 
chunks. Here I am sending data in the form of chunks because when we are sending large contents of data we cannot send it at a stretch, 
so for the better performance I have used transmission in the form of chunks. 
### Server.java:
In our project indexing server is the key aspect which maintains the data of all connected peers at any particular time, here I am
maintaining all these data using ArrayList. The connection between the clients and server is established by invoking accept() 
ServerSocket stance. Server maintains data off all connected clients in the globalArray. globalArray is of the type FileInfo class. 
FileInfo is a serializable class. I have implemented serialization concept to main data ArrayList data. Server after receiving data 
from peer, stores data in the globalArray (registry) and search for the (lookup) requested object and sends result object to client 
which contains the information regarding where and on which port the requested file is stored. We have declared globalArray as static
(global variable) so that it gives the same result for all the connected and requested peers. The main objective of Index server is to
register and lookup. Our server program is perform these two functions.
