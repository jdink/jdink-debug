package de.siteof.jdink.util.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class JDinkXStreamObjectOutput implements JDinkObjectOutput {

	private static final Log log = LogFactory.getLog(JDinkXStreamObjectOutput.class);
	
	private final File directory;
	
	public JDinkXStreamObjectOutput(File directory) {
		this.directory = directory;
	}

	@Override
	public void writeObject(String name, Object o) {
//		File directory = new File("./dump");
		File file = new File(directory, name + ".xml");
		if (!file.exists()) {
			directory.mkdirs();
			XStream xstream = new XStream(new DomDriver());
			String s = xstream.toXML(o);
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
