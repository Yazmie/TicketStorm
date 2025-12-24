package com.ticketstorm.exception;

import lombok.Data;


@Data
public class ArgumentError {
	
	private String argumentName;
	
	private String message;
}
