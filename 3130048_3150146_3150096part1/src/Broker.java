import java.io.*;
import java.util.*;
import java.security.*;
import java.math.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Broker {
	
	
	
	String myip ;
	String myport;
	String myhash;
	MessageDigest digest;  
	ArrayList<String[]>brokerlist ;
	ServerSocket Mydoor;                             
	Hashtable<String,String[]> networkmapper; 
	Hashtable<String,MusicFile[]> currentlystoredsongs;                           
	
     
	
	public static void main(String[] args) {
		Scanner inputscanner = new Scanner(System.in);
		System.out.println("Yes for test,No for real conditions");
        String decision = inputscanner.next();
        String myip =null;   
        String myport=null ;
        Broker brok =null;
        if (decision.compareTo("Yes")==0){
        	System.out.println("Do we want loopback?Type Yes or No");
        	decision = inputscanner.next();
        	if (decision.equals("Yes")) {
        	  myip = InetAddress.getLoopbackAddress().getHostAddress();
        	  
        	  System.out.println("Please insert my port's number");   
        	  myport = inputscanner.next();
        	}else {
        	   try {
        		myip = InetAddress.getLocalHost().getHostAddress();   
        		myport="60000";               
        	   }catch(Exception e){}
        	}
        	brok= new Broker(myip,myport);
        }else {
           try {
        	myip = InetAddress.getLocalHost().getHostAddress();
        	myport="60000";
        	System.out.println("Please insert my router's external ip: ");  
    		String myroutersip = inputscanner.nextLine();
    		System.out.println("Please insert my port on the router"); 
    		String myportonrouter = inputscanner.nextLine();
            brok= new Broker(myip,myport,myroutersip,myportonrouter);
           }catch(Exception e) {}
        }
		inputscanner.close();
		brok.makethehashes();
                
            try {
                brok.opentotheworld(); 
            } catch (IOException ex) {
                Logger.getLogger(Broker.class.getName()).log(Level.SEVERE, null, ex);
            }
		
	}	
	
	
	
	public Broker(String myip,String myport) {
	   try {
		this.myip = myip;
		this.myport = myport;
		this.digest = MessageDigest.getInstance("SHA-1");
		this.brokerlist = new ArrayList<String[]>();
		this.networkmapper = new Hashtable<String,String[]>();
	   }catch(Exception e) {}
    }
		 
	
		
	public Broker(String myip,String myport,String myroutersip,String myportonrouter) {
	    this(myip,myport);
	    this.myip=myroutersip;
	    this.myport= myportonrouter;
	}
	
	
    
	public void makethehashes () {
		try {
		    File Ipofbroks= new File("./The_brokers.txt");
			int i;
		    Scanner scannerofbroks;
			scannerofbroks = new Scanner(Ipofbroks);
		     while (scannerofbroks.hasNextLine()){
		    	 String [] truebroker = new String[3];
		    	 String [] nowbroker = scannerofbroks.nextLine().split(",");
		         truebroker[0] = nowbroker[0];
		         truebroker[1] = nowbroker[1]; 
		         String stringforhash = nowbroker[0].concat(nowbroker[1]);
		         this.digest.update(stringforhash.getBytes("utf-8"));
                         BigInteger bi = new BigInteger(1,digest.digest());
		         stringforhash = String.format("%040x", bi);
		         truebroker[2] = stringforhash;
		         for (i=0;i<this.brokerlist.size();i++) {
                                
                                BigInteger bii = new BigInteger(this.brokerlist.get(i)[2],16);
		        	 if (bii.compareTo(bi)==1) break;
		         }
		         this.brokerlist.add(i,truebroker);
		         this.digest.reset();
		         if (this.myip.compareTo(truebroker[0])==0 && this.myport.compareTo(truebroker[1])==0) this.myhash = truebroker[2];
		     }
			 scannerofbroks.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	
	}	
		
	
	
	public void opentotheworld() throws IOException {
            System.out.println("Broker: "+this.myip+" "+this.myport+" is up!");
            this.Mydoor=new ServerSocket(Integer.parseInt(myport),10,InetAddress.getByName(this.myip));
            while (true){
			Socket newsocket;
			try {
				newsocket = this.Mydoor.accept();
                                System.out.println("I have a client!");
				Checkthisclient newclient = new Checkthisclient(newsocket,this.brokerlist,this,this.myip,this.myport,this.myhash);
				newclient.start();
		    }catch (Exception e) {
                    e.printStackTrace();}
	    }	
    }
	
	

	public synchronized void networkmapperhandler(String whatshallido,ArrayList<String> thecommand,String frompub,String whoseportis) {
            
	   if(whatshallido.equals("client")) {
		   String newkey;
		   String [] keyinfos;
		   for (int i=0;i<thecommand.size();i+=5) {
			   keyinfos = new String[4];
			   newkey = thecommand.get(i);
                           
			   for (int j=i+1;j<i+5;j++) {
			      keyinfos[(j%5)-1] = thecommand.get(j);
                              
 		       }
			   this.networkmapper.put(newkey,keyinfos);
		   }
                   
	   }else if(whatshallido.equals("server")){
		String wheretoput []={this.myip,this.myport,frompub,whoseportis} ;
                for(int i =0;i<thecommand.size();i++) {
                    
		       this.networkmapper.put(thecommand.get(i),wheretoput);  
        }
		Set<String> keys = networkmapper.keySet();   
   	    ArrayList <String> keystosend = new ArrayList<String>();
            
   	    for (String key :keys) {
                           
			   String []infofkey = networkmapper.get(key);
                           
			   if (this.myip.equals(infofkey[0]) &&this.myport.equals(infofkey[1])){
                               
                               keystosend.add(key);
                           }
		}
   	    takemyartists(keystosend); 
	  }
           
	}
   	 
	
	public synchronized void takemyartists(ArrayList<String>keystosend) {
   	   try {
   		for (int d=0; d<this.brokerlist.size();d++) {
                    if(!this.brokerlist.get(d)[2].equals(this.myhash)){
		   Socket sharemyinfototheotherbrokers = new Socket(brokerlist.get(d)[0],Integer.parseInt(brokerlist.get(d)[1])); 
		   DataOutputStream sendtobroker = new DataOutputStream(sharemyinfototheotherbrokers.getOutputStream());
		   Thread.sleep(2000);
                   sendtobroker.writeUTF("takemyartists");
                   sendtobroker.flush();
		   for (String key :keystosend) {
                       Thread.sleep(2000);
		     sendtobroker.writeUTF(key); 
                     sendtobroker.flush();
		     for (String info : networkmapper.get(key)) {
		    	 sendtobroker.writeUTF(info);
                         sendtobroker.flush();
		     }
		   }
		   sharemyinfototheotherbrokers.shutdownOutput();
		   sharemyinfototheotherbrokers.close();
		}}
   	   }catch(Exception e) {}	
    }
	
	
	
}