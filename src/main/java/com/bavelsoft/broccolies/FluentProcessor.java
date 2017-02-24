package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentExpecter;
import com.bavelsoft.broccolies.annotation.FluentExpecters;
import com.bavelsoft.broccolies.annotation.FluentKey;
import com.bavelsoft.broccolies.annotation.FluentNestedSender;
import com.bavelsoft.broccolies.annotation.FluentNestedSenders;Q
import com.bavelsoft.broccolies.annotation.FluentScenario;
import com.bavelsoft.broccolies.annotation.FluentScenarios;
import com.bavelsoft.broccolies.annotation.FluentSender;
import com.bavelsoft.broccolies.annotation.FluentSenders;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bavelsoft.broccolies.util.AnnotationMirrorUtil.annotatedElements;
import static com.bavelsoft.broccolies.util.AnnotationMirrorUtil.annotationMaps;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AutoService(Processor.class)
public class FluentProcessor extends AbstractProcessor {
	private Messager messager;
	private Types typeUtils;
	private FluentSenderGenerator senderGenerator;
	private FluentNestedSenderGenerator nestedSenderGenerator;
	private FluentExpecterGenerator expecterGenerator;
	private FluentActorGenerator actorGenerator;
	private FluentScenarioGenerator scenarioGenerator;
	private final Class[] senderAnnotations = new Class[] { FluentSender.class, FluentSenders.class };
	private final Class[] scenarioAnnotations = new Class[] { FluentScenario.class, FluentScenarios.class };
	private final Class[] expecterAnnotations = new Class[] { FluentExpecter.class, FluentExpecters.class };

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);
		messager = env.getMessager();
		typeUtils = env.getTypeUtils();
		Elements elementUtils = env.getElementUtils();
		Filer filer = env.getFiler();
		senderGenerator  = new FluentSenderGenerator(typeUtils, elementUtils, filer);
		scenarioGenerator  = new FluentScenarioGenerator(typeUtils, elementUtils, filer);
		nestedSenderGenerator  = new FluentNestedSenderGenerator(typeUtils, elementUtils, filer);
		expecterGenerator = new FluentExpecterGenerator(elementUtils, filer);
		actorGenerator = new FluentActorGenerator(elementUtils, filer);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotationsParam, RoundEnvironment env) {
		generateActors(env);
		Map<String, String> referenceKeys = getReferenceKeys(env);
		Collection<TypeMirror> nesting = generateNestedSenders(env, referenceKeys);
		generateSenders(env, referenceKeys, nesting);
		generateExpecters(env, referenceKeys);
		generateScenarios(env);
		return true;
	}

	private Collection<TypeMirror> generateNestedSenders(RoundEnvironment env, Map<String, String> referenceKeys) {
		Collection<TypeMirror> nesting = new HashSet<>();
		Class[] annotationClasses = new Class[] { FluentNestedSender.class, FluentNestedSenders.class };
		for (Element element : annotatedElements(env, annotationClasses)) {
			for (Map<String, Object> map : annotationMaps(element, annotationClasses)) {
				try {
					List<TypeElement> containers = typeElementList(map.get("containers"));
					for (int i=0; i<(containers.size()-1); i++) {
						nesting.add(containers.get(i).asType());
					}
					nesting.add(typeElement(map.get("value")).asType());
				} catch (Exception e) {
					System.err.println(element);
					e.printStackTrace();
				}
			}
		}
		for (Element element : annotatedElements(env, annotationClasses)) {
			for (Map<String, Object> map : annotationMaps(element, annotationClasses)) {
				try {
					nestedSenderGenerator.generate(
						getInitializer(element, map),
						typeElement(map.get("value")),
						null, //TODO this has to from the top one
						referenceKeys,
						nesting,
						typeElementList(map.get("containers")),
						isLegacyCompatible(element));
				} catch (Exception e) {
					System.err.println(element);
					e.printStackTrace();
				}
			}
		}
		return nesting;
	}

	private Map<String, String> getReferenceKeys(RoundEnvironment env) {
		Map<String, String> referenceKeys = new HashMap<>();
		for (Element element : env.getElementsAnnotatedWith(FluentKey.class)) {
			String className = ((TypeElement)element.getEnclosingElement()).getQualifiedName().toString();
			String fieldName = element.getSimpleName().toString();
			referenceKeys.put(className, fieldName);
		}
		return referenceKeys;
	}

	private void generateSenders(RoundEnvironment env,
			Map<String, String> referenceKeys,
			Collection<TypeMirror> nesting) {
		for (Element element : annotatedElements(env, senderAnnotations)) {
			for (Map<String, Object> map : annotationMaps(element, senderAnnotations)) {
				try {
					senderGenerator.generate(getInitializer(element, map),
						typeElement(map.get("value")),
						typeElement(map.get("reference")),
						referenceKeys,
						nesting);
				} catch (Exception e) {
					System.err.println(element);
					e.printStackTrace();
				}
			}
		}
	}

	private void generateScenarios(RoundEnvironment env) {
		for (Element element : annotatedElements(env, scenarioAnnotations)) {
			for (Map<String, Object> map : annotationMaps(element, scenarioAnnotations)) {
				try {
					scenarioGenerator.generate(getInitializer(element, map),
						typeElement(map.get("value")),
						null,
						null,
						null);
				} catch (Exception e) {
					System.err.println(element);
					e.printStackTrace();
				}
			}
		}
	}

	private boolean isLegacyCompatible(Element element) {
		try {
			FluentActor a = element.getEnclosingElement().getAnnotation(FluentActor.class);
			return a.legacyCompatible();
		} catch (Exception e) {
		}
		return false;
	}

	private String getInitializer(Element element, Map<String, Object> s) {
		String initializer = (String)s.get("initializer");
		if (initializer == null) {
			FluentActor a = element.getEnclosingElement().getAnnotation(FluentActor.class);
			initializer = a.initializer();
			if (initializer == null || initializer.equals("")) {
				initializer = "new $T()";
			}
		}
		return initializer;
	}

	private void generateExpecters(RoundEnvironment env, Map<String, String> referenceKeys) {
		for (Element element : annotatedElements(env, expecterAnnotations)) {
			for (Map<String, Object> map : annotationMaps(element, expecterAnnotations)) {
				try {
					expecterGenerator.generate(
						typeElement(map.get("value")),
						typeElement(map.get("reference")),
						referenceKeys,
						(String)map.get("onlyLastOf"),
						isLegacyCompatible(element)
						);
				} catch (Exception e) {
					System.err.println(element);
					e.printStackTrace();
				}
			}
		}
	}

	private void generateActors(RoundEnvironment env) {
		for (Element element : env.getElementsAnnotatedWith(FluentActor.class)) {
			FluentActor s = element.getAnnotation(FluentActor.class);
			Collection<FluentActorGenerator.FluentElement> enclosedElements = getEnclosedElements(element);
			try {
				actorGenerator.generate(s.value(), element, enclosedElements);
			} catch (Exception e) {
				System.err.println(element);
				e.printStackTrace();
			}
		}
	}

	private Collection<FluentActorGenerator.FluentElement> getEnclosedElements(Element element) {
		Collection<FluentActorGenerator.FluentElement> enclosedElements = new ArrayList<FluentActorGenerator.FluentElement>();
		for (Element enclosed : element.getEnclosedElements()) {
			try {
				for (Map<String, Object> map : annotationMaps(enclosed, expecterAnnotations)) {
					enclosedElements.add(new FluentActorGenerator.FluentElement(
						typeElement(map.get("value")),
						false,
						typeElement(map.get("reference")),
						(String)map.get("expectMethod")));
				}
				for (Map<String, Object> map : annotationMaps(enclosed, senderAnnotations)) {
					enclosedElements.add(new FluentActorGenerator.FluentElement(
						typeElement(map.get("value")),
						true,
						typeElement(map.get("reference")),
						(String)map.get("sendMethod")));
				}
				for (Map<String, Object> map : annotationMaps(enclosed, scenarioAnnotations)) {
					enclosedElements.add(new FluentActorGenerator.FluentElement(
						typeElement(map.get("value")),
						true,
						null,
						(String)map.get("sendMethod")));
				}
			} catch (Exception e) {
				System.err.println(element + "/" + enclosed);
				e.printStackTrace();
			}
		}
		return enclosedElements;
	}

	private List<TypeElement> typeElementList(Object avl) {
		return ((List<AnnotationValue>)avl).stream().map(av->typeElement(av.getValue())).collect(toList());
	}

	private TypeElement typeElement(Object v) {
		return v == null ? null : (TypeElement)typeUtils.asElement((TypeMirror)v);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return annotations(FluentExpecters.class, FluentExpecter.class, FluentSenders.class, FluentSender.class, FluentActor.class, FluentKey.class, FluentScenarios.class, FluentScenario.class);
	}

	private Set<String> annotations(Class<?>... classes) {
		return Arrays.stream(classes).map(c->c.getCanonicalName()).collect(toSet());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}
