package Interface;

import Discriptor.MovieStruct;
import Discriptor.TezhengComparator;
import Discriptor.TezhengSet;
import Sound.PlayWaveException;
import Sound.SoundPlayer;
import javafx.util.Pair;
import main.MovieReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.general.DefaultKeyedValues2DDataset;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

class ImagePlayInfo{
    public String name;
    public int nowIdx = -1;
    public boolean isStart;
    public boolean isPause;
//    public boolean isSliding;
    public byte[][][] firstFrame;
}

public class MainPanel extends JFrame{

    //public int height = 288;
    //public int width = 352;

    public static String databaseName = "d0.txt";


    public static int queryLength = 150;
    public static int dbLength = 600;

    JTextField tf;
    JButton queryBtn;

    JList jlist;
    JLabel jlb1,jlb2;
    JScrollPane jsp;

    JPanel top;
    JPanel middle;
    JPanel buttom;


    JButton play1;
    JButton pause1;
    JButton stop1;

    JButton play2;
    JButton pause2;
    JButton stop2;

    JPanel bestMatchPanel;
    JButton jumpToBestMatch;
    JLabel bestMathFeame;
    private int bestMatchIdx;
    JButton playSync;

    BufferedImage image1;
    BufferedImage image2;

    JLabel imgLabel1;
    JLabel imgLabel2;

    JSlider slider;
    ChartPanel[] discriptors;

    LoadImageThread load1Thread;
    LoadImageThread load2Thread;
    //Thread renderThread;
    //private boolean renderFinish = false;
    Queue<BufferedImage> imageQueueQuery = new LinkedBlockingDeque<BufferedImage>();
    Queue<BufferedImage> imageQueueSelect = new LinkedBlockingDeque<BufferedImage>();


    public ImagePlayInfo pinfoQuery = new ImagePlayInfo();
    public ImagePlayInfo pinfoSelect = new ImagePlayInfo();
    private Timer timer1;
    private Timer timer2;

    private TezhengSet queryTzSet;

    private boolean isSliding = false;
    private static double frameRate = 30;


    private SoundPlayer sound1;
    private SoundPlayer sound2;


    private long preFrameTimestampQuery;
    private long preFrameTimestampSelect;



    //public String[] MatchedNames;
    public List<Pair<String, Double>> MatchInfo = new ArrayList<Pair<String, Double>>();

    public MainPanel(){
        LoadDataBase();

        RegisterTimers();

        top = new JPanel(new FlowLayout());
        middle = new JPanel(new GridLayout(1,2));
        buttom = new JPanel(new GridLayout(1,2));

        top.setPreferredSize(new Dimension(0,150));
        middle.setPreferredSize(new Dimension(0,250));
        buttom.setPreferredSize(new Dimension(0,340));

        buttom.setBackground(Color.gray);

        load1Thread = new LoadImageThread(pinfoQuery,imageQueueQuery);
        load2Thread = new LoadImageThread(pinfoSelect,imageQueueSelect);

        load1Thread.start();
        load2Thread.start();


        ConstructTopPanel();
        ConstructMiddlePanel();
        ConstructBottomPanel();



        Container contentPane=this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        //this.setLayout(new BorderLayout());

        this.add(top,BorderLayout.NORTH);
        this.add(middle,BorderLayout.CENTER);
        this.add(buttom,BorderLayout.SOUTH);

        this.setSize(800,800);
        this.setTitle("TEst");
        this.setVisible(true);
        this.setResizable(false);
    }


