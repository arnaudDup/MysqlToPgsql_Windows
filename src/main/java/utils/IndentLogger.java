package utils;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;

import com.mysql.jdbc.StringUtils;

/**
 * Class used to wrap a SLF4J logger and indent log output
 * 
 * @author FM2685EN
 */
public class IndentLogger implements Logger {
	// ===========================================================
	// Fields
	// ===========================================================
	private Logger parentLogger;

	// ===========================================================
	// Constructors
	// ===========================================================
	public IndentLogger(Logger parentLogger) {
		this.parentLogger = parentLogger;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public void debugBlockStart(String blockName) {
		debugBlockStart(blockName, "");
	}
	public void debugBlockStart(String blockName, String logStr) {
		debug("-> " + blockName + " " + logStr);
		incLogDepth();
	}
	
	public void debugBlockEnd(String blockName) {
		debugBlockEnd(blockName, "");
	}
	public void debugBlockEnd(String blockName, String logStr) {
		decLogDepth();
		debug("<- " + blockName + " " + logStr);
	}
	
	public void infoBlockStart(String blockName) {
		infoBlockStart(blockName, "");
	}
	public void infoBlockStart(String blockName, String logStr) {
		info("-> " + blockName + " " + logStr);
		incLogDepth();
	}
	
	public void infoBlockEnd(String blockName) {
		debugBlockEnd(blockName, "");
	}
	public void infoBlockEnd(String blockName, String logStr) {
		int initialDepth = getBlockDepth();
		decLogDepth();
		info((initialDepth == 0 ? "": "<- ") + blockName + " " + logStr);
	}

	public void errorBlockStart(String blockName, String logStr, Throwable e) {
		error("-> " + blockName + " " + logStr, e);
		incLogDepth();
	}
	
	public void errorBlockEnd(String blockName, String logStr, Throwable e) {
		int initialDepth = getBlockDepth();
		decLogDepth();
		error((initialDepth == 0 ? "": "<- ") + blockName + " " + logStr, e);
	}
	
	public void resetBlockDepth() {
		setBlockDepth(0);
	}
	
	private int getBlockDepth() {
		String depthStr = MDC.get("depth");
		return depthStr == null ? 0 : Integer.parseInt(depthStr);
	}
	
	private void setBlockDepth(int depth) {
		MDC.put("depth", "" + depth);
	}
	
	private void incLogDepth() {
		try {
			int depth = getBlockDepth();
			depth++;
			setBlockDepth(depth);
		} catch (Exception e) {
			error("incLogDepth() silent KO: " + e, e);
			MDC.put("depth", "0");
			MDC.put("depthTabs", "");
		}
	}
	
	private void decLogDepth() {
		try {
			int depth = getBlockDepth();
			depth = depth > 0 ? depth-1 : 0;
			setBlockDepth(depth);
		} catch (Exception e) {
			error("incLogDepth() silent KO: " + e, e);
			MDC.put("depth", "0");
			MDC.put("depthTabs", "");
		}
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
		parentLogger.debug(arg0, arg1, arg2, arg3);
	}

	public void debug(Marker arg0, String arg1, Object... arg2) {
		parentLogger.debug(arg0, arg1, arg2);
	}

	public void debug(Marker arg0, String arg1, Object arg2) {
		parentLogger.debug(arg0, arg1, arg2);
	}

	public void debug(Marker arg0, String arg1, Throwable arg2) {
		parentLogger.debug(arg0, arg1, arg2);
	}

	public void debug(Marker arg0, String arg1) {
		parentLogger.debug(arg0, arg1);
	}

	public void debug(String arg0, Object arg1, Object arg2) {
		parentLogger.debug(arg0, arg1, arg2);
	}

	public void debug(String arg0, Object... arg1) {
		parentLogger.debug(arg0, arg1);
	}

	public void debug(String arg0, Object arg1) {
		parentLogger.debug(arg0, arg1);
	}

	public void debug(String arg0, Throwable arg1) {
		parentLogger.debug(arg0, arg1);
	}

	public void debug(String arg0) {
		parentLogger.debug(arg0);
	}

	public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
		parentLogger.error(arg0, arg1, arg2, arg3);
	}

	public void error(Marker arg0, String arg1, Object... arg2) {
		parentLogger.error(arg0, arg1, arg2);
	}

	public void error(Marker arg0, String arg1, Object arg2) {
		parentLogger.error(arg0, arg1, arg2);
	}

	public void error(Marker arg0, String arg1, Throwable arg2) {
		parentLogger.error(arg0, arg1, arg2);
	}

	public void error(Marker arg0, String arg1) {
		parentLogger.error(arg0, arg1);
	}

	public void error(String arg0, Object arg1, Object arg2) {
		parentLogger.error(arg0, arg1, arg2);
	}

