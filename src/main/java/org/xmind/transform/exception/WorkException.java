package org.xmind.transform.exception;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
public class WorkException extends RuntimeException {
    private static final long serialVersionUID = -9133032757611866784L;

    private String code;

    public WorkException(String code, String message){
        super(message);
        this.code = code;
    }
}
