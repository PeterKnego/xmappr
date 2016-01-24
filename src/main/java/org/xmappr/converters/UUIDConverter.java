package org.xmappr.converters;

import java.util.UUID;

public class UUIDConverter extends ValueConverter {

	public boolean canConvert(Class type) {
		return java.util.UUID.class.isAssignableFrom(type);
	}

	@Override
	public Object fromValue(String value, String format, Class targetType, Object targetObject) {
		return UUID.fromString(value);
	}

	@Override
	public String toValue(Object object, String format) {
		return ((UUID) object).toString();
	}
}
