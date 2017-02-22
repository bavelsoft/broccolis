package com.bavelsoft.broccolies.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.bavelsoft.broccolies.util.GeneratorUtil;
import com.bavelsoft.broccolies.util.RegressionUtil;

import com.thoughtworks.xstream.XStream;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.bavelsoft.broccolies.util.FluentSenderGeneratorBase.isReference;
import static com.bavelsoft.broccolies.util.WriterUtil.write;

public class FluentActorGenerator {
	private final String onSend = "_onSend";
	private final String fromSystemUnderTest = "_inbox";
	private final String references = "references";
	private final Elements elementUtils;
	private final Filer filer;
	
	public FluentActorGenerator(Elements elementUtils, Filer filer) {
		this.elementUtils = elementUtils;
		this.filer = filer;
	}

	public void generate(String name, Element e, Collection<FluentElement> enclosedElements) throws IOException {
		ClassName className = ClassName.get(getPackageName(e), name);
		FluentElement firstElement = enclosedElements.iterator().next();
		TypeSpec.Builder typeBuilder = getType(className, firstElement.reference);
		for (FluentElement fe : enclosedElements) {
			typeBuilder.addMethod(getMethod(fe, className));
			if (fe.isSender)
				typeBuilder.addField(getConsumerField(fe.te));
			else
				typeBuilder.addMethod(getAddMethod(fe));
		}
		write(filer, className, typeBuilder);
	}

	private MethodSpec getAddMethod(FluentElement fe) {
		String packageName = getPackageName(fe.te);
		if (packageName.length() != 0) {
			packageName = packageName + ".";
		}
		String expecterName = packageName+GeneratorUtil.getName(fe.te)+"Expecter";
		String message = "message";
		MethodSpec.Builder builder = MethodSpec.methodBuilder("fromSystemUnderTest")
    			.addModifiers(Modifier.PUBLIC)
			.addParameter(TypeName.get(fe.te.asType()), message)
			.addStatement("$L.add($L)", fromSystemUnderTest, message)
			.addStatement("$T.ru.addMessage($L)", RegressionUtil.class, message);
		if (isReference(fe.reference)) {
			builder.addStatement("$L.enrichReference($L, $L)", expecterName, references, message);
		}
		return builder.build();
	}

	private TypeSpec.Builder getType(ClassName className, TypeElement reference) {
		TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
    			.addModifiers(Modifier.PUBLIC)
    			.addField(FieldSpec.builder(ClassName.get(java.util.Collection.class), fromSystemUnderTest)
    				.addModifiers(Modifier.PRIVATE)
				.initializer("new $T<>()", java.util.ArrayList.class)
				.build())
    			.addField(FieldSpec.builder(
				ClassName.get(Runnable.class), onSend)
    				.addModifiers(Modifier.PRIVATE)
				.initializer("()->$L.clear()", fromSystemUnderTest)
				.build())
			.addField(FieldSpec.builder(
				ClassName.get(XStream.class), "xstream")
				.initializer("new XStream()")
				.build());
		if (isReference(reference)) {
			builder.addField(FieldSpec.builder(ClassName.get(Map.class), references)
    				.addModifiers(Modifier.PRIVATE)
				.initializer("new $T()", HashMap.class).build());
		}
		return builder;
	}

	private MethodSpec getMethod(FluentElement fe, ClassName className) {
		TypeElement te = fe.te;
		ClassName c;
		MethodSpec.Builder m;
		String referencesOrNull = isReference(fe.reference) ? references : "null";
		String methodName = fe.methodName;
		if (fe.isSender) {
			c = ClassName.get(getPackageName(te), GeneratorUtil.getName(te)+"Sender");
			if (methodName == null || methodName.equals("")) {
				methodName = "send"+te.getSimpleName().toString();
			}
			m = MethodSpec.methodBuilder(methodName)
       				.addStatement("return new $T($L, $L, $L)", c, getConsumer(te), onSend, referencesOrNull);
		} else {
			c = ClassName.get(getPackageName(te), GeneratorUtil.getName(te)+"Expecter");
			if (methodName == null || !methodName.equals("")) {
				methodName = "expect"+te.getSimpleName().toString();
			}
			m = MethodSpec.methodBuilder(methodName)
       				.addStatement("return new $T($L)", c, fromSystemUnderTest);
		}

       		return m.addModifiers(Modifier.PUBLIC)
			.returns(c)
       			.build();
	}

	private String getPackageName(Element te) {
		return elementUtils.getPackageOf(te).getQualifiedName().toString();
	}

	private String getConsumer(TypeElement te) {
		return "_"+GeneratorUtil.getName(te)+"ToSystemUnderTest";
	}

	private FieldSpec getConsumerField(TypeElement te) {
		TypeName consumerType = ParameterizedTypeName.get(
			ClassName.get(Consumer.class),
			ClassName.get(te.asType()));
    		return FieldSpec.builder(consumerType, getConsumer(te))
    			.addModifiers(Modifier.PUBLIC)
			.build();
	}

	public static class FluentElement {
		public final TypeElement te, reference;
		public final boolean isSender;
		public final String methodName;

		public FluentElement(TypeElement te, boolean isSender, TypeElement reference, String methodName) {
			this.te = te;
			this.isSender = isSender;
			this.reference = reference;
			this.methodName = methodName;
		}
	}
}
