
import java.net.*;
import java.io.*;
import java.util.*;
import java.security.*;
import java.math.*;



public class Checkthisclient extends Thread  {
	
	
	
	Broker parent;
	ArrayList<String[]> brokerlist; 
	Socket mysocket ;
	DataInputStream input;
	ObjectOutputStream output;
	String myhash;
	MessageDigest digestofthread;
	ArrayList<String> thecommand;
	String [] clientinfo;
	String myip;
	String myport;
	

	
	public Checkthisclient(Socket mysocket,ArrayList<String[]> brokerlist,Broker parent,String myip,String myport,String myhash) {
		this.mysocket = mysocket;
		this.brokerlist = brokerlist;
		this.myhash = myhash;
		this.thecommand = new ArrayList<String>();
		this.clientinfo = new String[2];                     
		this.myip = myip;
		this.myport = myport;
		this.parent = parent;
	}
 

	
	public void run() {
	   try {
		this.clientinfo[0] = mysocket.getInetAddress().toString().substring(1);
		this.input= new DataInputStream(mysocket.getInputStream());
		this.output = new ObjectOutputStream(mysocket.getOutputStream());
		while (true) {        
                    
                    String newcomer =input.readUTF();
                    
		  while (!newcomer.equals("finished")) {   
		    this.thecommand.add(newcomer);
		    newcomer =input.readUTF();
		  }
		  String typeofcommand = this.thecommand.remove(0);
		  if(typeofcommand.equals("findthisong")){
                      
			     ArrayList<MusicFile> thesong = new ArrayList<MusicFile>();
			     findthisong(thesong);   
		  }  
	    }
	  }catch(Exception e){
	     }try {
                 
	       mysocket.close();
		   String typeofcommand = this.thecommand.remove(0);
		   if (typeofcommand.equals("artistallocation")) { 
			   artistallocation();
		   }else if(typeofcommand.equals("takemyartists")){
			   takemyartists();
	       }
	   }catch(Exception e) {}
	}
	
	
	
	public void artistallocation() {
		this.clientinfo[1] = thecommand.remove(thecommand.size()-1);
		BigInteger Maximushashstring= new BigInteger(this.brokerlist.get(brokerlist.size()-1)[2], 16); 
                BigInteger bigIntHash = new BigInteger(this.myhash,16);
                try {
			this.digestofthread = MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {}
		String stringforhash;
		for (int i = this.thecommand.size()-1;i>=0;i--) {
			 stringforhash = this.thecommand.get(i);
                         
			 try {
				this.digestofthread.update(stringforhash.getBytes("utf-8"));
			 }catch (Exception e) {}
                         BigInteger artistInteger = new BigInteger(1, this.digestofthread.digest());
                     artistInteger = artistInteger.remainder(Maximushashstring);
		     if (artistInteger.compareTo(bigIntHash)==-1) { 
		    	 for (int j = 0;j<this.brokerlist.size();j++) {
		    		 String hashbroker = this.brokerlist.get(j)[2];
                                 BigInteger bigIntBrok = new BigInteger(hashbroker,16);
                                 if(bigIntBrok.compareTo(artistInteger)==1){
                                    if ( bigIntBrok.compareTo(bigIntHash)==-1) {
                                     
		    			 this.thecommand.remove(i);     
		    			 break;
		    		 }else if (bigIntBrok.compareTo(bigIntHash)==0) { 
		    			 break;
		    		 }
                                }
                         }
		     }else{
                         this.thecommand.remove(i);
                     }
	         this.digestofthread.reset();
		}
                
		if (this.thecommand.size()!=0) {
                        
			this.parent.networkmapperhandler("server",thecommand,this.clientinfo[0],this.clientinfo[1]);
	    }
	}
	

	
	public void takemyartists() { 
		this.parent.networkmapperhandler("client",thecommand,null,null);
	}
	
	
	
	public void findthisong(ArrayList<MusicFile >thesong) { 
	 String [] myartistinfo = this.parent.networkmapper.get(thecommand.get(0)) ;
         
	  try {
	  if (myartistinfo !=null) {
		  if (myartistinfo[0].compareTo(this.myip)+myartistinfo[1].compareTo(this.myport)==0) {
			  Socket letsgetthisong = new Socket(myartistinfo[2],Integer.parseInt(myartistinfo[3]));
			  DataOutputStream sendapplication = new DataOutputStream(letsgetthisong.getOutputStream());
                          sendapplication.flush();
		      ObjectInputStream getingresults = new ObjectInputStream(letsgetthisong.getInputStream());
		      sendapplication.writeUTF(thecommand.get(0));
		      sendapplication.flush();
		      sendapplication.writeUTF(thecommand.get(1));
		      sendapplication.flush();
		      MusicFile chunk = (MusicFile) getingresults.readObject();
		      if (chunk!=null) {                           
                          this.output.writeObject("OK");
                          this.output.flush();
		        while(chunk!=null){
		    	  thesong.add(chunk);
		    	  chunk = (MusicFile) getingresults.readObject();
		        } 
		        letsgetthisong.close();
                        
		      for (MusicFile part : thesong) {
		    	  this.output.writeObject(part);
		    	  this.output.flush();
		      }
		     }else {
		    	 this.output.writeObject("no such song");
                         this.output.flush();
		     }
		   }else { 
                            
                            output.writeObject("Wrong Broker");
			  output.writeObject(myartistinfo);
                          output.flush();
			 
		  }
	  }else {
                    String noartist = "no such artist";
		  output.writeObject(noartist);
                  output.flush();
	  }
	  
	  this.output.flush();
          this.thecommand.clear();
	 }catch(Exception e) {}
    }
	

	
}
