package com.bavelsoft.broccolies.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.bavelsoft.broccolies.util.FluentSenderGeneratorBase;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.bavelsoft.broccolies.util.WriterUtil.write;

public class FluentNestedSenderGenerator extends FluentSenderGeneratorBase {
	public FluentNestedSenderGenerator(Types typeUtils, Elements elementUtils, Filer filer) {
		super(typeUtils, elementUtils, filer);
	}

	public void generate(String initializer, TypeElement te, TypeElement reference, Map<String, String> referenceKeys, Collection<TypeMirror> nesting, List<TypeElement> containers, boolean isLegacyCompatible) throws IOException {
		TypeSpec.Builder typeBuilder = getFullType(initializer, te, reference, referenceKeys, nesting);
		addContainerStuff(typeBuilder, te, nesting, containers);
		addSendMethod(typeBuilder, reference);

		if (isLegacyCompatible)
			typeBuilder.addMethod(MethodSpec.methodBuilder("back")
                        	.addStatement("return this").returns(getClassName(te)).build());

		write(filer, getClassName(te), typeBuilder);
	}

	@Override
	protected void makeRunnable(TypeSpec.Builder typeBuilder) {
	}

	@Override
	protected void addRunLast(MethodSpec.Builder constructor) {
	}

	private void addContainerStuff(TypeSpec.Builder typeBuilder, TypeElement te, Collection<TypeMirror> nesting, List<TypeElement> containers) {
		typeBuilder
			.addField(FieldSpec.builder(
				getClassName(containers.get(0)), "container").build())
			.addMethod(MethodSpec.constructorBuilder()
    				.addModifiers(Modifier.PUBLIC)
				.addParameter(getClassName(containers.get(0)), "container")
				.addParameter(ParameterizedTypeName.get(
					ClassName.get(Consumer.class),
					TypeName.get(te.asType())), "containerSetter")
				.addStatement("this.container = container")
				.addStatement("containerSetter.accept(underlying)")
				.build());
		for (TypeElement container : containers) {
			for (Element element : elementUtils.getAllMembers(container)) {
				MethodSpec method = getMethodForContainer(element, getClassName(container), nesting);
				if (method != null) {
					typeBuilder.addMethod(method);
				}
			}
		}
	}

	@Override
	protected void addSendMethod(TypeSpec.Builder typeBuilder, TypeElement reference) {
    		typeBuilder.addMethod(MethodSpec.methodBuilder("send")
        		.addModifiers(Modifier.PUBLIC)
        		.addStatement("container.send()")
        		.build());
	}

	private MethodSpec getMethodForContainer(Element e, ClassName className, Collection<TypeMirror> nesting) {
		if (e.getKind() != ElementKind.METHOD
			|| e.getModifiers().contains(Modifier.NATIVE)
			|| e.getModifiers().contains(Modifier.FINAL)) //TODO shouldn't use overriding
			return null;
		ExecutableElement element = (ExecutableElement)e;
		if (element.getReturnType().getKind() == TypeKind.BOOLEAN)
			return null;
		MethodSpec u = MethodSpec.overriding(element).build();
		if (u.parameters.size() != 1)
			return null;
		MethodSpec.Builder builder = FluentSenderGenerator.getMethodSignature(typeUtils, element, className, nesting);
		String name = getMethodName(element);
		return builder
    			.addStatement("return container.$L($L)",
				name,
				FluentSenderGenerator.isNested(nesting, element) ? "" : u.parameters.get(0).name)
			.build();
	}

}
