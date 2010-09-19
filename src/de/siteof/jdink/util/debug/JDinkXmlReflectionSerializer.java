package de.siteof.jdink.util.debug;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JDinkXmlReflectionSerializer {

	private static final Log log = LogFactory.getLog(JDinkXmlReflectionSerializer.class);

	private boolean ignoreNull = true;
	private final Map<String, Field[]> fieldMap = new HashMap<String, Field[]>();
	private final Map<Object, String> referenceTracker = new IdentityHashMap<Object, String>();

	public void serialize(OutputStream out, Object o) {
		BufferedOutputStream bufferedOut = new BufferedOutputStream(out, 4096);
		PrintWriter pw = new PrintWriter(bufferedOut);
		serialize(pw, null, o, 0, "");
		pw.flush();
	}

	private Field[] getFields(Class<?> type) {
		Field[] result = fieldMap.get(type.getName());
		if (result == null) {
			Field[] fields = type.getDeclaredFields();
			List<Field> fieldList = new ArrayList<Field>(fields.length);
			for (Field field: fields) {
				if (!Modifier.isStatic(field.getModifiers())) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					fieldList.add(field);
				}
			}
			if (!type.isArray()) {
				Class<?> superClass = type.getSuperclass();
				if (superClass != null) {
					Field[] superFields = this.getFields(superClass);
					fieldList.addAll(Arrays.asList(superFields));
				}
			}
			fields = fieldList.toArray(new Field[fieldList.size()]);
			Arrays.sort(fields, new Comparator<Field>() {
				@Override
				public int compare(Field o1, Field o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			result = fields;
			fieldMap.put(type.getName(), result);
		}
		return result;
	}

	private void serialize(PrintWriter out, Class<?> type, Object o, int level, String propertyName) {
		if ((type == null) && (o != null)) {
			type = o.getClass();
		}
		if (o == null) {
			out.write("<null");
			if (propertyName != null) {
				this.writeAttribute(out, type, o, level, "name", propertyName);
			}
			out.write("/>\n");
		} else if ((o instanceof CharSequence) || (o instanceof Number)) {
			serializeInline(out, type, o, level, propertyName);
		} else {
			String reference = referenceTracker.get(o);
			if (reference != null) {
				this.writeObjectBegin(out, type, o, level, propertyName);
				this.writeAttribute(out, type, o, level, "ref", reference);
			} else {
				reference = "TODO";
				this.referenceTracker.put(o, reference);
				if (o.getClass().isArray()) {
					this.writeObjectBegin(out, type, o, level, propertyName);
					int length = Array.getLength(o);
					this.writeAttribute(out, type, o, level, "length", Integer.toString(length));
					if (length == 0) {
						out.write("/>\n");
					} else {
						out.write(">\n");
						for (int i = 0; i < length; i++) {
							Object value;
							try {
								value = Array.get(o, i);
							} catch (Throwable e) {
								log.error("failed to retrieve array value due to " + e, e);
								value = null;
							}
							if ((value != null) || (!ignoreNull)) {
								this.serialize(out, o.getClass().getComponentType(),
										value, level + 1, Integer.toString(i));
							}
						}
						this.writeObjectEnd(out, type, o, level, propertyName);
					}
				} else {
					Field[] fields = getFields(o.getClass());
					this.writeObjectBegin(out, type, o, level, propertyName);
					if (fields.length == 0) {
						out.write("/>\n");
					} else {
						out.write(">\n");
						for (Field field: fields) {
							Object value;
							try {
								value = field.get(o);
							} catch (Throwable e) {
								log.error("failed to retrieve value due to " + e, e);
								value = null;
							}
							if ((value != null) || (!ignoreNull)) {
								this.serialize(out, field.getType(), value, level + 1, field.getName());
							}
						}
						this.writeObjectEnd(out, type, o, level, propertyName);
					}
				}
			}
		}
	}

	private void serializeInline(PrintWriter out, Class<?> type, Object o, int level, String propertyName) {
		this.writeObjectBegin(out, type, o, level, propertyName);
		this.writeAttribute(out, type, o, level, "value", o.toString());
		out.write("/>\n");
	}

	private void writeIndent(PrintWriter out, int level) {
		int length = 2 * level;
		for (int i = 0; i < length; i++) {
			out.write(' ');
		}
	}

	private void writeObjectBegin(PrintWriter out, Class<?> type, Object o, int level, String propertyName) {
		this.writeIndent(out, level);
		out.write("<");
		out.write(getTypeString(o.getClass()));
		if (propertyName != null) {
			this.writeAttribute(out, type, o, level, "name", propertyName);
		}
	}

	private void writeAttribute(PrintWriter out, Class<?> type, Object o, int level, String attributeName, String value) {
		out.write(" ");
		out.write(attributeName);
		out.write("=\"");
		out.write(value);
		out.write("\"");
	}

	private String getTypeString(Class<?> type) {
		String result;
		if (type.isArray()) {
			result = getTypeString(type.getComponentType()) + "[]";
		} else {
			result = type.getName();
		}
		return result;
	}

	private void writeObjectEnd(PrintWriter out, Class<?> type, Object o, int level, String propertyName) {
		this.writeIndent(out, level);
		out.write("</");
		out.write(getTypeString(o.getClass()));
		out.write(">\n");
	}

	public boolean isIgnoreNull() {
		return ignoreNull;
	}

	public void setIgnoreNull(boolean ignoreNull) {
		this.ignoreNull = ignoreNull;
	}

}