    private void RegisterTimers(){
        {
            ActionListener taskPerformer=new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e) {

                    long nowTime = System.currentTimeMillis();
                    if(nowTime - preFrameTimestampQuery < 33){
                        return;
                    }
                    preFrameTimestampQuery += 33;

                    if(pinfoQuery.nowIdx < 0){
                        return;
                    }
                    if(isSliding){
                        return;
                    }
                    while(true){
                        if(imageQueueQuery.isEmpty()){
                            if(pinfoQuery.nowIdx < queryLength){
                                continue;
                            }
                            Stop(0);
                            return;
                        }else{
                            BufferedImage img = imageQueueQuery.poll();
                            PlayRenderedImage(imgLabel1,img);
                            break;
                        }
                    }
//                    byte[][][] frame = MovieReader.readMovieframe(pinfoQuery.name,pinfoQuery.nowIdx);
//                    if(frame == null){
//                        Stop(0);
//                        return;
//                    }
//                    PlayImage(frame,imgLabel1,image1);
//                    try {
//                        sound1.playFrameAudio(pinfoQuery.nowIdx);
//                    } catch (PlayWaveException e1) {
//                        e1.printStackTrace();
//                    }
                    //PlayNextFrameSound();
                    //System.out.println("播放"+frameIdxQuery);

                    pinfoQuery.nowIdx += 1;

                }
            };
            timer1 = new Timer(5,taskPerformer);
        }

        {
            ActionListener taskPerformer=new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long nowTime = System.currentTimeMillis();
                    if(nowTime - preFrameTimestampSelect < 33){
                        return;
                    }
                    preFrameTimestampSelect += 33;
//                    long t = System.currentTimeMillis();
//                    System.out.println(t-pretime);
//                    pretime = t;

                    if(pinfoSelect.nowIdx < 0){
                        return;
                    }
                    if(isSliding){
                        return;
                    }
                    while(true){
                        if(imageQueueSelect.isEmpty()){
                            if(pinfoSelect.nowIdx < dbLength){
                                continue;
                            }
                            Stop(1);
                            return;
                        }else{
                            BufferedImage img = imageQueueSelect.poll();
                            PlayRenderedImage(imgLabel2,img);
                            break;
                        }
                    }
//                    try {
//                        sound2.playFrameAudio(frameIdxSelect);
//                    } catch (PlayWaveException e1) {
//                        e1.printStackTrace();
//                    }

//                    byte[][][] frame = MovieReader.readMovieframe(nowSelectMovieName,frameIdxSelect);
//                    if(frame == null){
//                        Stop(1);
//                        return;
//                    }

                    //System.out.println("播放"+frameIdxQuery);
//                    PlayImage(frame,imgLabel2,image2);
                    slider.setValue(pinfoSelect.nowIdx);
                    pinfoSelect.nowIdx += 1;

                }
            };
            timer2 = new Timer(5,taskPerformer);
        }
    }

    private void ConstructTopPanel(){
        JPanel jp1 = new JPanel(null);
        JPanel jp2 = new JPanel(null);

        jlb1 = new JLabel("Query: ");
        tf = new JTextField(10);
        queryBtn = new JButton("Query");


        queryBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                StartQuery();
            }
        });
        JPanel comp1 = new JPanel();

        comp1.add(jlb1);
        comp1.add(tf);
        comp1.add(queryBtn);
        comp1.setBounds(0,50,250,75);
        jp1.add(comp1);


        JPanel comp2 = new JPanel();

        jlb2 = new JLabel("Mached Videos: ");

        jlist = new JList(new String[0]);
        jlist.setVisibleRowCount(3);
        jlist.setFixedCellWidth(150);
        jsp = new JScrollPane(jlist);

        comp2.add(jlb2);
        comp2.add(jsp);
        comp2.setBounds(0,35,300,75);
        jp2.add(comp2);

        jp1.setPreferredSize(new Dimension(280,150));
        jp2.setPreferredSize(new Dimension(280,150));
        top.add(jp1);
        top.add(jp2);

        jlist.addMouseListener(new MouseAdapter() //列表框添加鼠标事件
        {
            public void mousePressed(MouseEvent e) {
                int idx = jlist.getSelectedIndex();
                if(idx < 0 || idx >= MatchInfo.size()){
                    return;
                }
                System.out.println(MatchInfo.get(idx));
                changeSelectMovie(MatchInfo.get(idx).getKey());
            }
        });
    }

    private void StartQuery(){
        String name = tf.getText();
        if(name==null || name.equals("")){
            return;
        }
        MovieStruct ms = MovieReader.readMovie(name);
        if(ms == null){
            return;
        }
        this.queryTzSet = TezhengSet.constructFromMovie(ms);
        System.out.println("start query");
        Map<String,Double> match = GetMatches(queryTzSet);
        MatchInfo.clear();
        for(Map.Entry<String,Double> entry : match.entrySet()){
            MatchInfo.add(new Pair<String,Double>(entry.getKey(),entry.getValue()));
        }

        MatchInfo.sort(new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });
        String[] toShow = new String[MatchInfo.size()];
        for(int i=0;i<toShow.length;i++){
            double remain4 = (int)(MatchInfo.get(i).getValue() * 10000.0) / 100.0;
            String rrate = String.valueOf(remain4);
            toShow[i] = MatchInfo.get(i).getKey()+"  "+rrate;
        }
        jlist.setListData(toShow);
        //changeQueryMovie(tf.getText());
        changeQueryMovie(name);

        sound1 = new SoundPlayer(name);
        try {
            sound1.loadMusic();
        } catch (PlayWaveException e) {
            e.printStackTrace();
        }
    }

    private void ConstructMiddlePanel(){

        JPanel lPanel = new JPanel();

        JPanel rPanel = new JPanel();

        middle.add(lPanel);
        middle.add(rPanel);

        CreateChartPanel();
        for(int i=0;i<discriptors.length;i++){
            rPanel.add(discriptors[i]);
        }


        slider = new JSlider(0, dbLength-1, 1);

        slider.setPreferredSize(new Dimension(300,50));

        slider.setPaintTicks(false);
        slider.setPaintLabels(true);

        ResetSliderRange(dbLength-1);

        // 添加刻度改变监听器
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                slidingChangeFrame(slider.getValue());
                //System.out.println("当前值: " + slider.getValue());
            }
        });
        MouseAdapter ma = new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                //System.out.println("按下");
                if(pinfoSelect.isStart&&!pinfoSelect.isPause){
                    sound2.SKillAdutio();
                }
                isSliding = true;
            }
            @Override
            public void mouseReleased(MouseEvent e){
                //System.out.println("松开");
                isSliding = false;
                load2Thread.startFrom(slider.getValue());
                if(pinfoSelect.isStart && !pinfoSelect.isPause){
                    sound2.Play(slider.getValue());
                }
            }

        };
        slider.addMouseListener(ma);
        slider.addMouseMotionListener(ma);
        rPanel.add(slider);


        bestMatchPanel = new JPanel(new FlowLayout(2));

        bestMathFeame = new JLabel("?");
        jumpToBestMatch = new JButton("Jump To");

        playSync = new JButton("Play Sync");

        bestMatchPanel.add(new JLabel("best match: "));
        bestMatchPanel.add(bestMathFeame);

        jumpToBestMatch.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(bestMatchIdx>=0){
                    Pause(1);
                    //sound2.SKillAdutio();
                    slider.setValue(bestMatchIdx);
                    //slidingChangeFrame(bestMatchIdx);
                    //sound2.Play(bestMatchIdx);
                }

            }
        });
        playSync.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Pause(0);
                Pause(1);
                Play(0);
                Play(1);
            }
        });


        //framePanel.add();
        bestMatchPanel.add(jumpToBestMatch);
        bestMatchPanel.add(playSync);
        bestMatchPanel.setPreferredSize(new Dimension(400,40));

        rPanel.add(bestMatchPanel);


    }


    private void ConstructBottomPanel(){
        JPanel blPanel = new JPanel(new BorderLayout());

        JPanel blbPanel = new JPanel();

        JPanel brPanel = new JPanel(new BorderLayout());

        JPanel brbPanel = new JPanel();

        JPanel brb2Panel = new JPanel();
        JPanel blb2Panel = new JPanel();

        play1 = new JButton("Play");
        pause1 = new JButton("Pause");
        stop1 = new JButton("Stop");

        {
            play1.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    System.out.print("play1");
                    Play(0);
                }
            });
            pause1.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    System.out.print("pause1");
                    Pause(0);
                }
            });
            stop1.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    System.out.print("stop1");
                    Stop(0);
                }
            });
        }



        blbPanel.add(play1);
        blbPanel.add(pause1);
        blbPanel.add(stop1);


        play2 = new JButton("Play");
        pause2 = new JButton("Pause");
        stop2 = new JButton("Stop");


        {
            play2.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    System.out.print("play2");
                    Play(1);
                }
            });
            pause2.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    System.out.print("pause2");
                    Pause(1);
                }
            });
            stop2.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    System.out.print("stop2");
                    Stop(1);
                }
            });
        }

        brbPanel.add(play2);
        brbPanel.add(pause2);
        brbPanel.add(stop2);

        blbPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
        brbPanel.setBorder(new EmptyBorder(0, 5, 5, 5));


        {
            image1 = new BufferedImage(MovieReader.width, MovieReader.height, BufferedImage.TYPE_INT_RGB);
            ImageIcon imageIcon = new ImageIcon(image1);
            imgLabel1 = new JLabel(imageIcon,SwingConstants.CENTER);
        }

        {
            image2 = new BufferedImage(MovieReader.width, MovieReader.height, BufferedImage.TYPE_INT_RGB);
            ImageIcon imageIcon = new ImageIcon(image2);
            imgLabel2 = new JLabel(imageIcon,SwingConstants.CENTER);
        }




