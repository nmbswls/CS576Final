package Interface;


import Discriptor.MovieStruct;

public interface TezhengHandler {



    ITezheng extractFromMovie(MovieStruct input);
    ITezheng loadFromFile();

}
