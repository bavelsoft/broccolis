package com.bavelsoft.broccolies.util;

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

	public void startTest(String testName) {
		if (!isReading(testName))
			write(testName);
	}

	private boolean isReading(String testName) {
		try {
			in = xstream.createObjectInputStream(new FileReader(testName));
			return true;
		} catch (FileNotFoundException e) {
			in = null;
			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void write(String testName) {
		try {
			out = xstream.createObjectOutputStream(new PrintWriter(testName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addMessage(Object message) {
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
				boolean areEqual = (boolean)expecterMethod.invoke(recordedMessage, message);
				assertTrue("didn't publish recorded message", areEqual);
			}
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void createFileIfNecessary() {
		if (out == null)
			write("messages.xml");
	}
}
