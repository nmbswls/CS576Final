package Sound;


import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.DataLine.Info;

public class PlaySound {

    private InputStream waveStream;
    private SourceDataLine dataLine;
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    /**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream) {
        this.waveStream = waveStream;
    }


    public void Stop(){

    }




    public void play() throws PlayWaveException {

        AudioInputStream audioInputStream = null;
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
        AudioFormat audioFormat = audioInputStream.getFormat();
        Info info = new Info(SourceDataLine.class, audioFormat);

        // opens the audio channel
        SourceDataLine dataLine = null;
        try {
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
        } catch (LineUnavailableException e1) {
            throw new PlayWaveException(e1);
        }

        // Starts the music :P
        dataLine.start();

        System.out.println("start");
        int readBytes = 0;
        byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

        try {
            while (readBytes != -1) {
                readBytes = audioInputStream.read(audioBuffer, 0,
                        audioBuffer.length);
                System.out.println("s");
                if (readBytes >= 0){
                    dataLine.write(audioBuffer, 0, readBytes);
                }
                System.out.println("e");
            }
        } catch (IOException e1) {
            throw new PlayWaveException(e1);
        } finally {
            System.out.println("close");
            // plays what's left and and closes the audioChannel
            dataLine.drain();
            dataLine.close();
        }
    }


//    public double[] timeToFreq(){
//        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
//        Complex[] result = fft.transform(inputData, TransformType.FORWARD);
//        result[0].abs()
//    }


}