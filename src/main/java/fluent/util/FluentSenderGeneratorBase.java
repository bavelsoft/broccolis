package broccolies.util;

import java.util.function.Consumer;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.io.IOException;
import broccolies.util.GeneratorUtil;
import broccolies.util.LastRunnable;
import broccolies.annotation.FluentSender;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import static broccolies.util.WriterUtil.write;

public abstract class FluentSenderGeneratorBase {
	protected static final String underlying = "underlying";
	protected static final String consumer = "consumer";
	protected static final String onSend = "onSend";
	protected static final String references = "references";
	protected static final String ref = "ref";
	protected final Elements elementUtils;
	protected final Types typeUtils;
	protected final Filer filer;
	protected Map<String, String> setters;

	public FluentSenderGeneratorBase(Types typeUtils, Elements elementUtils, Filer filer) {
		this.typeUtils = typeUtils;
		this.elementUtils = elementUtils;
		this.filer = filer;
	}

	public void generate(String initializer, TypeElement te, TypeElement reference, Map<String, String> referenceKeys, Collection<TypeMirror> nesting) throws IOException {
		TypeSpec.Builder typeBuilder = getFullType(initializer, te, reference, referenceKeys, nesting);
		addSendMethod(typeBuilder);
		makeRunnable(typeBuilder);
		write(filer, getClassName(te), typeBuilder);
	}

	protected TypeSpec.Builder getFullType(String initializer, TypeElement te, TypeElement reference, Map<String, String> referenceKeys, Collection<TypeMirror> nesting) {
		ClassName className = getClassName(te);
		TypeSpec.Builder typeBuilder = getType(initializer, te, reference, className);
		setters = new HashMap<>();
		for (Element element : elementUtils.getAllMembers(te)) {
			MethodSpec method = getMethod(element, className, nesting);
			if (method != null)
				typeBuilder.addMethod(method);
		}
		if (isReference(reference))
			typeBuilder.addMethod(getReferenceMethod(te, className, reference, referenceKeys));
		return typeBuilder;
	}

	protected static ClassName getClassName(Element element) {
		Element parent = element;
		while (!(parent instanceof PackageElement)) {
			parent = parent.getEnclosingElement();
		}
		String packageName = ((PackageElement)parent).getQualifiedName().toString();
		ClassName className = ClassName.get(packageName, GeneratorUtil.getName(element)+"Sender");
		return className;
	}

	public static boolean isReference(TypeElement reference) {
		return reference != null && !reference.getQualifiedName().toString().equals("java.lang.Object");
	}

//TODO would be nice to somehow factor the reference support to a separate class
	protected MethodSpec getReferenceMethod(TypeElement te, ClassName className, TypeElement reference, Map<String, String> referenceKeys) {
		String refType = reference.getQualifiedName().toString();
		String field = referenceKeys.get(refType);
//TODO check if it's null
		MethodSpec.Builder method = MethodSpec.methodBuilder("reference")
    			.returns(className)
			.addParameter(TypeName.get(reference.asType()), ref)
        		.addModifiers(Modifier.PUBLIC)
			.addStatement("$L.put($L.$L, $L)", references, ref, field, ref);
		for (Element element : elementUtils.getAllMembers(reference)) {
			String name = element.getSimpleName().toString();
			if (element.getKind() == ElementKind.FIELD && setters.containsKey(name)) {
				method.addStatement("$L($L.$L)", name, ref, name);
			}
		}
		method.addStatement("return this");
        	return method.build();
	}

