package main;

import Discriptor.MovieStruct;
import Sound.SoundReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MovieReader {

    public static int height = 288;
    public static int width = 352;

    public static String basePath = "movies/";

    public static byte[][][] readMovieframe(String name, int idx){
        String fileName =   String.format("%s%03d.rgb", name,idx+1);
        String truePath = basePath + name + "/" + fileName;
        byte[][][] image = new byte[height][width][3];
        try
        {
            int frameLength = width*height*3;
            File file = new File(truePath);
            if(!file.exists()){
                return null;
            }
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];


            raf.read(bytes);
            int ind = 0;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    image[y][x][0] = r;
                    image[y][x][1] = g;
                    image[y][x][2] = b;
                    ind++;
                }
            }
            return image;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[][][] readFirstFrame(String name){
        return readMovieframe(name,0);
    }

    public static List<byte[][][]> readMovieSeq(String name)
    {

        List<byte[][][]> images = new ArrayList<byte[][][]>();
        int idx = 0;
        while(true){
            byte[][][] frame = readMovieframe(name,idx);
            if(frame==null){
                break;
            }
            idx++;
            images.add(frame);
        }
        return images;
    }

    public static MovieStruct readMovie(String name){
        List<byte[][][]> frames = readMovieSeq(name);
        MovieStruct ms = new MovieStruct();
        ms.vedio = frames;
        SoundReader.readAudio(name,ms);
        if(ms.audio == null || frames.size() == 0){
            return null;
        }

        return ms;
    }
}
