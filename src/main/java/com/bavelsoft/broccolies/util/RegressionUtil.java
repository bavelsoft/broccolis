package com.bavelsoft.broccolies.util;

import java.io.Closeable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.thoughtworks.xstream.XStream;

import static org.junit.Assert.assertTrue;

public class RegressionUtil {
	public static RegressionUtil ru = new RegressionUtil();

	private XStream xstream = new XStream();
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private static final String prefix = "target/"; 

	public void startTest(String testName) {
		String fullTestName=prefix+testName;
		try {
			read(fullTestName);
		} catch (FileNotFoundException e) {
			write(fullTestName);
		}
	}

	public void stopTest() {
		close(out);
		out = null;
		close(in);
		in = null;
	}

	private void read(String testName) throws FileNotFoundException {
//System.err.println("*** read "+testName);
		try {
			in = xstream.createObjectInputStream(new FileReader(testName));
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void write(String testName) {
//System.err.println("*** write "+testName);
		try {
			out = xstream.createObjectOutputStream(new PrintWriter(testName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void close(Closeable c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addMessage(Object message) {
//System.err.println("*** addMessage");
		try {
			if (in == null) {
				createFileIfNecessary();
				out.writeObject(message);
				out.flush();
			} else {
				Object recordedMessage = in.readObject();
//TODO instead of reflection, generate a mapping of class to method ref
				String expecterClass = recordedMessage.getClass().getName() + "Expecter";
				Method expecterMethod = Class.forName(expecterClass).getMethod("equals", Object.class, Object.class);
				boolean areEqual = (boolean)expecterMethod.invoke(null, recordedMessage, message);
				assertTrue("didn't publish recorded message", areEqual);
			}
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void createFileIfNecessary() throws IOException {
		if (out == null)
			write(prefix+"messages.xml");
	}
}
