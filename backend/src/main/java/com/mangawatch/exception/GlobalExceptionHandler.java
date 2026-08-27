package com.mangawatch.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/* This is a stopgap: matching on ex.getMessage().contains("not found") is
 * fragile long-term. The cleaner fix is custom exception types (e.g. a
 * ResourceNotFoundException) with their own @ExceptionHandler methods, so
 * you're not relying on string matching. Worth revisiting once you have a
 * few more of these "not found" cases across the codebase.
 */
//Catches unhandled exceptions across all controllers, and gives out a cleaner
//http response instead of letting them run as raw stack traces
//same for test failures
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<?> handleRuntimeException(RuntimeException ex){
		String message = ex.getMessage();
		
		if (message != null && message.toLowerCase().contains("not found")) {
			return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", message));
		}
		
		//In cases where something unexpected happens, leak nothing integral
		//instead we give a message that just says it, not a str8 500 error
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Whoops, an unexpected Error Occured"));
	}

}
