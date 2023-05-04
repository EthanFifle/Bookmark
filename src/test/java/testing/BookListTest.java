package testing;

import home.controller.BookmarkController;
import home.controller.PortfolioController;
import home.controller.search.BookSearchManager;

import home.model.*;
import org.junit.Test;
import java.lang.reflect.Method;


public class BookListTest {

    @Test
    public void bookListTest() throws Exception {
        SearchCriteria sc = new SearchCriteria(
                BookmarkConstants.TYPE_BOOK,
                BookmarkConstants.KEY_BOOK_NAME,
                "the ghost of graylock");

        BookSearchManager bookSearch = new BookSearchManager();
        PortfolioController pc = new PortfolioController(new BookmarkController());
        Method method = PortfolioController.class.getDeclaredMethod("updateBookPortfolio", BookToPortfolio.class, String.class);
        method.setAccessible(true);
        //method.invoke(pc,book, "AddToSavedBooks");

    }


}
