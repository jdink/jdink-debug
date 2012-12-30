package de.siteof.jdink.util.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JDinkXmlReflectionObjectOutput implements JDinkObjectOutput {

	private static final Log log = LogFactory.getLog(JDinkXmlReflectionObjectOutput.class);

	private final File directory;

	public JDinkXmlReflectionObjectOutput(File directory) {
		this.directory = directory;
	}

	@Override
	public void writeObject(String name, Object o) {
		File file = new File(directory, name + ".xml");
		if (!file.exists()) {
			directory.mkdirs();
			JDinkXmlReflectionSerializer serializer = new JDinkXmlReflectionSerializer();
			OutputStream out = null;
			try {
				out = new FileOutputStream(file);
				serializer.serialize(out, o);
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
