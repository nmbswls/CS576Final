package Discriptor;

import Interface.ITezheng;
import Interface.MainPanel;

import java.util.ArrayList;
import java.util.List;

public class TezhengComparator {

    public static double GetMaxRelate(TezhengSet tz1, TezhengSet tz2){

        List<double[]> relates = GetRelateArray(tz1,tz2);
        double maxRelate = 0;
        for(int i=0;i<relates.get(0).length;i++){
            maxRelate = maxRelate > relates.get(0)[i] ? maxRelate : relates.get(0)[i];
        }
        return maxRelate;
    }

    public static List<double[]> GetRelateArray(TezhengSet tz1, TezhengSet tz2){

        double[] diff1 = getAudioDiffArray(tz1.audioLevels,tz2.audioLevels);
        double[] relate1 = new double[diff1.length];
        for(int i=0;i<diff1.length;i++){
            relate1[i] = 1-diff1[i];
        }
        double[] diff2 = getMovionDiffArray(tz1.motionVecotrLengths,tz2.motionVecotrLengths);
        double[] relate2 = new double[diff2.length];
        for(int i=0;i<diff2.length;i++){
            relate2[i] = 1-diff2[i];
        }

        double[] relateAll = new double[relate2.length];
        for(int i=0;i<relateAll.length;i++){
            relateAll[i] = relate1[i]*1 + relate2[i] * 0;
        }
        List<double[]> reates = new ArrayList<double[]>();
        reates.add(relateAll);
        reates.add(relate1);
        reates.add(relate2);
        return reates;
    }

    public static double arrayDiffOnePos(double[] s, double[] l, int lidx){
        double totalDiff = 0;
        for(int j=0;j<s.length;j++){
            double diff = s[j] - l[lidx+j] > 0 ? s[j]-l[lidx+j] : l[lidx+j] - s[j];
            totalDiff += diff;
        }
        double avfDiff = totalDiff / s.length;
        avfDiff = (int)Math.round(avfDiff * 100) / 100.0;
        return avfDiff;
    }


    public static double[] getMovionDiffArray(double[] v1, double[] v2){
        double[] s;
        double[] l;
        if(v1.length > v2.length){
            s = v1;
            l = v2;
        }else{
            l = v2;
            s = v1;
        }
        double[] ret = new double[l.length-s.length+1];
        for(int i=0;i<l.length-s.length+1;i++){
            double diff = arrayDiffOnePos(s,l,i);
            ret[i] = diff / 21.21;
        }
        return ret;
    }

    public static double[] getAudioDiffArray(double[] a1, double[] a2){
        double[] s;
        double[] l;
        if(a1.length > a2.length){
            s = a2;
            l = a1;
        }else{
            l = a2;
            s = a1;
        }
        double[] ret = new double[l.length-s.length+1];
        for(int i=0;i<l.length-s.length+1;i++){
            double diff = arrayDiffOnePos(s,l,i);
            //System.out.println(diff);
            ret[i] = diff / 120.0;
        }
        double[] projected = new double[l.length /4 - s.length /4 + 1];
        for(int i=0;i<projected.length-1;i++){
            double minDiff = Double.MAX_VALUE;
            for(int k=0;k<4;k++){
                minDiff = minDiff < ret[2*i+k] ? minDiff : ret[2*i+k];
            }
            projected[i] = minDiff;
        }
        projected[projected.length-1] = ret[ret.length-1];
        return projected;
    }

//    public static double getSmallestAudioDiff(double[] a1, double[] a2){
//        double[] diffArray = getAudioDiffArray(a1,a2);
//
//        double smallestDiff = Double.MAX_VALUE;
//        for(int i=0;i<diffArray.length;i++){
//
//            smallestDiff = smallestDiff < diffArray[i] ? smallestDiff : diffArray[i];
//        }
//        return smallestDiff;
//    }
}
