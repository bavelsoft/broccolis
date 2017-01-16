package broccolies.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.processing.RoundEnvironment;

public class AnnotationMirrorUtil {
	public static Collection<Element> annotatedElements(RoundEnvironment env, Class... c) {
		return (Collection<Element>)Arrays.stream(c).flatMap(x->env.getElementsAnnotatedWith(x).stream()).collect(toList());
	}

	public static Collection<Map<String, Object>> annotationMaps(Element element, Class... annotationClasses) {
		Collection<Map<String, Object>> annotationMaps = new ArrayList<>();
		for (AnnotationMirror am : getAnnotationMirrors(element, annotationClasses)) {
			annotationMaps.add(
				am.getElementValues().entrySet().stream().collect(toMap(
					e->e.getKey().getSimpleName().toString(),
					e->e.getValue().getValue()
			)));
		}
		return annotationMaps;
	}

	private static Collection<AnnotationMirror> getAnnotationMirrors(Element element, Class[] annotationClasses) {
		Collection<AnnotationMirror> annMirrors = null;
		AnnotationMirror annMirrorsHolder = getAnnotationMirror(element, annotationClasses[1]);
		if (annMirrorsHolder != null) {
			Map <? extends ExecutableElement, ? extends AnnotationValue> map = annMirrorsHolder.getElementValues();
			for (ExecutableElement ee : map.keySet()) {
				if (ee.getSimpleName().toString().equals("value")) {
					annMirrors = (List<AnnotationMirror>)map.get(ee).getValue();
					break;
				}
			}
		} else {
			AnnotationMirror am = getAnnotationMirror(element, annotationClasses[0]);
			if (am == null) {
				annMirrors = Collections.emptySet();
			}  else {
				annMirrors = Collections.singleton(am);
			}
		}
		return annMirrors;
	}

	private static AnnotationMirror getAnnotationMirror(Element element, Class<?> clazz) {
		String clazzName = clazz.getName();
		for(AnnotationMirror m : element.getAnnotationMirrors()) {
			if(m.getAnnotationType().toString().equals(clazzName)) {
				return m;
			}
		}
		return null;
	}
}

