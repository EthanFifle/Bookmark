package testing;
import home.controller.search.BookNameSearchStrategy;
import home.controller.search.BookSearchManager;
import home.controller.search.CoverUrlExtractor;
import home.model.Book;
import home.model.SearchCriteria;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BookNameSearchStrategyTest {


    @Test
    void getSearchURLRegTitleSearch() throws MalformedURLException {
        BookNameSearchStrategy nameURL = new BookNameSearchStrategy();
        String type = "TYPE_BOOK";
        String searchKey = "KEY_BOOK_NAME";
        String value = "lord of the rings";

        URL expectedURL = new URL("https://openlibrary.org/search.json?q=lord+of+the+rings");
        SearchCriteria sc = new SearchCriteria(type,searchKey,value);
        URL actualURL = nameURL.getSearchURL(sc);

        BookSearchManager bookSearch = new BookSearchManager();
        Set<Book> result = bookSearch.searchBook(sc);

        Assertions.assertEquals(expectedURL,actualURL);
        Assertions.assertNotNull(result);
    }

    @Test
    void getSearchURLNullTitleSearch() throws MalformedURLException {
        BookNameSearchStrategy nameURL = new BookNameSearchStrategy();
        String type = "KEY_BOOK";
        String searchKey = "KEY_BOOK_NAME";
        String value = "";

        URL expectedURL = new URL("https://openlibrary.org/search.json?q=");
        SearchCriteria sc = new SearchCriteria(type,searchKey,value);
        URL actualURL = nameURL.getSearchURL(sc);

        BookSearchManager bookSearch = new BookSearchManager();
        Set<Book> result = bookSearch.searchBook(sc);

        Assertions.assertEquals(expectedURL,actualURL);
        Assertions.assertTrue(result.size() == 0);
    }

}
