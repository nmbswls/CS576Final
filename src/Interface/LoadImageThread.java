package Interface;

import main.MovieReader;

import java.awt.image.BufferedImage;
import java.util.Queue;

public class LoadImageThread extends Thread {

    Queue<BufferedImage> imageQueue;
    int bufferSize = 5;
    ImagePlayInfo pinfo;
    int renderIdx = -1;
    boolean renderFinish;
    public LoadImageThread(ImagePlayInfo pinfo, Queue<BufferedImage> imageQueue){
        this.pinfo = pinfo;
        this.imageQueue = imageQueue;
        renderFinish = false;

        reset();
    }



    @Override
    public void run() {
        while(true){
            if(pinfo == null || pinfo.name == null || pinfo.nowIdx < 0 || renderFinish || imageQueue.size() > 5){
                try {
                    Thread.sleep(20);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            BufferedImage newImage = new BufferedImage(MovieReader.width,MovieReader.height,BufferedImage.TYPE_INT_RGB);
            byte[][][] frame = MovieReader.readMovieframe(pinfo.name,renderIdx);
            if(frame == null){
                renderFinish = true;
                continue;
            }
            MainPanel.fillImage(frame, newImage);
            imageQueue.add(newImage);
            renderIdx ++;
        }
    }

    public void reset(){
        startFrom(0);
    }

    public void startFrom(int startIdx){
        renderFinish = false;
        imageQueue.clear();
        renderIdx = startIdx;
    }
}
