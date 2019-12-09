package Sound;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SoundPlayer {

    private InputStream waveStream;
    private AudioInputStream audioInputStream;
    private SourceDataLine dataLine;
    private static int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    private byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];

    private byte[] sourceBytes;
    private int bytesPerFrame;

    private AudioFormat audioFormat;

    private Thread SoundThread;

    public static String basePath = "movies/";
    /**
     * CONSTRUCTOR
     */
    public SoundPlayer(String name) {
        String filename = basePath + name + "/" + name + ".wav";
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        this.waveStream = inputStream;
    }

    public void SetSourceBytes(byte[] src){
        sourceBytes = src;
    }

    public void Play(int startFrame){
        int startIdx = startFrame * bytesPerFrame;
        dataLine.close();
        //SoundThrea
        SoundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                try {
                    dataLine = (SourceDataLine) AudioSystem.getLine(info);
                    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
                } catch (LineUnavailableException e1) {
                    return;
                }
                dataLine.start();
                dataLine.write(sourceBytes,startIdx,sourceBytes.length-startIdx);
                dataLine.drain();
                dataLine.close();
            }
        });
        SoundThread.start();

    }

    public void playFrameAudio(int frame) throws PlayWaveException{
        int startIdx = frame * bytesPerFrame;
        dataLine.write(sourceBytes,startIdx,bytesPerFrame);
    }

    public void SKillAdutio(){
        dataLine.close();
    }


//    public void PlayNextFrameSound() throws PlayWaveException{
//        int byteToRead = 44100 / 30 * 2;
//
//        int readBytes = 0;
//        //if(bufferOffset > byteToRead){
//        dataLine.write(audioBuffer, bufferOffset, leftInBuffer);
//        byteToRead -= leftInBuffer;
//        //}
//        try {
//            while (readBytes != -1) {
//                readBytes = audioInputStream.read(audioBuffer, 0,
//                        audioBuffer.length);
//                if (readBytes >= 0){
//                    if(readBytes < byteToRead){
//                        byteToRead -= readBytes;
//                        dataLine.write(audioBuffer, 0, readBytes);
//                    }else{
//                        dataLine.write(audioBuffer,0,byteToRead);
//                        bufferOffset = byteToRead;
//                        leftInBuffer = readBytes - byteToRead;
//                        break;
//                    }
//                }
//            }
//            if(readBytes == -1){
//                dataLine.drain();
//                dataLine.close();
//            }
//        } catch (IOException e1) {
//            dataLine.drain();
//            dataLine.close();
//            throw new PlayWaveException(e1);
//        }
//    }

    public void loadMusic() throws PlayWaveException {

        audioInputStream = null;
        try {
            //audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);

            //add buffer for mark/reset support, modified by Jian
            InputStream bufferedIn = new BufferedInputStream(this.waveStream);
            audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);

        } catch (UnsupportedAudioFileException e1) {
            throw new PlayWaveException(e1);
        } catch (IOException e1) {
            throw new PlayWaveException(e1);
        }

        // Obtain the information about the AudioInputStream
        audioFormat = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        System.out.println(audioFormat.getFrameRate());
        bytesPerFrame = (int)audioFormat.getFrameRate()/30*audioFormat.getSampleSizeInBits()/8 + 20;

        // opens the audio channel
        dataLine = null;
        try {
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
        } catch (LineUnavailableException e1) {
            throw new PlayWaveException(e1);
        }
        List<Byte> ret = new ArrayList<Byte>();
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
        sourceBytes = new byte[ret.size()];
        for(int i=0;i<ret.size();i++){
            sourceBytes[i] = ret.get(i);
        }
        dataLine.start();
    }
}
