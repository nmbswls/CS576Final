package Discriptor;

import main.MovieReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TezhengSet {

    public double[] audioLevels;
    public double[] motionVecotrLengths;
    public int[][] mainColors;
    public int[] ys;


    public String saveToFile(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<audioLevels.length;i++){
            sb.append(audioLevels[i]);
            sb.append(" ");
        }
        sb.append("\n");
        for(int i=0;i<motionVecotrLengths.length;i++){
            sb.append(motionVecotrLengths[i]);
            sb.append(" ");
        }
        sb.append("\n");
        for(int i=0;i<mainColors.length;i++){
            sb.append(mainColors[i][0]);
            sb.append(" ");
            sb.append(mainColors[i][1]);
            sb.append(" ");
            sb.append(mainColors[i][2]);
            sb.append(" ");
        }
        sb.append("\n");
        for(int i=0;i<ys.length;i++){
            sb.append(ys[i]);
            sb.append(" ");
        }
        //sb.append("\n");
        return sb.toString();
    }



    public static TezhengSet constrctuFromFile(Scanner scanner){
        TezhengSet ret = new TezhengSet();

        String[] d1 = scanner.nextLine().trim().split(" ");
        ret.audioLevels = new double[d1.length];
        for(int i=0;i<d1.length;i++){
            ret.audioLevels[i] = Double.parseDouble(d1[i]);
        }

        String[] d2 = scanner.nextLine().trim().split(" ");
        ret.motionVecotrLengths = new double[d2.length];
        for(int i=0;i<d2.length;i++){
            ret.motionVecotrLengths[i] = Double.parseDouble(d2[i]);
        }

        String[] d3 = scanner.nextLine().trim().split(" ");
        ret.mainColors = new int[d3.length/3][3];
        for(int i=0;i<d3.length/3;i++){
            ret.mainColors[i][0] = Integer.parseInt(d3[3*i]);
            ret.mainColors[i][1] = Integer.parseInt(d3[3*i+1]);
            ret.mainColors[i][2] = Integer.parseInt(d3[3*i+2]);
        }

        String[] d4 = scanner.nextLine().trim().split(" ");
        ret.ys = new int[d4.length];
        for(int i=0;i<d4.length;i++){
            ret.ys[i] = Integer.parseInt(d4[i]);
        }

        return ret;
    }

    public static TezhengSet constructFromMovie(MovieStruct input){
        TezhengSet ret = new TezhengSet();
        ret.extractAudioLevels(input);
        ret.extractMotionVector(input);
        ret.extractColorLevels(input);
        ret.extractY(input);
        return ret;
    }

    private int getY(int R,int G,int B) {
        return (int)(R*0.299+G*0.587+B*0.114);
    }


    private void extractY(MovieStruct input){
        ys = new int[input.vedio.size()];
        for(int i=0;i<input.vedio.size();i++){
            byte[][][] frame = input.vedio.get(i);
            int totalY = 0;
            for (int y = 0; y < frame.length; y++) {
                for (int x = 0; x < frame[0].length; x++) {
                    byte[] rgb = frame[y][x];
                    totalY += getY(rgb[0] & 0xff,rgb[1] & 0xff,rgb[2] & 0xff);
                }
            }
            ys[i] = totalY / (frame.length * frame[0].length);
        }
    }

    private void extractColorLevels(MovieStruct input){
        mainColors = new int[input.vedio.size()][3];
        for(int i=0;i<input.vedio.size();i++){

            int[] rgb = handleColorOneFrame(input.vedio.get(i));
            mainColors[i] = rgb;
        }
    }

    private void countBucketNum(Map<Integer,Integer> bucket, byte r, byte g, byte b) {
        int rLevel=(r&0xff) / 32;
        int gLevel=(g&0xff) / 32;
        int bLevel=(b&0xff) / 32;

        int bucketIdx = rLevel * 64 + gLevel * 8 + bLevel;
        bucket.put(bucketIdx,bucket.getOrDefault(bucketIdx,0)+1);
    }

    private int[] handleColorOneFrame(byte[][][] frame){
        Map<Integer, Integer> bucket = new HashMap<Integer, Integer>();
        for (int y = 0; y < frame.length; y++) {
            for (int x = 0; x < frame[0].length; x++) {
                byte r = frame[y][x][0];
                byte g = frame[y][x][1];
                byte b = frame[y][x][2];
                countBucketNum(bucket,r,g,b);
            }
        }
        int max_index=-1,max_count=Integer.MIN_VALUE;
        for(Map.Entry<Integer, Integer> entry : bucket.entrySet()){
            if(entry.getValue() > max_count){
                max_count = entry.getValue();
                max_index = entry.getKey();
            }
        }
        if(max_index == -1){

        }
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        for (int y = 0; y < frame.length; y++) {
            for (int x = 0; x < frame[0].length; x++) {
                byte r = frame[y][x][0];
                byte g = frame[y][x][1];
                byte b = frame[y][x][2];

                int rLevel=(r&0xff) / 32;
                int gLevel=(g&0xff) / 32;
                int bLevel=(b&0xff) / 32;

                int bucketIdx = rLevel * 64 + gLevel * 8 + bLevel;
                if(bucketIdx == max_index){
                    sumR += r & 0xff;
                    sumG += g & 0xff;
                    sumB += b & 0xff;
                }
            }
        }

        int avgr = sumR / max_count;
        int avgg = sumG / max_count;
        int avgb = sumB / max_count;

        return new int[]{avgr,avgg,avgb};
    }


    public static int audioFrameLidu = 120;
    private void extractAudioLevels(MovieStruct input){

        int bunchSize = input.frameRate / audioFrameLidu;
        int sum = 0;
        int bunchIdx = 0;
        int idxInBunch = 0;

        audioLevels = new double[input.audio.length/2/bunchSize/input.channel];
        int idx = 0;
        for(int i=0;i<input.audio.length/input.channel/2;i++){

            int totalWav = 0;
            for(int j=0;j<input.channel;j++){
                byte b1 = input.audio[idx++];
                byte b2 = input.audio[idx++];
                short wavValue = (short) ((b1 & 0xFF)| (b2 << 8));
                totalWav += wavValue;
            }

            sum += totalWav / input.channel;
            idxInBunch += 1;

            if(idxInBunch > bunchSize){
                double avg = sum * 1.0 / bunchSize;
                avg = Math.abs(avg);
                double dbOrigin = 20 * Math.log10(avg/32767);
                double dbAc = (dbOrigin + 93) * 120.0 / 93.0;
                if(dbAc < 0){
                    dbAc = 0;
                }
                if(dbAc>120){
                    dbAc = 120;
                }
                audioLevels[bunchIdx] = Math.round(dbAc * 100) / 100.0;


                idxInBunch = 0;
                sum = 0;
                bunchIdx ++;
            }

        }
        System.out.println("");
    }


    public void extractMotionVector(MovieStruct input){

        double[] ret = new double[input.vedio.size()];
        for(int i=1;i<input.vedio.size();i++){
            double vectorMo = getMotionVectorMo(input.vedio.get(i),input.vedio.get(i-1));
            if(vectorMo == -1){
                ret[i] = -1;
            }else{
                ret[i] = Math.round(vectorMo * 100) / 100.0;
            }
        }
        this.motionVecotrLengths = ret;
    }

    //public static double MotionMaxDiff = 100;

    public double getMotionVectorMo(byte[][][] nowf, byte[][][] pref){

        double vectorMoSum = 0;
        int validVectorNum = 0;
        for(int i=0;i< MovieReader.height/16;i++){
            for(int j=0;j<MovieReader.width/16;j++){
                int oi = 16*i;
                int oj = 16*j;
                //int k = 31;

                int[] vector = logSearch(nowf,pref,oi,oj);
                if(vector != null){
                    validVectorNum += 1;
                    vectorMoSum += Math.sqrt(vector[0]*vector[0] +  vector[1]*vector[1]);
                }else{
                    //System.out.println("??");
                }
                //if(smallestDiff>)
            }
        }
        if(validVectorNum == 0){
            return -1;
        }
        return vectorMoSum / validVectorNum;
    }

    private static double maxDiff = 16*16*0.5;
    int[][] searchDir = new int[][]{{-1,0},{-1,-1},{-1,1},{1,0},{1,1},{1,-1},{0,1},{0,-1},{0,0}};
    public int[] logSearch(byte[][][] nowf, byte[][][] pref, int oiNow, int ojNow){
        int ojSearch = ojNow;
        int oiSearch = oiNow;
        int k = 16;
        double globalMin = Double.MAX_VALUE;
        while(k>1){
            int newCenterI = -1;
            int newCenterJ = -1;
            double minDiff = Double.MAX_VALUE;
            for(int i=0;i<searchDir.length;i++){
                int di = searchDir[i][0] * k/2;
                int dj = searchDir[i][1] * k/2;
                int newI = oiSearch + di;
                int newJ = ojSearch + dj;
                double diff = getDiff(nowf,pref,oiNow,ojNow,newI,newJ);
                if(diff<0){
                    continue;
                }
                if(diff<minDiff){
                    minDiff = diff;
                    newCenterI = newI;
                    newCenterJ = newJ;
                }
            }
            globalMin = minDiff;
            k /= 2;
            ojSearch = newCenterJ;
            oiSearch = newCenterI;
        }
        if(globalMin > maxDiff){
            return null;
        }else{
            //System.out.println("you");
            return new int[]{oiSearch-oiNow,ojSearch-ojNow};
        }
    }

    public int[] hierarchicalSearch(){
        return null;

    }

    public double getDiff(byte[][][] nowf, byte[][][] pref, int oiNow, int ojNow, int oiPre,int ojPre){
        double diff = 0;
        for(int i=0;i<16;i++){
            for(int j=0;j<16;j++){
                if(oiNow+i<0||oiNow+i>=MovieReader.height
                        ||ojNow+j<0||ojNow+j>=MovieReader.width
                        ||oiPre+i<0||oiPre+i>=MovieReader.height
                        ||ojPre+j<0||ojPre+j>=MovieReader.width){
                    return -1;
                }

                double colorDiff = getColorDiff(nowf[oiNow+i][ojNow+j],pref[oiPre+i][ojPre+j]);
                diff += colorDiff;
            }
        }
        return diff;
    }

    public double getColorDiff(byte[] c1, byte[] c2){
        double dsqr = (c1[0]-c2[0]) * (c1[0]-c2[0]) + (c1[1]-c2[1]) * (c1[1]-c2[1]) + (c1[2]-c2[2]) * (c1[2]-c2[2]);
        return Math.sqrt(dsqr)/441.68;
    }





}
