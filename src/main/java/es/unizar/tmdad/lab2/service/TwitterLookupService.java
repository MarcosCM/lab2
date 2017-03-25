package es.unizar.tmdad.lab2.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class TwitterLookupService {

	private static final int MAX_STREAMS = 10;
	
	private LinkedHashMap<String, String> connections = new LinkedHashMap<String, String>(MAX_STREAMS){
		// Remove oldest entry when max number of streams is reached
		protected boolean removeEldestEntry(Map.Entry<String, String> eldest){
			return this.size() > MAX_STREAMS;
		}
	};

	public void search(String query) {
		connections.putIfAbsent(query, query);
	}
	
	public Set<String> getQueries() {
		return connections.keySet();
	}
	
}
