package es.unizar.tmdad.lab2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.social.twitter.api.FilterStreamParameters;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import es.unizar.tmdad.lab2.domain.TargetedTweet;

@Service
public class StreamSendingService {
	
	private static final Logger logger = LoggerFactory.getLogger(StreamSendingService.class);

	@Autowired
	private SimpMessageSendingOperations ops;
	
	@Autowired
	private TwitterTemplate twitterTemplate;

	@Autowired
	private TwitterLookupService lookupService;
	
	private Stream stream;

	@Autowired
	private StreamListener integrationStreamListener;

	@PostConstruct
	public void initialize() {
		//FilterStreamParameters fsp = new FilterStreamParameters();
		//fsp.addLocation(-180, -90, 180, 90);

		// Primer paso
		// Registro un gateway para recibir los mensajes
		// Ver @MessagingGateway en MyStreamListener en TwitterFlow.java
		//stream = twitterTemplate.streamingOperations().filter(fsp, Collections.singletonList(integrationStreamListener));
		stream = twitterTemplate.streamingOperations().sample(Collections.singletonList(integrationStreamListener));
	}

	// Cuarto paso
	// Recibe un tweet y hay que enviarlo a tantos canales como preguntas hay registradas en lookupService
	//
	public void sendTweet(Tweet tweet) {
		logger.info("Got Tweet: " + tweet.getText());
		Map<String, Object> headers = new HashMap<>();
		headers.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);

		// Expresión lambda: si el tweet contiene s, devuelve true
		Predicate<String> containsTopic = s -> tweet.getText().contains(s);
		// Expresión lambda: envia un tweet al canal asociado al tópico s
		Consumer<String> convertAndSend = s -> ops.convertAndSend("/queue/search/" + s, tweet, headers);

		lookupService.getQueries().stream().filter(containsTopic).forEach(convertAndSend);
	}

	public void sendTweet(TargetedTweet tweet) {
		logger.info("Got TargetedTweet: " + tweet.getTweet().getText());
		//
		// CAMBIOS A REALIZAR:
		//
		// Crea un mensaje que envie un tweet a un único tópico destinatario
		//
		Map<String, Object> headers = new HashMap<>();
		headers.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
		ops.convertAndSend("/queue/search/" + tweet.getFirstTarget(), tweet.getTweet(), headers);
	}


	public Stream getStream() {
		return stream;
	}

}
