package com.example.propertysupervision.protocol.inbound;

/**
 * 相同报文编号的业务单被另一个事务抢先创建，需要回滚后重新处理。
 */
public class ConcurrentBusinessOrderCreationException extends RuntimeException {

    public ConcurrentBusinessOrderCreationException(String messageNo, Throwable cause) {
        super("业务单已被并发请求创建，messageNo=" + messageNo, cause);
    }
}
