package search_procedures;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.Code;

public class EnumeratorLogger<SomeCode extends Code> implements ICodeEnumerator<SomeCode> {
	static final private Logger logger = LoggerFactory.getLogger(EnumeratorLogger.class);
	private ICodeEnumerator<SomeCode> enumerator;
	private double totalCodesCount;
	private int viewedCodesCount = 0;
	private long startTime;
	private long lastOutline;
	private LoggingMode mode;
	
	public static enum LoggingMode {
		TimeLogging,
		CountLogging
	}
	
	public EnumeratorLogger(ICodeEnumerator<SomeCode> enumerator) {
		this.enumerator = enumerator;
		this.mode = LoggingMode.TimeLogging;
		this.totalCodesCount = enumerator.count().doubleValue();
		logger.info("total codes: " + totalCodesCount);
	}
	
	public EnumeratorLogger(ICodeEnumerator<SomeCode> enumerator, LoggingMode mode) {
		this.enumerator = enumerator;
		this.mode = mode;
		if (mode == LoggingMode.TimeLogging) {
			this.totalCodesCount = enumerator.count().doubleValue();
			logger.info("total codes: " + totalCodesCount);
		}
	}
	
	@Override
	public void reset() {
		enumerator.reset();	
		viewedCodesCount = 0;		
	}
	
	@Override
	public SomeCode next() {
		if (viewedCodesCount == 0) {
			startTime = System.currentTimeMillis();
			lastOutline = startTime;
		}
		
		++viewedCodesCount;			
		if (mode == LoggingMode.TimeLogging) {
			if ((System.currentTimeMillis() - lastOutline) > 1000) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				double speed = viewedCodesCount * 1000 / elapsedTime;
				double remainingTime = ((totalCodesCount - viewedCodesCount) * elapsedTime / 1000 / viewedCodesCount);
						
				logger.info("codes viewed: " + viewedCodesCount + ", remaining time: " + remainingTime + "s, speed: " + speed + "c/s");
				lastOutline = System.currentTimeMillis();
			}
		} else if (mode == LoggingMode.CountLogging) {
			if (viewedCodesCount % 10000 == 0) {
				logger.info("codes viewed: " + viewedCodesCount);
			}
		}
		
		return enumerator.next();
	}
	
	@Override
	public BigInteger count() {		
		return enumerator.count();
	}
}
