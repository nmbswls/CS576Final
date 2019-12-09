package Interface;

import Discriptor.MovieStruct;
import Interface.Handlers.AudioHandler;

import java.util.ArrayList;
import java.util.List;

public class TezhengManager {

    public List<TezhengHandler> handlers = new ArrayList<TezhengHandler>();

    public TezhengManager(){
        registerHandlers();
    }

    private void registerHandlers(){
        handlers.clear();
        handlers.add(new AudioHandler());
    }

    public void extractTezhengFromMovie(MovieStruct input){
        List<ITezheng> ret = new ArrayList<ITezheng>();
        for(int i=0;i<handlers.size();i++){
            ITezheng tz = handlers.get(i).extractFromMovie(input);
            ret.add(tz);
        }
    }
}
