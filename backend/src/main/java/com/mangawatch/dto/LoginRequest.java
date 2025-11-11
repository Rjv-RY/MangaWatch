package com.mangawatch.dto;

//Files for Login/Register

public class LoginRequest {
    private String username;
    private String password;
    
    // gets and sets
	public String getUsername() {return username;}
	public void setUsername(String username) {this.username = username;}
	
	public String getPassword() {return password;}
	public void setPassword(String password) {this.password = password;}
}
