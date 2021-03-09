import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import com.mpatric.mp3agic.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Publisher {

	
	
    char s;
	char t;
	int id;
	String myport;
	String myip;                                       
	ArrayList<String> songs ;                        
	ArrayList<String> artists ;                       
	ArrayList<String []> brokers ;
	ServerSocket Gigasocket;
	
	
	
	Publisher(int id, char s, char t,String myip,String myport,String myportonrouter){
         this(id,s,t,myip,myport);
         this.myport= myportonrouter;    
   }
	
	
	
	Publisher(int id,char s,char t,String myip,String myport) {
	   try {
		this.id = id;
        this.s = s;
        this.t = t;
        this.myport = myport;
        this.myip = myip;
        this.songs = new ArrayList<String>();
        this.artists= new ArrayList<String>() ;                      
        this.brokers = new ArrayList<String []>();
    	
	   }catch(Exception e) {}
	}

	

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
        System.out.println("Enter Publisher id: ");
        String answer = sc.next();
        int id = Integer.parseInt(answer);
        System.out.println("Enter first letter and last letter, separated with a comma (no spaces): ");
        answer = sc.next();
        char s = answer.charAt(0);
        char t = answer.charAt(2);
        System.out.println("Yes for test,No for real conditions");
        String decision = sc.next();
        String myip =null;  
        String myport=null ;
        Publisher pub =null;
        if (decision.compareTo("Yes")==0){
        	System.out.println("Do we want loopback?Type Yes or No");
        	decision = sc.next();
        	if (decision.equals("Yes")) {
        	  myip = InetAddress.getLoopbackAddress().getHostAddress();
        	  System.out.println("Please insert my port's number");
        	  myport = sc.next();
        	}else {
        	   try {
        		myip = InetAddress.getLocalHost().getHostAddress();
            	myport="60000";
        	   }catch(Exception e){}
        	}
        	pub= new Publisher(id,s,t,myip,myport);
        }else {
           try {
        	myip = InetAddress.getLocalHost().getHostAddress();
        	myport="60000";
        	System.out.println("Enter the port of the router in which i ll listen");
            String myportonrouter = sc.nextLine();
            pub = new Publisher(id,s,t,myip,myport,myportonrouter);
           }catch(Exception e) {}
        }
        sc.close();
        pub.readSongs("./dataset2");
        
        pub.readbrokers();
        pub.communicate();
        pub.serve();
	}    
        

	
    void readSongs(String filename){
        final File folder = new File(filename);
        for (final File file : folder.listFiles()) {
            String name = file.getName();
            if (name.charAt(0) != '.') {
                int i = name.lastIndexOf('/');
                String songName = name.substring(i + 1, name.length() - 4);

                try {
                    Mp3File mp3file = new Mp3File(file);

                    if (mp3file.hasId3v2Tag()) {
                        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                        String artist;
                        artist = id3v2Tag.getArtist();
                        

                        char firstLetter = Character.toUpperCase(artist.charAt(0));
                        int compare = Character.compare(t, firstLetter);
                        if (compare > 0) {
                            songs.add(songName);
                            if (!artists.contains(artist)) {
                                artists.add(artist);
                            }
                        }
                    } else if (mp3file.hasId3v1Tag()) {
                        ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                        String artist;
                        artist = id3v1Tag.getArtist();
                        

                        char firstLetter = Character.toUpperCase(artist.charAt(0));

                        int compare = Character.compare(t, firstLetter);
                        if (compare > 0) {
                            songs.add(songName);
                            if (!artists.contains(artist)) {
                                artists.add(artist);
                                
                            }
                        }
                    } else {
                        System.out.println("no info");
                        
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    
    public void readbrokers()  {
    	try {
		 File Ipofbroks= new File("./The_brokers.txt");
		 Scanner brokscanner= new Scanner(Ipofbroks);
	     while (brokscanner.hasNextLine()){
	    	 brokers.add(brokscanner.nextLine().split(","));
                 
	     }
             
	     brokscanner.close();
    	}catch(Exception e) {}  
    }
    
    
    
    public void communicate() {
        
    	Searchbroker brokercontextinform;
  		for (int i=0;i<brokers.size();i++) {
                    
  			brokercontextinform = new Searchbroker(brokers.get(i),this.artists,this.myport);
  			brokercontextinform.start();                        
         }
    }
    
    
    
    void serve() {
        try {
            this.Gigasocket = new ServerSocket(Integer.parseInt(this.myport),10,InetAddress.getByName(this.myip));
            while (true) {
                Socket request;
                
                    request = this.Gigasocket.accept();
                    System.out.println("Song Request!");
                    Servebroker thethreadforthisrequest = new Servebroker(request,this.songs);
                    thethreadforthisrequest.start();       
                
                
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Publisher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Publisher.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

    
    
}

   
  
    
    
  

