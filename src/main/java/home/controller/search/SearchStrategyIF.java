package home.controller.search;

import home.model.SearchCriteria;

import java.net.URL;

//Strategy design pattern: Strategy Interface
public interface SearchStrategyIF {
    public URL getSearchURL(SearchCriteria searchCriteria);
}