	public static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0))+s.substring(1);
	}

	protected TypeSpec.Builder getType(String initializer, TypeElement te, TypeElement reference, ClassName className) {
		TypeName consumerType = ParameterizedTypeName.get(
			ClassName.get(Consumer.class), TypeName.get(te.asType()));
		MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
    				.addModifiers(Modifier.PUBLIC)
				.addParameter(consumerType, consumer)
				.addParameter(ClassName.get(Runnable.class), onSend)
				.addStatement("this.$L = $L", consumer, consumer)
				.addStatement("this.$L = $L", onSend, onSend)
				.addParameter(ClassName.get(Map.class), references)
				.addStatement("this.$L = $L", references, references);
		addRunLast(constructor);

		return TypeSpec.classBuilder(className.simpleName())
    			.addModifiers(Modifier.PUBLIC)
    			.addField(FieldSpec.builder(
				TypeName.get(te.asType()), underlying)
				.initializer(initializer, te)
				.build())
    			.addField(FieldSpec.builder(
				consumerType, consumer)
				.build())
    			.addField(FieldSpec.builder(
				ClassName.get(Runnable.class), onSend)
				.build())
			.addField(FieldSpec.builder(ClassName.get(Map.class), references).build())
    			.addMethod(constructor.build());
	}

	protected abstract void addRunLast(MethodSpec.Builder constructor);

	protected abstract void addSendMethod(TypeSpec.Builder typeBuilder);

	protected abstract void makeRunnable(TypeSpec.Builder typeBuilder);

	protected MethodSpec getMethod(Element element, ClassName className, Collection<TypeMirror> nesting) {

		if (element.getModifiers().contains(Modifier.NATIVE)
			|| element.getModifiers().contains(Modifier.FINAL)) //TODO shouldn't use overriding
			return null;
		if (element.getKind() == ElementKind.METHOD) {
			ExecutableElement ex = (ExecutableElement)element;
			if (ex.getReturnType().getKind() == TypeKind.BOOLEAN) {
				return null;
			} else {
				return getMethod(ex, className, nesting);
			}
		} else if (element.getKind() == ElementKind.FIELD && element.getModifiers().contains(Modifier.PUBLIC))
			return getMethod((VariableElement)element, className);
		else
			return null;
	}

	protected MethodSpec getMethod(VariableElement element, ClassName className) {
		String name = element.getSimpleName().toString();
		return MethodSpec.methodBuilder(name)
    			.addModifiers(Modifier.PUBLIC)
    			.returns(className)
			.addParameter(getTypeName(element), "x")
    			.addStatement("$L.$L = $L", underlying, name, "x")
    			.addStatement("return this")
    			.build();
	}

	protected TypeName getTypeName(Element element) {
		try {
			return TypeName.get(element.asType());
		} catch (IllegalArgumentException e) {
			return ClassName.bestGuess(element.asType().toString());
		}
	}

	protected MethodSpec getMethod(ExecutableElement element, ClassName className, Collection<TypeMirror> nesting) {
		MethodSpec u = MethodSpec.overriding(element).build();
		if (u.parameters.size() != 1)
			return null;
		String name = getMethodName(element);
		setters.put(name, u.name); //TODO refactor
		MethodSpec.Builder builder = getMethodSignature(typeUtils, element, className, nesting);
		if (isNested(nesting, element)) {
			ClassName c = getClassName(typeUtils.asElement(element.getParameters().get(0).asType()));
			builder.addStatement("return new $L(this, x->$L.$L(x))",
				c, underlying, u.name);
		} else {
    			builder.addStatement("$L.$L($L)", underlying, u.name, u.parameters.get(0).name)
    				.addStatement("return this");
		}
		return builder.build();
	}

	protected static MethodSpec.Builder getMethodSignature(Types typeUtils, ExecutableElement element, ClassName className, Collection<TypeMirror> nesting) {
		MethodSpec u = MethodSpec.overriding(element).build();
		String name = getMethodName(element);
		MethodSpec.Builder builder = MethodSpec.methodBuilder(name)
    			.addModifiers(u.modifiers);

		if (isNested(nesting, element)) {
			ClassName c = getClassName(typeUtils.asElement(element.getParameters().get(0).asType()));
			builder.returns(c);
		} else {
			builder.returns(className)
    				.addParameters(u.parameters);
		}

    		return builder;
	}

	protected static boolean isNested(Collection<TypeMirror> nesting, ExecutableElement element) {
		return nesting != null
			&& nesting.contains(element.getParameters().get(0).asType());
	}

	protected static String getMethodName(ExecutableElement element) {
		String name = element.getSimpleName().toString();
		int lengthOfSet = "set".length();
		if (name.startsWith("set") && Character.isUpperCase(name.charAt(lengthOfSet))) {
			name = Character.toLowerCase(name.charAt(lengthOfSet)) + name.substring(lengthOfSet+1);
		}
		return name;
	}
}
