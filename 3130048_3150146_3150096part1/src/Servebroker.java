import java.net.*;
import java.nio.file.Files;
import java.io.*;
import java.util.*;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;



public class Servebroker extends Thread {

	
	
	Socket businessocket ;
	ArrayList<String> songs; 
	DataInputStream take;
	ObjectOutputStream give;
	
	
	
	public Servebroker(Socket businessocket,ArrayList<String> songs) {
	   try {
		this.businessocket = businessocket;
		this.take = new DataInputStream(businessocket.getInputStream());
		this.give = new ObjectOutputStream(businessocket.getOutputStream());
		this.songs = songs;
	   }catch(Exception e) {}
	}
	
	
	
	public void run() {
	  try {
	   String demandedartist = take.readUTF();  
	   String demandedsong = take.readUTF();
	   MusicFile[] thefile= searchForRequestedSong(demandedartist,demandedsong,"./dataset2");
	   if (thefile==null) {
		   give.writeObject(thefile);   
                  
	   }else {
	     for (MusicFile mychunk : thefile) {
		   give.writeObject(mychunk);
		   give.flush();
	     }
	     give.writeObject(null);
	   }
	   give.flush();
	   businessocket.shutdownOutput();
	   businessocket.close();
	  }catch(Exception e) {}
	   
	}

   
	
	 MusicFile[] searchForRequestedSong(String demandedartist,String songName, String folder){
		    MusicFile [] track =null;
	        if (songExists(songName)) {
	            
	            songName=songName.concat(".mp3");
	            File dir = new File(folder);
	            for(final File file : dir.listFiles()){
	                
	                if(file.getName().compareToIgnoreCase(songName)==0){
	                    
	                    try {
	                        Mp3File mp3file = new Mp3File(file);

	                        if (mp3file.hasId3v2Tag()) {
	                            ID3v2 id3v2Tag = mp3file.getId3v2Tag();
	                            String artist;
	                            byte[] data;
	                            artist = id3v2Tag.getArtist();
	                            data = Files.readAllBytes(file.toPath());
	                            if (demandedartist.equals(artist)) {
	                             
	                             track = chunking(songName.substring(0,songName.length()-4),artist,data);
                                 
	                            }
	                        }
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    } catch (UnsupportedTagException e) {
	                        e.printStackTrace();
	                    } catch (InvalidDataException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        }
			return track;
	    }


	
   boolean songExists(String songName){

    for(String song:songs){
        if (song.compareToIgnoreCase(songName)==0) {
            return true;
        }
    }
    return false;
   }
   
   
   
   MusicFile[] chunking(String name, String artist,byte[] data){
       File folder = new File("./output");
       if(!folder.exists()){
           folder.mkdir();
       }
      
       int size = (int) Math.ceil((double) data.length /(512*1024));

       MusicFile[] chunks = new MusicFile[size];
       for(int i = 0; i<size; i++){
           String chunkName = name.concat("_"+(i+1)+"_"+size);
           byte[] chunkData;
           int lastByte = (i+1)*(512*1024)-1;
           
           int totalBytes = (lastByte <= data.length ? 512*1024 : data.length-i*(512*1024));
           
           int stop = (lastByte <= data.length ? lastByte+1 : data.length);
          
           chunkData = new byte[totalBytes];
           int k=0;
           for(int j = i*(512*1024); j< stop; j++){
               chunkData[k]=data[j];
               k++;
           }
           chunks[i] = new MusicFile(chunkName,artist,chunkData);
       }
       return chunks;
   }

   
   
}



