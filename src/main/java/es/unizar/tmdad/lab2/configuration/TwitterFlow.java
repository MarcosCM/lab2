package es.unizar.tmdad.lab2.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.Tweet;

import es.unizar.tmdad.lab2.domain.MyTweet;
import es.unizar.tmdad.lab2.domain.TargetedTweet;
import es.unizar.tmdad.lab2.service.TwitterLookupService;

@Configuration
@EnableIntegration
@IntegrationComponentScan
@ComponentScan
public class TwitterFlow {

	@Autowired
	private TwitterLookupService twitterLookupService;
	
	@Bean
	public DirectChannel requestChannel() {
		return new DirectChannel();
	}

	// Tercer paso
	// Los mensajes se leen de "requestChannel" y se envian al método "sendTweet" del
	// componente "streamSendingService"
	@Bean
	public IntegrationFlow sendTweet() {
		return IntegrationFlows.from(requestChannel())
				// Filter --> asegurarnos que el mensaje es un Tweet
				.filter(tweet -> tweet instanceof Tweet)
				// Transform --> convertir un Tweet en un TargetedTweet con tantos tópicos como coincida
		        .<Tweet, TargetedTweet>transform(tweet ->
		        {
		        		MyTweet myTweet = new MyTweet(tweet);
		        		List<String> topics = twitterLookupService.getQueries().stream()
		        			.filter(key -> tweet.getText().toLowerCase().contains(key.toLowerCase()))
		        			.collect(Collectors.toList());
		        		return new TargetedTweet(myTweet, topics);
		        })
		        // Split --> dividir un TargetedTweet con muchos tópicos en tantos TargetedTweet como tópicos haya
		        .split(TargetedTweet.class, tweet ->
				{
					List<TargetedTweet> targetedTweets = new ArrayList<TargetedTweet>(tweet.getTargets().size());
					tweet.getTargets().stream().forEach(target ->
					{
						targetedTweets.add(new TargetedTweet(tweet.getTweet(), target));
					});
					return targetedTweets;
				})
		        // Transform --> señalar el contenido de un TargetedTweet
		        .<TargetedTweet, TargetedTweet>transform(tweet ->
				{
					String newText = tweet.getTweet().getText().replaceAll(
							"(?i)("+tweet.getFirstTarget()+")", "<strong>$1</strong>");
					tweet.getTweet().setUnmodifiedText(newText);
					return tweet;
				})
				.handle("streamSendingService", "sendTweet").get();
	}

}

// Segundo paso
// Los mensajes recibidos por este @MessagingGateway se dejan en el canal "requestChannel"
@MessagingGateway(name = "integrationStreamListener", defaultRequestChannel = "requestChannel")
interface MyStreamListener extends StreamListener {

}
