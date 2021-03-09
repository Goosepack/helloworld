import java.util.*;
import java.io.*;
import java.net.*;



public class Searchbroker extends Thread {

	
	
	String brokerip ;
	int brokersport ;
	ArrayList<String> artistfile;
	Socket mysocketformapping ;
    String pubserverport;
	
    
    
	public Searchbroker(String [] broker,ArrayList<String> artists,String pubserverport)  {
	    this.artistfile = artists;
	    this.brokerip=broker[0];
	    this.brokersport= Integer.parseInt(broker[1]);
	    this.pubserverport = pubserverport;
	}
	
	
	
    public void run(){
        
		 try {
		  this.mysocketformapping = new Socket(brokerip,brokersport);
		  Thread.sleep(2000);     
		  while (!mysocketformapping.isConnected()) {    
			  this.mysocketformapping = new Socket(this.brokerip,this.brokersport);
			  Thread.sleep(2000);
		  }
		  artistallocation();
		}catch(Exception e) {
                }
	  }
	  
	  
	  
	  public void artistallocation() {
		 try {
		  DataOutputStream myoutstream = new DataOutputStream(mysocketformapping.getOutputStream()) ;
		    myoutstream.writeUTF("artistallocation");
		    myoutstream.flush();
		    for (int d=0; d<artistfile.size();d++) {
                        
	    	  myoutstream.writeUTF(artistfile.get(d));
	    	  myoutstream.flush();
	        }
	        myoutstream.writeUTF(pubserverport);
	        myoutstream.flush();
	        mysocketformapping.shutdownOutput();
            mysocketformapping.close();
		}catch(Exception e) {}
	  }
	 
	  
	  
}
	   
