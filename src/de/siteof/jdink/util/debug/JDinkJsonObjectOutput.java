package de.siteof.jdink.util.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import flexjson.JSONSerializer;

public class JDinkJsonObjectOutput implements JDinkObjectOutput {

	private static final Log log = LogFactory.getLog(JDinkJsonObjectOutput.class);

	@Override
	public void writeObject(String name, Object o) {
		File directory = new File("./dump");
		File file = new File(directory, name + ".json");
		if (!file.exists()) {
			directory.mkdirs();
			JSONSerializer serializer = new JSONSerializer();
			serializer.include("*");
			String s = serializer.prettyPrint(o);
			OutputStream out = null;
			try {
				out = new FileOutputStream(file);
				out.write(s.getBytes());
			} catch (IOException e) {
				log.error("[writeObject] failed to write object due to " + e, e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
	}

}
