package home.controller.search;

import home.model.Book;
import home.model.BookmarkConstants;
import home.model.SearchCriteria;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

//Strategy design pattern: Context and Client implementation
//This class behaves as the manager for the book search
//It creates a new strategy object of type SearchStategyIF
//It is getting the key from the SearchCriteria from the model and instantiates the relevant search strategy needed
public class BookSearchManager {

    SearchStrategyIF searchStrategy = null;

    public Set<Book> searchBook (SearchCriteria searchCriteria){
        Set<Book> books = new HashSet<Book>();
        URL url = null;
        //OPTION-1: instantiating the relevant search strategy using Factory pattern
        SearchStrategyFactory searchStrategyFactory = new SearchStrategyFactory();
        this.searchStrategy = searchStrategyFactory.createSearchStrategy(searchCriteria.getSearchKey());

        //using the strategy
        searchBooksAPI(books, searchStrategy.getSearchURL(searchCriteria));

        return books;


        //getting the key from searchCriteria
        //OPTION-2: instantiating the relevant search strategy based on the key
        /*
        if(searchCriteria.getSearchKey().equals(BookmarkConstants.KEY_BOOK_NAME)){
            this.searchStrategy = new BookNameSearchStrategy();
        }else if(searchCriteria.getSearchKey().equals(BookmarkConstants.KEY_BOOK_AUTHOR)){
            this.searchStrategy = new BookAuthorSearchStrategy();
        }else if(searchCriteria.getSearchKey().equals(BookmarkConstants.KEY_BOOK_GENRE)){
            this.searchStrategy = new BookGenreSearchStrategy();
        }
         */
    }

    //This method gets the json from the url
    private static void searchBooksAPI(Set<Book> books, URL url) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    informationString.append((scanner.nextLine()));
                }
                scanner.close();

                extractBookInfo(books, informationString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //this method gets the information of the books and adds it to the books hashset
    private static void extractBookInfo(Set<Book> books, StringBuilder informationString) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(String.valueOf(informationString));
        JSONArray array = new JSONArray();
        array.add(obj);

        JSONObject bookData = (JSONObject) array.get(0);
        JSONArray arr = (JSONArray) bookData.get("docs");
        for (Object o : arr) {
            JSONObject book = (JSONObject) o;
            String title = (String) book.get("title_suggest");
            JSONArray authorArr = (JSONArray) book.get("author_name");
            JSONArray isbnArr = (JSONArray) book.get("edition_key");

            String isbn = (String)isbnArr.get(0);

            ArrayList<String> authors = new ArrayList<String>();
            if(authorArr == null){
                authors.add("Unknown");
            }
            else {
                for (Object author : authorArr) {
                    authors.add((String) author);
                }
            }
            Book b = new Book(title, authors, isbn, 0);
            books.add(b);
        }
    }

    public String GetBookDescription (String id) {
        String bookDescription = "";
        try {
            URL url = new URL("https://openlibrary.org/"+id+".json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    informationString.append((scanner.nextLine()));
                }
                scanner.close();

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(String.valueOf(informationString));
                JSONArray array = new JSONArray();
                array.add(obj);
                JSONObject data = (JSONObject) array.get(0);

                bookDescription = (String) data.get("description");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookDescription;
    }


}