	public void error(String arg0, Object... arg1) {
		parentLogger.error(arg0, arg1);
	}

	public void error(String arg0, Object arg1) {
		parentLogger.error(arg0, arg1);
	}

	public void error(String arg0, Throwable arg1) {
		parentLogger.error(arg0, arg1);
	}

	public void error(String arg0) {
		parentLogger.error(arg0);
	}

	public String getName() {
		return parentLogger.getName();
	}

	public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
		parentLogger.info(arg0, arg1, arg2, arg3);
	}

	public void info(Marker arg0, String arg1, Object... arg2) {
		parentLogger.info(arg0, arg1, arg2);
	}

	public void info(Marker arg0, String arg1, Object arg2) {
		parentLogger.info(arg0, arg1, arg2);
	}

	public void info(Marker arg0, String arg1, Throwable arg2) {
		parentLogger.info(arg0, arg1, arg2);
	}

	public void info(Marker arg0, String arg1) {
		parentLogger.info(arg0, arg1);
	}

	public void info(String arg0, Object arg1, Object arg2) {
		parentLogger.info(arg0, arg1, arg2);
	}

	public void info(String arg0, Object... arg1) {
		parentLogger.info(arg0, arg1);
	}

	public void info(String arg0, Object arg1) {
		parentLogger.info(arg0, arg1);
	}

	public void info(String arg0, Throwable arg1) {
		parentLogger.info(arg0, arg1);
	}

	public void info(String arg0) {
		parentLogger.info(arg0);
	}

	public boolean isDebugEnabled() {
		return parentLogger.isDebugEnabled();
	}

	public boolean isDebugEnabled(Marker arg0) {
		return parentLogger.isDebugEnabled(arg0);
	}

	public boolean isErrorEnabled() {
		return parentLogger.isErrorEnabled();
	}

	public boolean isErrorEnabled(Marker arg0) {
		return parentLogger.isErrorEnabled(arg0);
	}

	public boolean isInfoEnabled() {
		return parentLogger.isInfoEnabled();
	}

	public boolean isInfoEnabled(Marker arg0) {
		return parentLogger.isInfoEnabled(arg0);
	}

	public boolean isTraceEnabled() {
		return parentLogger.isTraceEnabled();
	}

	public boolean isTraceEnabled(Marker arg0) {
		return parentLogger.isTraceEnabled(arg0);
	}

	public boolean isWarnEnabled() {
		return parentLogger.isWarnEnabled();
	}

	public boolean isWarnEnabled(Marker arg0) {
		return parentLogger.isWarnEnabled(arg0);
	}

	public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
		parentLogger.trace(arg0, arg1, arg2, arg3);
	}

	public void trace(Marker arg0, String arg1, Object... arg2) {
		parentLogger.trace(arg0, arg1, arg2);
	}

	public void trace(Marker arg0, String arg1, Object arg2) {
		parentLogger.trace(arg0, arg1, arg2);
	}

	public void trace(Marker arg0, String arg1, Throwable arg2) {
		parentLogger.trace(arg0, arg1, arg2);
	}

	public void trace(Marker arg0, String arg1) {
		parentLogger.trace(arg0, arg1);
	}

	public void trace(String arg0, Object arg1, Object arg2) {
		parentLogger.trace(arg0, arg1, arg2);
	}

	public void trace(String arg0, Object... arg1) {
		parentLogger.trace(arg0, arg1);
	}

	public void trace(String arg0, Object arg1) {
		parentLogger.trace(arg0, arg1);
	}

	public void trace(String arg0, Throwable arg1) {
		parentLogger.trace(arg0, arg1);
	}

	public void trace(String arg0) {
		parentLogger.trace(arg0);
	}

	public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
		parentLogger.warn(arg0, arg1, arg2, arg3);
	}

	public void warn(Marker arg0, String arg1, Object... arg2) {
		parentLogger.warn(arg0, arg1, arg2);
	}

	public void warn(Marker arg0, String arg1, Object arg2) {
		parentLogger.warn(arg0, arg1, arg2);
	}

	public void warn(Marker arg0, String arg1, Throwable arg2) {
		parentLogger.warn(arg0, arg1, arg2);
	}

	public void warn(Marker arg0, String arg1) {
		parentLogger.warn(arg0, arg1);
	}

	public void warn(String arg0, Object arg1, Object arg2) {
		parentLogger.warn(arg0, arg1, arg2);
	}

	public void warn(String arg0, Object... arg1) {
		parentLogger.warn(arg0, arg1);
	}

	public void warn(String arg0, Object arg1) {
		parentLogger.warn(arg0, arg1);
	}

	public void warn(String arg0, Throwable arg1) {
		parentLogger.warn(arg0, arg1);
	}

	public void warn(String arg0) {
		parentLogger.warn(arg0);
	}	
}
