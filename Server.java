package server;

import java.io.IOException;
import java.net.ServerSocket;

public class Server
{

    public static void main(String[] args)
    {
    	int port = 2222;
    	try 
    	{
			ServerSocket serverSoket = new ServerSocket(port);
			while(true)
			{
				new ServerNit(serverSoket.accept()).start();
			}
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
    }

}
