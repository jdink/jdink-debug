package de.siteof.jdink.util.debug;

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JDinkXmlEncoderObjectOutput implements JDinkObjectOutput {

	private static final Log log = LogFactory.getLog(JDinkXmlEncoderObjectOutput.class);

	@Override
	public void writeObject(String name, Object o) {
		File directory = new File("./dump");
		File file = new File(directory, name + ".xml");
		if (!file.exists()) {
			file.mkdirs();
			OutputStream out = null;
			try {
				out = new FileOutputStream(file);
				XMLEncoder encoder = new XMLEncoder(out);
				encoder.writeObject(o);
				encoder.close();
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
