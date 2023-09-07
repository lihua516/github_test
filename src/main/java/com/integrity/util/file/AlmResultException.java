package com.integrity.util.file;


import lombok.Data;

/**
 * 用户不存在异常
 *
 * @author
 */
@Data
public class AlmResultException extends RuntimeException {

	private  Integer resultCode;

	private  String errorMsgZh;

	private  String errorMsgEn;

	public AlmResultException(int code, String msgZh, String msgEn){
		super();
		this.resultCode = code;
		this.errorMsgZh = msgZh;
		this.errorMsgEn = msgEn;
	}
}
