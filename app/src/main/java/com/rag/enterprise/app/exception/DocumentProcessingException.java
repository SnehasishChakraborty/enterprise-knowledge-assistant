package com.rag.enterprise.app.exception;

public class DocumentProcessingException extends RuntimeException{
    public DocumentProcessingException(String message, Throwable cause){
        super(message, cause);
    }
}
