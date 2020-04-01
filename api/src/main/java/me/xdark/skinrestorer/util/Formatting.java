package me.xdark.skinrestorer.util;

import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class Formatting {
	public String throwable(Throwable t) {
		val trace = t.getStackTrace();
		return t.getClass().getSimpleName() + " : " + t.getMessage() + (trace.length > 0 ? " @ " + t.getStackTrace()[0].getClassName() + ":" + t.getStackTrace()[0].getLineNumber() : "");
	}
}
