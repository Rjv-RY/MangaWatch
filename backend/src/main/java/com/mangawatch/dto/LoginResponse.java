package com.mangawatch.dto;

//Files for Login/Register

public class LoginResponse {
	private String message;
	
	public LoginResponse(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
