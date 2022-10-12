import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static final String IN_FILE_TXT = "src\\materials\\inFile.txt";
    private static final String OUT_MUSIC_FILE_TXT = "src\\materials\\outMusicFile.txt";
    private static final String OUT_PICTURE_FILE_TXT = "src\\materials\\outPictureFile.txt";

    public static void main(String[] args) {
        String text;
        String[] data = new String[4];
        String musUrl;
        String musPath = "src";
        String picUrl;
        String picPath = "src";
        int stroke = 0;


        new File("src/materials/music").mkdirs();
        new File("src/materials/picture").mkdirs();

        try (BufferedReader inFile = new BufferedReader(new FileReader(IN_FILE_TXT));
             BufferedWriter outFile = new BufferedWriter(new FileWriter(OUT_MUSIC_FILE_TXT));
             BufferedWriter outPictureFile = new BufferedWriter(new FileWriter(OUT_PICTURE_FILE_TXT))) {
            URL urlPic = new URL("http:/");
            URL urlMus = new URL("http:/");
            while ((text = inFile.readLine()) != null) {
                data = text.split("\\s");
                switch (stroke) {
                    case 0:
                        picUrl = data[0];
                        urlPic = new URL(picUrl);
                        picPath = data[1];
                        break;
                    case 1:
                        musUrl = data[0];
                        urlMus = new URL(musUrl);
                        musPath = data[1];
                        break;
                }
                stroke++;
            }
            String result;
            String pictureSite;
            try (BufferedReader bfMus = new BufferedReader(new InputStreamReader(urlMus.openStream()));
                 BufferedReader bfPic = new BufferedReader(new InputStreamReader(urlPic.openStream()))) {
                result = bfMus.lines().collect(Collectors.joining("\n"));
                pictureSite = bfPic.lines().collect(Collectors.joining("\n"));
            }
            Pattern email_pattern = Pattern.compile("/track/dl/15762983/grottesque-\\S*.mp3");
            Pattern picture_pattern = Pattern.compile("images/screenshots/p05.png");
            Matcher matcher = email_pattern.matcher(result);
            Matcher matcherPic = picture_pattern.matcher(pictureSite);
            while (matcherPic.find()) {
                outPictureFile.write("http://www.celestegame.com/" + matcherPic.group() + "\r\n");
            }
            while (matcher.find()) {
                outFile.write("https://musify.club" + matcher.group() + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader musicFile = new BufferedReader(new FileReader(OUT_MUSIC_FILE_TXT));
             BufferedReader pictureFile = new BufferedReader(new FileReader(OUT_PICTURE_FILE_TXT))) {
            String music;
            String picture;
            try {
                while ((music = musicFile.readLine()) != null) {
                    Thread pic = new Download(music, musPath + String.valueOf(1) + ".mp3",true);
                    pic.start();
                }
                while ((picture = pictureFile.readLine()) != null) {
                    Thread mus = new Download(picture, picPath + String.valueOf(1) + ".png",false);
                    mus.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mp3Player(String path) {
        try (FileInputStream inputStream = new FileInputStream(path)) {
            try {
                Player player = new Player(inputStream);
                player.play();
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class Download extends Thread{
    String strUrl;
    String file;
    boolean music;
    Download(String strUrl, String file, boolean music){
        this.strUrl = strUrl;
        this.file = file;
        this.music = music;
    }
    public void run() {
        try {
            URL url = new URL(strUrl);
            ReadableByteChannel byteChannel = Channels.newChannel(url.openStream());
            FileOutputStream stream = new FileOutputStream(file);
            stream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
            stream.close();
            byteChannel.close();
        }catch (IOException e){
            e.getMessage();
            System.out.println("Произошла не предвиденная ошибка");
        }
        if(music){
            Main.mp3Player(file);
        }
    }
}