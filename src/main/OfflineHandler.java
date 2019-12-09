package main;

import Discriptor.MovieStruct;
import Discriptor.TezhengSet;
import Sound.PlaySound;
import Sound.SoundReader;

import java.io.File;
import java.util.List;

public class OfflineHandler {

    public static void main(String[] args){

        String databaseName = "d0.txt";
        File outFile = new File("d0.txt");

        if(outFile.exists()){
            outFile.delete();
        }

        String[] databaseNames = new String[]{"flowers","movie","interview","musicvideo","sports","traffic","starcraft"};
        for(int i=0;i<databaseNames.length;i++){
            String name = databaseNames[i];
            System.out.println(name);

            MovieStruct ms = new MovieStruct();

            List<byte[][][]> frames = MovieReader.readMovieSeq(name);
            ms.vedio = frames;

            SoundReader.readAudio(name,ms);



            TezhengSet tz0 = TezhengSet.constructFromMovie(ms);
            String tzString = tz0.saveToFile();

            FileWriter.WriteToFILE(databaseName,name);
            FileWriter.WriteToFILE(databaseName,tzString);
        }

    }






}
