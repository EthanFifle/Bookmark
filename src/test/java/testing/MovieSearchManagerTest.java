package testing;

import home.controller.search.MovieSearchManager;
import home.controller.search.MovieTitleSearchStrategy;
import home.model.Movie;
import home.model.SearchCriteria;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.Test;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MovieSearchManagerTest {

    @Test
    void getMovieOverview() {
        MovieSearchManager mvs = new MovieSearchManager();

        String type = "KEY_MOVIE";
        String searchKey = "KEY_MOVIE_TITLE";
        String value = "cars";
        SearchCriteria searchCriteria = new SearchCriteria(type,searchKey,value);
        Set<Movie> expectedSet = mvs.searchMovie(searchCriteria);
        String expectedOverview = "Blindsided by a new generation of blazing-fast racers, the legendary Lightning McQueen is suddenly pushed out of the sport he loves. To get back in the game, he will need the help of an eager young race technician with her own plan to win, inspiration from the late Fabulous Hudson Hornet, and a few unexpected turns. Proving that #95 isn't through yet will test the heart of a champion on Piston Cup Racing’s biggest stage!";

        Iterator<Movie> itr = expectedSet.iterator();

        String actualDescription = itr.next().getOverview();
//        Assertions.assertNotNull(actualDescription);
        while(itr.hasNext()){
            Movie mov = itr.next();
            String title = mov.getTitle();
            String overV = mov.getOverview();
            if(title == "Cars 3"){
                Assertions.assertEquals(expectedOverview, overV);
            }
//            if(itr.next().getTitle().equals("Cars 3")){
//                Assertions.assertEquals(expectedOverview, itr.next().getOverview());
//            }
        }
        }

    }
