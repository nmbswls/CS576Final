package Sound;

import Discriptor.MovieStruct;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SoundReader {

    private static int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
    public static String basePath = "movies/";

    public static void readAudio(String name, MovieStruct ms){
        String filename = basePath + name + "/" + name + ".wav";
//        "440Hz_44100Hz_16bit_05sec.wav";

        // opens the inputStream
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
            //inputStream = this.getClass().getResourceAsStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println("no audio found");
            //e.printStackTrace();
            return;
        }
        try {
            ReadAudioFile(inputStream,ms);
        } catch (PlayWaveException e) {
            e.printStackTrace();
        }
    }

    public static void ReadAudioFile(InputStream waveStream, MovieStruct ms) throws PlayWaveException{
        List<Byte> ret = new ArrayList<Byte>();
        AudioInputStream audioInputStream = null;
        try {
            InputStream bufferedIn = new BufferedInputStream(waveStream);
            audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);

        } catch (UnsupportedAudioFileException e1) {
            throw new PlayWaveException(e1);
        } catch (IOException e1) {
            throw new PlayWaveException(e1);
        }
        AudioFormat audioFormat = audioInputStream.getFormat();

        // Obtain the information about the AudioInputStream
        int readBytes = 0;
        byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
        try {
            while (readBytes != -1) {
                readBytes = audioInputStream.read(audioBuffer, 0,
                        audioBuffer.length);
                if (readBytes >= 0){
                    for(int i=0;i<readBytes;i++){
                        ret.add(audioBuffer[i]);
                    }
                }

            }
        } catch (IOException e1) {
            throw new PlayWaveException(e1);
        }
        ms.channel = audioFormat.getChannels();
        ms.frameRate = (int)audioFormat.getFrameRate();
        ms.audio = new byte[ret.size()];
        for(int i=0;i<ret.size();i++){
            ms.audio[i] = ret.get(i);
        }
    }
}
