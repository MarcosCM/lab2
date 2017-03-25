package es.unizar.tmdad.lab2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import es.unizar.tmdad.lab2.domain.TwitterQuery;
import es.unizar.tmdad.lab2.service.TwitterLookupService;


@Controller
public class SearchController {

	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
	
    @Autowired
    TwitterLookupService twitter;

    @MessageMapping("/search")
    public void search(TwitterQuery twitterQuery) {
    	logger.info("/app/search called with param query="+twitterQuery.getQuery());
    	twitter.search(twitterQuery.getQuery());
    }
}