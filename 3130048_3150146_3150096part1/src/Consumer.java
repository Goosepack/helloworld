import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {

    boolean registered;
    int id;

    public Consumer(int id, boolean registered){
        this.id = id;
        this.registered = registered;
    }

    void finalSong(MusicFile[] chunks){
        int numOfChunks = chunks.length;
        
        byte[] song = new byte[(numOfChunks-1)*512*1024+ chunks[numOfChunks-1].musicFileExtract.length];
        
        int k = 0;
        for(int i=0; i<numOfChunks;i++){
            
            int chunkIndex1 = chunks[i].trackName.indexOf("_")+1;
            int chunkIndex2 = chunks[i].trackName.lastIndexOf("_")-1;
            k = Integer.parseInt(chunks[i].trackName.substring(chunkIndex1,chunkIndex2+1));
            
            int l = (k-1)*512*1024;
           

            for(int s = 0; s<chunks[i].musicFileExtract.length;s++){

                
                song[l]= chunks[i].musicFileExtract[s];
                l++;
            }
        }
        
        File folder = new File("./output");
        if(!folder.exists()){
            folder.mkdir();
        }
       
        int index = chunks[0].trackName.indexOf("_");
        File file = new File("./output/"+chunks[0].trackName.substring(0,index)+".mp3");
        try {
            Files.write(file.toPath(),song);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    String[] connectWithBroker(String ip, int port,String artist, String song){
        MusicFile[] mf=null;
        String[] complete =new String[5];
        String myArtist = artist;
        String mySong = song;
        
        try {
            Socket s = new Socket(ip,port);
            
            InputStream is = null;
            OutputStream os = null;

            is = s.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            os = s.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            
           
            while(true){
                String tosend="findthisong";
                 dos.writeUTF(tosend);
            dos.flush();
            if(artist==null || song==null){
            Scanner scn = new Scanner(System.in);
            
            System.out.println("Exit? Yes/No");
            String exit = scn.nextLine();
            
            if(exit.equals("Yes")){
                complete[0] = "exit";
                break;
            }
            
            System.out.println("Enter Artist: ");
            myArtist= scn.nextLine();
            dos.writeUTF(myArtist);
            dos.flush();
            
            System.out.println("Enter Song: ");
            mySong=scn.nextLine();
            dos.writeUTF(mySong);
            dos.flush();
            
            tosend = "finished";
            dos.writeUTF(tosend);
            dos.flush();
            }else{
                tosend = myArtist;
                dos.writeUTF(tosend);
                dos.flush();
                
                tosend=mySong;
                dos.writeUTF(tosend);
                dos.flush();
                
                tosend = "finished";
            dos.writeUTF(tosend);
            dos.flush();
                
            }
            
            
            
            String answer = (String) ois.readObject();
            System.out.println(answer);
            
                if(answer.equals("OK")) {
                    
                    mf = getMFs(ois);
                    finalSong(mf);
                    complete[0] = "yes";
                    artist = null;
                    song = null;
                }else if(answer.equals("Wrong Broker")){
                    
                    System.out.println("Connection closed");
                    String[] artistinfo = (String[]) ois.readObject();
                    complete[0] = answer;
                    complete[1] = artistinfo[0];
                    complete[2]= artistinfo[1];
                    complete[3]= myArtist;
                    complete[4]= mySong;
                    
                    break;
                }else{
                    complete[0]=answer;
                }}
            
            s.shutdownOutput();
            s.shutdownInput();
            
            s.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return complete;
    }
    
     MusicFile[] getMFs(ObjectInputStream ois){
        MusicFile[] mf = null;

        try {

            MusicFile m = (MusicFile) ois.readObject();
            int j1 = m.trackName.lastIndexOf("_");
            
            int size = Integer.parseInt(m.trackName.substring(j1 + 1));
            
            mf = new MusicFile[size];
            mf[0] = m;

            for (int i = 1; i < size; i++) {
                mf[i] = (MusicFile) ois.readObject();

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return mf;
    }

    

    public static void main(String[] args){
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter consumer id: ");
            int id = Integer.parseInt(sc.nextLine());
            
            System.out.println("Subscribed Consumer: ");
            String sub = sc.nextLine();
            boolean subscribed=false;
            if(sub.equals("Yes")){
                subscribed = true;
            }
            
            Consumer con = new Consumer(id, subscribed);
            File brokers = new File("./The_brokers.txt");
            sc = new Scanner(brokers);
            
            String[] randomBroker = sc.nextLine().split(",");
            int port = Integer.parseInt(randomBroker[1]);
            
            String[] answer = con.connectWithBroker(randomBroker[0], port,null,null);
            while(answer[0].equals("Wrong Broker") && !answer[0].equals("exit")){
                System.out.println(answer[1]+" "+answer[2]);
                answer=con.connectWithBroker(answer[1], Integer.parseInt(answer[2]),answer[3],answer[4]);
            }
            System.out.println(answer[0]);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }
}
