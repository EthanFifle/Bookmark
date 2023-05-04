package home.controller.search;

import home.model.SearchCriteria;

import java.net.MalformedURLException;
import java.net.URL;

//Strategy design pattern: Concrete Strategy
public class BookAuthorSearchStrategy implements SearchStrategyIF {
    //Giving interface method relevant body
    //Search criteria is in the model, it is used by both the frontend and the backend
    //Getting the search key from searchCriteria then returning the appropriate url associated with the key
    @Override
    public URL getSearchURL(SearchCriteria searchCriteria) {
        System.out.println("BookAuthorSearchStrategy in action");
        String search = searchCriteria.getValue().replaceAll(" ", "%20");
        URL url = null;
        try {
            url = new URL("https://openlibrary.org/search.json?author=" + search + "&sort=new");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return url;
    }
}
