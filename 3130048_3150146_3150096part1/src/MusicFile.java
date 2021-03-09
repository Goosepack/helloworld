
import java.io.Serializable;

public class MusicFile implements Serializable {
    String trackName;
    String artistName;

    byte[] musicFileExtract;

    public MusicFile(String trackName,String artistName, byte[] musicFileExtract){
        this.trackName=trackName;
        this.artistName=artistName;
        this.musicFileExtract=musicFileExtract;
       
    }
}