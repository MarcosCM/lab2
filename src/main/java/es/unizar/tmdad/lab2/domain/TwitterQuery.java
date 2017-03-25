package es.unizar.tmdad.lab2.domain;

public class TwitterQuery {
	
	private String query;
	
	public TwitterQuery(){
		
	}
	
	public TwitterQuery(String query){
		this.query = query;
	}
	
	public String getQuery(){
		return query;
	}
	
	public void setQuery(String query){
		this.query = query;
	}
}