package main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileWriter {

    public static void WriteToFILE(String filename, String toPrint){

        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename,true)));
            out.write(toPrint);
            out.write("\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
