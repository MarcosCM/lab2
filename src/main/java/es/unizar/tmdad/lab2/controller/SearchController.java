package es.unizar.tmdad.lab2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import es.unizar.tmdad.lab2.domain.TwitterQuery;
import es.unizar.tmdad.lab2.service.TwitterLookupService;


@Controller
public class SearchController {
	
    @Autowired
    TwitterLookupService twitter;

    @MessageMapping("/search")
    public void search(TwitterQuery twitterQuery) {
    	twitter.search(twitterQuery.getQuery());
    }
}