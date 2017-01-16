package broccolies.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

public abstract class GeneratorUtil {
	public static String getName(Element e) {
		String name = e.getSimpleName().toString();
		while (true) {
			e = e.getEnclosingElement();
			if (e == null || e.getKind() != ElementKind.CLASS)
				break;
			name = e.getSimpleName().toString()+"_"+name;
		}
		return name;
	}
}
