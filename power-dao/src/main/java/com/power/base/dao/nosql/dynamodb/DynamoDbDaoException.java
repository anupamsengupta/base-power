package com.power.base.dao.nosql.dynamodb;

/**
 * Runtime exception thrown when DynamoDB operations fail.
 */
public class DynamoDbDaoException extends RuntimeException {

    public DynamoDbDaoException(String message) {
        super(message);
    }

    public DynamoDbDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}

