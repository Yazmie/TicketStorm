package com.ticketstorm.exception;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.enums.BaseCode;
import lombok.Data;


@Data
public class TicketStormFrameException extends BaseException {

	private Integer code;
	
	private String message;

	public TicketStormFrameException() {
		super();
	}

	public TicketStormFrameException(String message) {
		super(message);
	}
	
	
	public TicketStormFrameException(String code, String message) {
		super(message);
		this.code = Integer.parseInt(code);
		this.message = message;
	}
	
	public TicketStormFrameException(Integer code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}
	
	public TicketStormFrameException(BaseCode baseCode) {
		super(baseCode.getMsg());
		this.code = baseCode.getCode();
		this.message = baseCode.getMsg();
	}
	
	public TicketStormFrameException(ApiResponse apiResponse) {
		super(apiResponse.getMessage());
		this.code = apiResponse.getCode();
		this.message = apiResponse.getMessage();
	}

	public TicketStormFrameException(Throwable cause) {
		super(cause);
	}

	public TicketStormFrameException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}

	public TicketStormFrameException(Integer code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
		this.message = message;
	}
}
