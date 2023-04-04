package home.yorku.bookmarks.controller;
import home.yorku.bookmarks.controller.database.ConnectionMethods;
import home.yorku.bookmarks.controller.search.BookSearchManager;
import home.yorku.bookmarks.controller.search.CoverUrlExtractor;
import home.yorku.bookmarks.controller.search.MovieSearchManager;
import home.yorku.bookmarks.controller.sorting.AlphaSort;
import home.yorku.bookmarks.model.*;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import java.util.concurrent.atomic.AtomicReference;

public class BookmarkController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private VBox LoginBox;
    private Stage stage;
    List<Tab> removedTabs = new ArrayList<>();
    @FXML
    private ChoiceBox<String> searchType;
    private ObservableList<String> moviesSearchOptions = FXCollections.observableArrayList(
            "Title", "Actor"
    );
    @FXML
    private ChoiceBox<String> searchBy;
    private ObservableList<String> booksSearchOptions = FXCollections.observableArrayList(
            "Title", "Genre", "Author"
    );
    @FXML
    private ChoiceBox<String> user;
    private ObservableList<String> userOptions = FXCollections.observableArrayList(
            "QA", "TA", "Client"
    );
    @FXML
    private TextField searchText;
    @FXML
    private Label ErrorChecking;
    @FXML
    private Label LoginError;
    private String searchString = "";
    @FXML
    private ListView<String> myListView;
    @FXML
    private ListView<String> myBookList;
    private ObservableList<String> bookList = FXCollections.observableArrayList();
    @FXML
    private ListView<String> myMovieList;
    private ObservableList<String> movieList = FXCollections.observableArrayList();
    @FXML
    private ListView<String> ML_myBookList;
    private ObservableList<String> MLbookList = FXCollections.observableArrayList();
    @FXML
    private ListView<String> favourite_books;
    private ObservableList<String> MLfavBooks = FXCollections.observableArrayList();
    @FXML
    private ListView<String> ML_myMovieList;
    private ObservableList<String> MLmovieList = FXCollections.observableArrayList();
    @FXML
    private ListView<String> favourite_movies;
    private ObservableList<String> MLfavMovies = FXCollections.observableArrayList();
    @FXML
    private ListView<String> upNextList;
    private ObservableList<String> futureList = FXCollections.observableArrayList();
    @FXML
    private ImageView coverImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Label authorOrRelease;
    @FXML
    private Label descLabel;
    @FXML
    private Label description;
    private Set<Movie> MovieSet;
    private Set<Book> BookSet;
    private BookPortfolio bookPortfolio;
    private MoviePortfolio moviePortfolio;
    private double sceneHeight;
    private double sceneWidth;
    private boolean logout = false;
    private String myList = "";
    private String upNext = "";

    public BookmarkController() {
    }
    // Initializes all listviews, dynamic buttons, tab views on login,
    // book/movie portfolio's for each user session to manipulate during
    // run time
    @FXML
    private void initialize() {
        // Initialize portfolios
        bookPortfolio = new BookPortfolio();
        moviePortfolio = new MoviePortfolio();
        // Initialize the list when null
        user.setItems(userOptions);
        myBookList.setItems(bookList);
        upNextList.setItems(futureList);
        myMovieList.setItems(movieList);
        ML_myBookList.setItems(MLbookList);
        ML_myMovieList.setItems(MLmovieList);
        favourite_books.setItems(MLfavBooks);
        favourite_movies.setItems(MLfavMovies);

        Scene scene = anchorPane.getScene();
        if (scene != null) {
            sceneHeight = scene.getHeight();
            sceneWidth = scene.getWidth();
        }

        // All dynamic button layouts
        anchorPane.sceneProperty().addListener((observable, oldScene, newScene) -> {

            if (newScene != null) {
                newScene.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                    sceneHeight = newHeight.doubleValue();
                    LoginBox.setLayoutY((sceneHeight/2) - (20 + LoginBox.getHeight()/2)); // 20 for padding between boxes

                });

                newScene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                    sceneWidth = newWidth.doubleValue();
                    LoginBox.setLayoutX((sceneWidth/2) - (LoginBox.getWidth()/2));

                });
            }

        });

        // Initialize locked Panes
        setPane();

        // Initialize search options for Books and Movies
        searchType.setOnAction(event -> {

            if (searchType.getValue().equals("Movies")) {
                searchBy.setItems(moviesSearchOptions);
                searchBy.setValue("Search by");
            } else if (searchType.getValue().equals("Books")) {
                searchBy.setItems(booksSearchOptions);
                searchBy.setValue("Search by");
            }

        });

    }

    // function to disable tabPanes on login/logout
    private void setPane() {
        for (Tab tab : tabPane.getTabs()) {

            if (!tab.getId().equals("LoginPane")) {
                tab.setDisable(true);
            }

        }

        Tab logoutTab = tabPane.getTabs().stream()
                .filter(tab -> tab.getId().equals("LogoutPane"))
                .findFirst()
                .orElse(null);
        removedTabs.add(logoutTab);
        tabPane.getTabs().remove(logoutTab);
    }

    // Used to clear the observable Book and Movie set returned from the search
    // Is used when the user starts a new search
    private void clearSet(){
        if(MovieSet != null){
            MovieSet.clear();
        }

        if(BookSet != null){
            BookSet.clear();
        }
    }

    // Responsible for calling the specific search managers based on user filers
    // and populating the Book and Movie observable list with the returned results from the
    // manager
    @FXML
    private void onSearchButtonClick(ActionEvent event) {

        clearSet();
        clearDescription();
        searchString = searchText.getText();
        ErrorChecking.setTextFill(Color.WHITE);

        if(searchType.getValue().equals("Movies")){ // first drop down choice box

            SearchCriteria searchCriteria;
            switch (searchBy.getValue()) {
                case "Title": {
                    ErrorChecking.setText("Searching Movies by Title: " + searchString + "...");
                    //
                    searchCriteria = new SearchCriteria(
                            BookmarkConstants.TYPE_MOVIE,
                            BookmarkConstants.KEY_MOVIE_TITLE,
                            searchString);

                    MovieSearchManager search = new MovieSearchManager();
                    MovieSet  = search.searchMovie(searchCriteria);
                    MovieController movieController = new MovieController(BookSet, MovieSet, myListView);
                    movieController.display();

                    break;
                }
                case "Actor": {
                    ErrorChecking.setText("Searching Movies by Actor: " + searchString + "...");
                    //
                    searchCriteria = new SearchCriteria(
                            BookmarkConstants.TYPE_MOVIE,
                            BookmarkConstants.KEY_MOVIE_ACTOR,
                            searchString);

                    MovieSearchManager search = new MovieSearchManager();
                    MovieSet  = search.searchMovie(searchCriteria);
                    MovieController movieController = new MovieController(BookSet, MovieSet, myListView);
                    movieController.display();

                    break;
                }
                default:
                    ErrorChecking.setTextFill(Color.RED);
                    ErrorChecking.setText("Please choose a selection from the drop down title \"Search by\" ");
                    break;
            }

            myList = "Movies I've watched";
            upNext = "Movies I want to watch";

        }else if (searchType.getValue().equals("Books")){

            SearchCriteria searchCriteria = null;
            switch (searchBy.getValue()) {
                case "Title": {
                    ErrorChecking.setText("Searching Books by Title: " + searchString + "...");
                    searchCriteria = new SearchCriteria(
                            BookmarkConstants.TYPE_BOOK,
                            BookmarkConstants.KEY_BOOK_NAME,
                            searchString);

                    BookSearchManager bookSearch = new BookSearchManager();
                    BookSet = bookSearch.searchBook(searchCriteria);

                    BookController bookController = new BookController(BookSet, MovieSet, myListView);
                    bookController.display();

                    break;
                }
                case "Genre": {
                    ErrorChecking.setText("Searching Books by Genre: " + searchString + "...");
                    searchCriteria = new SearchCriteria(
                            BookmarkConstants.TYPE_BOOK,
                            BookmarkConstants.KEY_BOOK_GENRE,
                            searchString);

                    BookSearchManager bookSearch = new BookSearchManager();
                    BookSet = bookSearch.searchBook(searchCriteria);

                    BookController bookController = new BookController(BookSet, MovieSet, myListView);
                    bookController.display();

                    break;
                }
                case "Author": {
                    ErrorChecking.setText("Searching Books by Author: " + searchString + "...");
                    searchCriteria = new SearchCriteria(
                            BookmarkConstants.TYPE_BOOK,
                            BookmarkConstants.KEY_BOOK_AUTHOR,
                            searchString);

                    BookSearchManager bookSearch = new BookSearchManager();
                    BookSet = bookSearch.searchBook(searchCriteria);

                    BookController bookController = new BookController(BookSet, MovieSet, myListView);
                    bookController.display();

                    break;
                }
                default:
                    ErrorChecking.setTextFill(Color.RED);
                    ErrorChecking.setText("Please choose a selection from the drop down title \"Search by\" ");
                    break;
            }

            myList = "Books I've read";
            upNext = "Books I want to read";

        } else {
            ErrorChecking.setTextFill(Color.RED);
            ErrorChecking.setText("Please choose a selection from the drop down title \"Type\" and \"Search by\" ");
        }

    }

    // Responsible for getting the type "Book" or "Movie" of the selected item (from the user)
    // Is used to determine which list to add the selected item to "My Book/Movie list"
    protected String getType(int selectedIndex){

        String type = "";

        if(MovieSet!=null){
            int i = 0;
            for (Movie m : MovieSet) {
                if(i == selectedIndex ){
                    type = m.getIdentifier();
                }
                i++;
            }
        }

        if(BookSet!=null){
            int i = 0;
            for (Book b : BookSet) {
                if(i == selectedIndex ){
                    type = b.getIdentifier();
                }
                i++;
            }
        }

        return type;
    }

    // Responsible for the "Save to:" dropdown button which adds Books or Movies to the respective
    // My Book/Movie list OR Future Book/Movie list
    @FXML
    private void buttonControl(MouseEvent event) {

        myListView.setCellFactory(lv -> new ListCell<String>() {

            private ComboBox<String> activeComboBox = null;
            private final StackPane stackPane = new StackPane();
            private final ComboBox<String> comboBox = new ComboBox<>();
            private final PauseTransition delay = new PauseTransition(Duration.millis(150));

            {
                comboBox.setVisible(false);
                comboBox.getItems().addAll(myList, upNext);
                comboBox.setValue("Save to:");
                comboBox.setPrefWidth(85);
                stackPane.setAlignment(Pos.CENTER_LEFT);
                StackPane.setAlignment(comboBox, Pos.CENTER_RIGHT);
                stackPane.getChildren().addAll(new Label(), comboBox);

                setOnMouseEntered(e -> {
                    if (activeComboBox != null && activeComboBox != comboBox) {
                        comboBox.setVisible(false);
                    }
                    // Start the delay before showing the ComboBox
                    delay.setOnFinished(event -> {
                        comboBox.setVisible(true);
                        activeComboBox = comboBox;
                    });
                    delay.playFromStart();
                });

                setOnMouseExited(e -> {
                    if (!isHover() && activeComboBox != null) {
                        Bounds bounds = activeComboBox.localToScene(activeComboBox.getBoundsInLocal());
                        if (!bounds.contains(e.getSceneX(), e.getSceneY())) {
                            activeComboBox.setVisible(false);
                            activeComboBox = null;
                        }
                    }

                    delay.stop();
                });

                comboBox.showingProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue && !comboBox.getSelectionModel().isEmpty()) {
                        // Call the "saveToList" function when an item is selected from the ComboBox
                        int listViewIndex = getListView().getItems().indexOf(getItem());
                        int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();

                        if (selectedIndex == 0) {
                            saveToMyCurrentList(listViewIndex);
                        } else if (selectedIndex == 1) {
                            saveToMyFutureList(listViewIndex);
                        }
                    }
                });

            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    stackPane.getChildren().set(0, label);
                    setGraphic(stackPane);
                }
            }
        });
    }

    // Updates the description view box with the description of the movie
    // If the selection is a book it will load the cover image of the book
    @FXML
    private void callDescription(MouseEvent event) throws IOException {

        final int selectedIndex = myListView.getSelectionModel().getSelectedIndex();

        clearDescription();

        if(getType(selectedIndex).equals("Movie")){

            int i = 0;
            for (Movie m : MovieSet) {
                if(i == selectedIndex){
                    descLabel.setText("Description: ");
                    descLabel.setPadding(new Insets(1, 1, 5, 5));
                    setDescription(m.getTitle(), m.getIdentifier(), "Release date: ", m.getReleaseDate());
                    description.setText(m.getOverview());
                    description.setPadding(new Insets(5, 5, 5, 5));
                }
                i++;
            }
        }

        if(getType(selectedIndex).equals("Book")){
            CoverUrlExtractor url = new CoverUrlExtractor();

            int i = 0;
            for (Book b : BookSet) {
                if(i == selectedIndex ){
                    url.getBookCover(b.getIsbn());
                    setDescription(b.getTitle(), b.getIdentifier(), "Author(s): ", b.getAuthor().toString());

                    if(url.getBookCover(b.getIsbn())){
                        InputStream stream = Files.newInputStream(Paths.get("./temporary.jpg"));
                        Image coverImage = new Image(stream);
                        coverImageView.setImage(coverImage);
                        coverImageView.setFitWidth(100);
                        coverImageView.setFitHeight(200);
                    }else{
                        InputStream stream = Files.newInputStream(Paths.get("images/book-placeholder.jpg"));
                        Image coverImage = new Image(stream);
                        coverImageView.setImage(coverImage);
                        coverImageView.setFitWidth(100);
                        coverImageView.setFitHeight(200);

                    }
                }
                i++;
            }
        }

        System.out.println("clicked on " + myListView.getSelectionModel().getSelectedItem());
    }

    // Responsible for setting, updating and formatting text labels and descriptions for movies and books
    private void setDescription(String title, String identifier, String dynamicLabel, String dynamicText){

        titleLabel.setText("Title: " + title);
        titleLabel.setPadding(new Insets(1, 1, 5, 5));
        idLabel.setText("Type: " + identifier);
        idLabel.setPadding(new Insets(1, 1, 5, 5));

        if(identifier.equals("Book")){
            String author = dynamicText.substring(1, dynamicText.length() - 1);
            authorOrRelease.setText(dynamicLabel + author);
        } else {
            authorOrRelease.setText(dynamicLabel + ": " + dynamicText);
        }
        authorOrRelease.setPadding(new Insets(1, 1, 5, 5));

    }

    private void clearDescription(){

        titleLabel.setText("");
        idLabel.setText("");
        authorOrRelease.setText("");
        descLabel.setText("");
        description.setText("");
        coverImageView.setImage(null);

    }

    // Used to connect to the database and update the listViews for "my book list" in the "MyList" tab
    // it also updates the book portfolio whenever a user adds/deletes or moves a book to/from the favourite's tab
    private void updateBooks(){

        Set<Book> localBookSet = new HashSet<Book>();
        ConnectionMethods method = new ConnectionMethods();

        this.bookPortfolio.getSavedBooks().clear();
        this.bookPortfolio.getFavouriteBooks().clear();
        bookList.clear();
        MLbookList.clear();
        MLfavBooks.clear();

        localBookSet = method.pullBooks(user.getValue());

        for (Book b : localBookSet) {

            if(b.getIsFavourite() == 1){
                this.bookPortfolio.AddToFavourites(b);
                MLfavBooks.add(b.getTitle());
                bookList.add("*" + b.getTitle());
            } else {
                this.bookPortfolio.AddToSavedBooks(b);
                MLbookList.add(b.getTitle());
                bookList.add(b.getTitle());
            }

        }

        alphaSort(myBookList, bookList);
        ML_myBookList.setItems(MLbookList);
        favourite_books.setItems(MLfavBooks);

        method.closeConnection();

    }

    // Used to connect to the database and update the listViews for "my movie list" in the "MyList" tab
    // it also updates the movie portfolio whenever a user adds/deletes or moves a movie to/from the favourite's tab
    private void updateMovies(){

        Set<Movie> localMovieSet = new HashSet<Movie>();
        ConnectionMethods method = new ConnectionMethods();

        this.moviePortfolio.getSavedMovies().clear();
        this.moviePortfolio.getFavouriteMovies().clear();
        movieList.clear();
        MLmovieList.clear();
        MLfavMovies.clear();

        localMovieSet = method.pullMovies(user.getValue());

        for (Movie m : localMovieSet) {

            if(m.getIsFavourite() == 1){
                this.moviePortfolio.AddToFavourites(m);
                MLfavMovies.add(m.getTitle());
                movieList.add("*" + m.getTitle());
            } else {
                this.moviePortfolio.AddToSavedMovies(m);
                MLmovieList.add(m.getTitle());
                movieList.add(m.getTitle());
            }

        }

        alphaSort(myMovieList, movieList);
        ML_myMovieList.setItems(MLmovieList);
        favourite_movies.setItems(MLfavMovies);

        method.closeConnection();

    }

    // Used to connect to the database and update the listViews for "my future list" in the "MyList" tab
    // it updates the list whenever a user adds/deletes movie/book to/from the list
    private void updateFutureList(){

        ConnectionMethods method = new ConnectionMethods();

        futureList = FXCollections.observableList(method.pullFutureList(user.getValue()));
        upNextList.setItems(futureList);

        method.closeConnection();
    }

    // Responsible for sending the content of book and movie objects to the database for the "My book/movie List"
    // saved objects and calling Book Movie functions to update the users list based on their actions on book/movie
    // object
    @FXML
    private void saveToMyCurrentList(int listViewIndex){

        ConnectionMethods method = new ConnectionMethods();

        if(getType(listViewIndex).equals("Book")){

            int i = 0;
            for (Book b : BookSet) {
                if(i == listViewIndex){

                    method.insertBook(b.getIsbn(), user.getValue(), b.getIdentifier(), b.getTitle(), b.getAuthor().toString(), b.getIsFavourite());
                }
                i++;
            }

            updateBooks();

        }else if(getType(listViewIndex).equals("Movie")){

            int i = 0;
            for (Movie m : MovieSet) {
                if(i == listViewIndex){

                    method.insertMovie(m.getId(), user.getValue(), m.getIdentifier(), m.getTitle(), m.getReleaseDate(), m.getOverview(),0);
                }
                i++;
            }

            updateMovies();

        }else{
            System.out.println("Error near line 557: No searchType value");
        }

    }

    // Responsible for sending the content of a book/movie object to the database table for "My Future List"
    // also updates the list the user sees by calling updateFutureList() method
    @FXML
    private void saveToMyFutureList(int listViewIndex){

        ConnectionMethods method = new ConnectionMethods();

        if(getType(listViewIndex).equals("Book")){

            int i = 0;
            for (Book b : BookSet) {
                if(i == listViewIndex){

                    method.insertFutureList(b.getIsbn(), 0L , user.getValue(), b.getIdentifier() , b.getTitle(), b.getAuthor().toString(), null, null);
                }
                i++;
            }

        }else if(getType(listViewIndex).equals("Movie")){

            int i = 0;
            for (Movie m : MovieSet) {
                if(i == listViewIndex){

                    method.insertFutureList("null", m.getId(), user.getValue(), m.getIdentifier(), m.getTitle(),  null, m.getReleaseDate(), m.getOverview());
                }
                i++;
            }

        }else{
            System.out.println("Error near line 593: No searchType value");
        }

        updateFutureList();
    }

    // Responsible for adding a book to the favourites list by updating the is_favourite flag in the database
    // using method. calls, then also calls the updateBooks() method to update the books list seen by the user
    @FXML
    private void addBookToFavourites(ActionEvent event){

        ConnectionMethods method = new ConnectionMethods();
        int selectedIndex = ML_myBookList.getSelectionModel().getSelectedIndex();

        if (selectedIndex == -1) {
            System.out.println("No Selected Item");
            return; // exit the method
        }

        int i = 0;
        for(Book b: this.bookPortfolio.getSavedBooks()){

            if(i == selectedIndex){

                method.addFavouriteBook(b.getIsbn(), user.getValue());
            }
            i++;

        }

        updateBooks();
    }

    // Responsible for removing a book to the favourites list by updating the is_favourite flag in the database
    // using method. calls, then also calls the updateBooks() method to update the books list seen by the user
    @FXML
    private void removeBookFromFavourites(ActionEvent event){

        ConnectionMethods method = new ConnectionMethods();
        int selectedIndex = favourite_books.getSelectionModel().getSelectedIndex();

        if (selectedIndex == -1) {
            System.out.println("No Selected Item");
            return; // exit the method
        }

        int i = 0;
        for(Book b: this.bookPortfolio.getFavouriteBooks()){

            if(i == selectedIndex){

                method.removeFavouriteBook(b.getIsbn(), user.getValue());
            }
            i++;

        }

        updateBooks();
    }

    // Responsible for adding a movie to the favourites list by updating the is_favourite flag in the database
    // using method. calls, then also calls the updateMovies() method to update the movies list seen by the user
    @FXML
    private void addMovieToFavourites(ActionEvent event){

        ConnectionMethods method = new ConnectionMethods();
        int selectedIndex = ML_myMovieList.getSelectionModel().getSelectedIndex();

        if (selectedIndex == -1) {
            System.out.println("No Selected Item");
            return; // exit the method
        }

        int i = 0;
        for(Movie m: this.moviePortfolio.getSavedMovies()){

            if(i == selectedIndex){

                method.addFavouriteMovie(m.getId(), user.getValue());
            }
            i++;

        }

        updateMovies();
    }

    // Responsible for removing a movie to the favourites list by updating the is_favourite flag in the database
    // using method. calls, then also calls the updateMovies() method to update the movies list seen by the user
    @FXML
    private void removeMovieFromFavourites(ActionEvent event){

        ConnectionMethods method = new ConnectionMethods();
        int selectedIndex = favourite_movies.getSelectionModel().getSelectedIndex();

        if (selectedIndex == -1) {
            System.out.println("No Selected Item");
            return; // exit the method
        }

        int i = 0;
        for(Movie m: this.moviePortfolio.getFavouriteMovies()){

            if(i == selectedIndex){

                method.removeFavouriteMovie(m.getId(), user.getValue());
            }
            i++;

        }

        updateMovies();
    }

    // Responsible for removing a book from the "My Book List" by removing the book from the database using its
    // unique id(isbn) then also calls the updateBooks() method to update the movies list seen by the user
    @FXML
    private void removeBook(ActionEvent event){

        ConnectionMethods method = new ConnectionMethods();
        int selectedIndex = ML_myBookList.getSelectionModel().getSelectedIndex();

        if (selectedIndex == -1) {
            System.out.println("No Selected Item");
            return; // exit the method
        }

        int i = 0;
        for(Book b: this.bookPortfolio.getSavedBooks()){

            if(i == selectedIndex){

                method.removeBook(b.getIsbn(), user.getValue());
            }
            i++;

        }

        updateBooks();
    }

    // Responsible for removing a movie from the "My Movie List" by removing the movie from the database using its
    // unique id then also calls the updateMovies() method to update the movies list seen by the user
    @FXML
    private void removeMovie(ActionEvent event){

        ConnectionMethods method = new ConnectionMethods();
        int selectedIndex = ML_myMovieList.getSelectionModel().getSelectedIndex();

        if (selectedIndex == -1) {
            System.out.println("No Selected Item");
            return; // exit the method
        }

        int i = 0;
        for(Movie m: this.moviePortfolio.getSavedMovies()){

            if(i == selectedIndex){

                method.removeMovie(m.getId(), user.getValue());
            }
            i++;

        }

        updateMovies();
    }

    // Responsible for removing a movie/book from the "My Future Movie/Book List" by removing the movie/book
    // from the database using its unique id then also calls the updateFutureList() method to update the
    // movies/books list seen by the user
    @FXML
    private void removeFutureList(ActionEvent event){
        //Need to change, will be problematic in the future
        ConnectionMethods method = new ConnectionMethods();

        String selectedItem = upNextList.getSelectionModel().getSelectedItem();
        final int selectedIndex = upNextList.getSelectionModel().getSelectedIndex();

        if (selectedIndex == -1) {
            System.out.println("No Selected Item");
            return; // exit the method
        }

        method.removeFutureList(selectedItem, user.getValue());
        updateFutureList();
    }

    // Responsible for sorting the visible Book list alphabetically as well as the book portfolio
    // so that the references to unique Ids remain intact
    @FXML
    private void sortAlphaBook(MouseEvent event){

        this.bookPortfolio.getSavedBooks().sort(new Comparator<Book>() {
            @Override
            public int compare(Book b1, Book b2) {
                return b1.getTitle().compareTo(b2.getTitle());
            }
        });

        alphaSort(ML_myBookList, MLbookList);
    }

    // Responsible for sorting the visible Movie list alphabetically as well as the movie portfolio
    // so that the references to unique Ids remain intact
    @FXML
    private void sortAlphaMovie(MouseEvent event){
        this.moviePortfolio.getSavedMovies().sort(new Comparator<Movie>() {
            @Override
            public int compare(Movie m1, Movie m2) {
                return m1.getTitle().compareTo(m2.getTitle());
            }
        });

        alphaSort(ML_myMovieList, MLmovieList);
    }

    // Responsible actually sorting all the visible Book/Movie lists by converting the observable list to an
    // array list, sorting it using alphaSort. methods, the returning the sorted array as an observable array
    private void alphaSort(ListView<String> list, ObservableList<String> items){

        AlphaSort alphaSort = new AlphaSort();

        ArrayList<String> arrayList = new ArrayList<>(list.getItems());
        arrayList = alphaSort.sortMovies(arrayList);

        list.getItems().removeAll(items);
        list.getItems().addAll(arrayList);
    }

    // Responsible for displaying, locking and unlocking tabs at login as well as updating all users list
    // on login
    public void login(MouseEvent mouseEvent) {

        ConnectionMethods method = new ConnectionMethods();

        if(!user.getValue().equals("Team:")){
            method.userLogin(user.getValue(), "Login");
            updateBooks();
            updateMovies();
            updateFutureList();
            logout = false;
            stage = (Stage) tabPane.getScene().getWindow();
            stage.setWidth(900);
            stage.setHeight(680);
            listen();

            tabPane.getTabs().addAll(removedTabs);
            removedTabs.clear();

            for (Tab tab : tabPane.getTabs()) {
                tab.setDisable(false);
            }

            // Enable and show the login tab
            Tab loginTab = tabPane.getTabs().stream()
                    .filter(tab -> tab.getId().equals("LoginPane"))
                    .findFirst()
                    .orElse(null);
            removedTabs.add(loginTab);
            tabPane.getTabs().remove(loginTab);
        }else{
            LoginError.setTextFill(Color.RED);
            LoginError.setText("Please select your Team to Login");
        }
    }

    @FXML
    private void clearSearchTab(){
        myListView.getItems().clear();
        searchText.clear();
        ErrorChecking.setText("");
        searchType.setValue("Type");
        searchBy.setValue("Search by");
        user.setValue("Team: ");
    }

    // Responsible for locking, unlocking and displaying tabs when the user logs out
    public void logout(MouseEvent mouseEvent) {

        ConnectionMethods method = new ConnectionMethods();
        method.userLogin(user.getValue(), "Logout");
        logout = true;
        stage = (Stage) tabPane.getScene().getWindow();
        stage.setWidth(300);
        stage.setHeight(300);
        listen();
        clearSet();
        clearSearchTab();
        clearDescription();

        tabPane.getTabs().add(0,removedTabs.get(0));
        removedTabs.clear();

        setPane();
        LoginError.setText("");
    }

    // Responsible for logging the user out if they click the close button instead of logout button
    public void listen() {
        ConnectionMethods method = new ConnectionMethods();
        stage = (Stage) tabPane.getScene().getWindow();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

                if(!logout){
                    method.userLogin(user.getValue(), "Logout");
                    clearSet();
                    clearSearchTab();
                    clearDescription();
                }

            }
        });
    }

}