package openml.io;

import java.text.ParseException;
import java.util.Date;
import java.util.Observable;

import openml.algorithms.DateParser;

public class ApiSessionHash extends Observable {
	
	private String username;
	private String sessionHash;
	private long validUntil;
	
	public ApiSessionHash() {
		sessionHash = null;
		username = null;
	}
	
	public boolean isValid() {
		Date utilDate = new Date();
		return validUntil > utilDate.getTime();
	}
	
	public void set( String username, String hash, String validUntil ) throws ParseException {
		this.username = username;
		this.sessionHash = hash;
		this.validUntil = DateParser.mysqlDateToTimeStamp(validUntil);
		setChanged();
		notifyObservers();
	}

	public String getUsername() {
		return username;
	}

	public String getSessionHash() {
		return sessionHash;
	}
}
