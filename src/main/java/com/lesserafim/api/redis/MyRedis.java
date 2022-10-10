package com.lesserafim.api.redis;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RTransaction;
import org.springframework.transaction.TransactionStatus;

public interface MyRedis {
	public RLock lock(String lockKey);

	public RLock lock(String lockKey, long timeout);

	public RLock lock(String lockKey, TimeUnit unit, long timeout);

	public boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime);

	public void unlock(String lockKey);

	public void unlock(RLock lock);
    
	public Object getValue(String key);
    
	public void setValue(String key, Object value);
	
    public boolean deleteKey(String key);

	public TransactionStatus startDBTransacton();

	public void commitDB(TransactionStatus status);

	public void rollbackDB(TransactionStatus status);

	public RTransaction startRedisTransacton();

	public void commitRedis(RTransaction transaction);

	public void rollbackRedis(RTransaction transaction);

	public boolean canUnlock(String lockKey);
}