//

        blPanel.add(imgLabel1, BorderLayout.NORTH);
        blPanel.add(blbPanel,BorderLayout.SOUTH);

        //blPanel.setPreferredSize(new Dimension(400,300));
        blPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        brPanel.add(imgLabel2, BorderLayout.NORTH);
        brPanel.add(brbPanel,BorderLayout.SOUTH);
        //brPanel.setPreferredSize(new Dimension(400,300));
        brPanel.setBorder(new EmptyBorder(5, 5, 5, 5));



        buttom.add(blPanel);
        buttom.add(brPanel);

    }
    public void changeResMovie(String name){
        //读取 并存放
        //

    }

    public void changeQueryMovie(String name){
        //读取 并存放
        //
        //List<byte[][][]> movie = MovieReader.readMovieSeq(name);
        pinfoQuery.firstFrame = MovieReader.readFirstFrame(name);
        pinfoQuery.name = name;

        if(pinfoQuery.firstFrame != null){
            PlayImage(pinfoQuery.firstFrame,imgLabel1,image1);
        }

        pinfoQuery.nowIdx = 0;
        pinfoQuery.isStart = false;
        pinfoQuery.isPause = false;
        timer1.stop();
    }

    public void changeSelectMovie(String name){
        //读取 并存放
        //

        pinfoSelect.firstFrame = MovieReader.readFirstFrame(name);
        pinfoSelect.name = name;

        if(pinfoSelect.firstFrame != null){
            PlayImage(pinfoSelect.firstFrame,imgLabel2,image2);
        }

        pinfoSelect.nowIdx = 0;
        pinfoSelect.isStart = false;
        pinfoSelect.isPause = false;
        timer2.stop();


        //改变进度条

        slider.setValue(0);
        //改变特征

        TezhengSet tzSet = database.getOrDefault(pinfoSelect.name,null);
        if(tzSet != null){
            List<double[]> relates = TezhengComparator.GetRelateArray(queryTzSet,tzSet);
            SetDiscriptorData(relates);
            int minIdx = 0;
            for(int i=1;i<relates.get(0).length;i++){
                if(relates.get(0)[i]>relates.get(0)[minIdx]){
                    minIdx = i;
                }
            }
            bestMathFeame.setText((minIdx+1)+"");
            bestMatchIdx = minIdx;
        }

        bestMatchPanel.setVisible(true);


        sound2 = new SoundPlayer(name);
        try {
            sound2.loadMusic();
        } catch (PlayWaveException e) {
            e.printStackTrace();
        }
    }

    public List<String> getRelatedMovies(){

        return new ArrayList<String>();
    }


    public void slidingChangeFrame(int idx){
        pinfoSelect.nowIdx = idx;
        byte[][][] frame = MovieReader.readMovieframe(pinfoSelect.name,pinfoSelect.nowIdx);
        if(frame == null){
            Stop(1);
            return;
        }
        PlayImage(frame,imgLabel2,image2);
    }

    public void Play(int idx){
        if(idx == 0){
            if(pinfoQuery.isStart&&!pinfoQuery.isPause){
                return;
            }
            //frameIdxQuery = 0;
            pinfoQuery.isStart = true;
            pinfoQuery.isPause = false;

            timer1.start();

            preFrameTimestampQuery = System.currentTimeMillis();
            //sound1.Play(0);

            sound1.Play(pinfoQuery.nowIdx);

        }else{

            if(pinfoSelect.isStart&&!pinfoSelect.isPause){
                return;
            }
            //frameIdxQuery = 0;
            pinfoSelect.isStart = true;
            pinfoSelect.isPause = false;
            timer2.start();

            //load1 = new

            preFrameTimestampSelect = System.currentTimeMillis();

            //load1Thread
//            renderIdxSelect = frameIdxSelect;
//            if(renderThread != null){
//                renderThread.interrupt();
//            }
//            imageQueue.clear();
//            renderThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while(true){
//                        if(renderIdxSelect - frameIdxSelect > 5){
//                            try {
//                                Thread.sleep(10);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        BufferedImage newImage = new BufferedImage(MovieReader.width,MovieReader.height,BufferedImage.TYPE_INT_RGB);
//                        byte[][][] frame = MovieReader.readMovieframe(nowSelectMovieName,renderIdxSelect);
//                        if(frame == null){
//                            renderFinish = true;
//                            break;
//                        }
//                        fillImage(frame, newImage);
//                        imageQueue.add(newImage);
//                        renderIdxSelect ++;
//                    }
//
//                }
//            });
            sound2.Play(pinfoSelect.nowIdx);
            //renderThread.start();
        }
    }

    public void Pause(int idx){
        if(idx == 0){
            if(!pinfoQuery.isStart || pinfoQuery.isPause){
                return;
            }
            timer1.stop();
            pinfoQuery.isPause = true;


            sound1.SKillAdutio();
        }else{
            if(!pinfoSelect.isStart || pinfoSelect.isPause){
                return;
            }
            timer2.stop();
            pinfoSelect.isPause = true;

            sound2.SKillAdutio();
        }
    }

    public void Stop(int idx){
        if(idx == 0){
            if(!pinfoQuery.isStart){
                return;
            }
            pinfoQuery.nowIdx = 0;
            timer1.stop();
            pinfoQuery.isStart = false;
            pinfoQuery.isPause = false;

            if(pinfoQuery.firstFrame != null){
                PlayImage(pinfoQuery.firstFrame,imgLabel1,image1);
            }

            load1Thread.reset();
            sound1.SKillAdutio();

        }else{
            if(!pinfoSelect.isStart){
                return;
            }
            pinfoSelect.nowIdx = 0;
            timer2.stop();
            pinfoSelect.isStart = false;
            if(pinfoSelect.firstFrame != null){
                PlayImage(pinfoSelect.firstFrame,imgLabel2,image2);
            }
            load2Thread.reset();
            sound2.SKillAdutio();
        }
    }

    public void ResetSliderRange(int maxFrame){
        slider.setMaximum(maxFrame);
        Hashtable<Integer,JComponent> hashtable = new Hashtable<Integer,JComponent>();
        hashtable.put(0,new JLabel("Start"));
        hashtable.put(maxFrame,new JLabel("End"));
        slider.setLabelTable(hashtable);
    }

    public Map<String,Double> GetMatches(TezhengSet tz){



        Map<String,Double> matchMap = new HashMap<String,Double>();

        for(Map.Entry<String, TezhengSet> entry : database.entrySet()){
            double relate =  TezhengComparator.GetMaxRelate(tz,entry.getValue());
            matchMap.put(entry.getKey(),relate);
        }

        //matchMap.put("ss",0.3f);

        return matchMap;
    }

    Map<String, TezhengSet> database = new HashMap<String, TezhengSet>();

    public void LoadDataBase(){

        Scanner scanner;
        try {
            scanner = new Scanner(new FileInputStream(databaseName));
            while(scanner.hasNextLine()){
                String name = scanner.nextLine();
                TezhengSet set = TezhengSet.constrctuFromFile(scanner);
                System.out.println(name);
                database.put(name, set);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //constructFromString()
    }




    //一次性读取


    public void PlayImage(byte[][][] frame,JLabel lbIm1, BufferedImage imgOne){
        fillImage(frame,imgOne);
        ImageIcon imageIcon = new ImageIcon(imgOne);
        lbIm1.setIcon(imageIcon);
    }

    public void PlayRenderedImage(JLabel lbIm1, BufferedImage imgOne){
        //ImageIcon imageIcon = new ImageIcon(imgOne);
        ImageIcon icon = (ImageIcon)lbIm1.getIcon();
        icon.setImage(imgOne);
        lbIm1.repaint();
        //lbIm1.setIcon(imageIcon);
    }


    public static void fillImage(byte[][][] rawImg, BufferedImage outImg){
        for(int y=0;y<MovieReader.height;y++){
            for(int x=0;x<MovieReader.width;x++){
                byte[] originRgb = rawImg[y][x];
                int pix = 0xff000000 | ((originRgb[0] & 0xff) << 16) | ((originRgb[1] & 0xff) << 8) | (originRgb[2] & 0xff);
                outImg.setRGB(x,y,pix);
            }
        }
    }

    public JFreeChart genChart(DefaultKeyedValues2DDataset mDataset){
        JFreeChart mChart = ChartFactory.createLineChart(
                null,//图名字
                null,//横坐标
                null,//纵坐标
                mDataset,//数据集
                PlotOrientation.VERTICAL,
                false, // 显示图例
                true, // 采用标准生成器
                false);// 是否生成超链接

        mChart.setBorderVisible(false);


        CategoryPlot mPlot = (CategoryPlot)mChart.getPlot();
        mPlot.setBackgroundPaint(Color.white);

        LineAndShapeRenderer lasp = (LineAndShapeRenderer) mPlot.getRenderer();
        //lasp.setSeriesStroke(0, new BasicStroke(3F));
        mPlot.setDomainCrosshairVisible(false);
        mPlot.setDomainGridlinesVisible(false);

        mPlot.setRangeGridlinePaint(Color.BLUE);//背景底部横虚线
        mPlot.setOutlinePaint(Color.RED);//边界线

        CategoryAxis categoryAxis = mPlot.getDomainAxis();
        categoryAxis.setAxisLineVisible(false);
        categoryAxis.setTickLabelsVisible(false);
        categoryAxis.setLowerMargin(0);
        categoryAxis.setUpperMargin(0);

        System.out.println(categoryAxis.getLowerMargin());


        NumberAxis numberAxis = (NumberAxis) mPlot.getRangeAxis();
        numberAxis.setRange(0,1);
        numberAxis.setAxisLineVisible(false);
        numberAxis.setTickLabelsVisible(false);

        return mChart;
    }

    public void CreateChartPanel(){

        DefaultKeyedValues2DDataset empty = new DefaultKeyedValues2DDataset();
        JFreeChart mChart = genChart(empty);

        discriptors = new ChartPanel[5];
        for(int i=0;i<5;i++){
            discriptors[i] = new ChartPanel(mChart);
            discriptors[i].setPreferredSize(new Dimension(300,30));
        }
    }

    public double[] projectTo100Points(double[] input){
        double rate = input.length / 100.0;
        double[] ret = new double[100];
        for(int i=0;i<ret.length;i++){
            int projectIdx = (int)(i * rate);
            if(projectIdx >= input.length){
                projectIdx = input.length-1;
            }
            ret[i] = input[projectIdx];
        }
        return ret;
    }

    public void SetDiscriptorData(List<double[]> relates){

        for(int i=0;i<relates.size();i++){
            double[] values = projectTo100Points(relates.get(i));
            DefaultKeyedValues2DDataset mDataset = new DefaultKeyedValues2DDataset();
            for(int j=0;j<values.length;j++){
                mDataset.addValue(values[j],"1",j+"");
            }
            JFreeChart mChart = genChart(mDataset);
            discriptors[i].setChart(mChart);
        }

//        mDataset.addValue(0.1, "First", "2013");
//        mDataset.addValue(0.3, "First", "2014");
//        mDataset.addValue(0.2, "First", "2015");
//        mDataset.addValue(0.6, "First", "2016");
//        mDataset.addValue(0.5, "First", "2017");
//        mDataset.addValue(0.12, "First", "2018");


    }

    public static void main(String[] args){
        MainPanel mp = new MainPanel();

    }

}
