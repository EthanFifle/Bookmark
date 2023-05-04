package home.controller.search;

import home.model.BookmarkConstants;
import home.model.SearchCriteria;

import java.net.MalformedURLException;
import java.net.URL;

public class MovieActorSearchStrategy implements SearchStrategyIF {
    //Giving interface method relevant body
    //Getting the search key from searchCriteria then returning the appropriate url associated with the key
    @Override
    public URL getSearchURL(SearchCriteria searchCriteria) {
        System.out.println("MovieTitleSearchStrategy in action");
        URL url = null;
        if (searchCriteria != null) {
            if (searchCriteria.getSearchKey().equals(BookmarkConstants.KEY_MOVIE_ACTOR)) {
                String search = searchCriteria.getValue().replaceAll(" ", "+"); //format for url
                try {
                    url = new URL("https://api.themoviedb.org/3/person/"+searchCriteria.getValue()+"/movie_credits?api_key=9383f37fea2d70dbfae46cb8688e0da3&language=en-US");
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return url;
    }
}
