package broccolies;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
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
import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.io.IOException;
import broccolies.util.GeneratorUtil;
import broccolies.util.ExpecterUtil;
import broccolies.util.LastRunnable;
import static broccolies.util.FluentSenderGeneratorBase.isReference;
import static broccolies.util.FluentSenderGeneratorBase.capitalize;
import static broccolies.util.WriterUtil.write;

public class FluentExpecterGenerator {
	private final String fromSystemUnderTest = "fromSystemUnderTest";
	private final String conditions = "conditions";
	private final String references = "references";
	private final Elements elementUtils;
	private final Filer filer;
	private Map<String, String> getters;

	public FluentExpecterGenerator(Elements elementUtils, Filer filer) {
		this.elementUtils = elementUtils;
		this.filer = filer;
	}

	public void generate(TypeElement te, TypeElement reference, Map<String, String> referenceKeys, String onlyLastOf) throws IOException {
		String packageName = elementUtils.getPackageOf(te).getQualifiedName().toString();
		ClassName className = ClassName.get(packageName, GeneratorUtil.getName(te)+"Expecter");
		TypeSpec.Builder typeBuilder = getType(te, className, onlyLastOf);
		getters = new HashMap<>();
		for (Element element : elementUtils.getAllMembers(te)) {
			MethodSpec method = getMethod(element, className);
			if (method != null) {
				typeBuilder.addMethod(method);
			}
		}
		typeBuilder.addMethod(getEnrichReferenceMethod(te, reference, referenceKeys));
		if (isReference(reference)) {
			typeBuilder.addMethod(getEnrichParticularReferenceMethod(te, reference));
			typeBuilder.addMethod(getGetReferenceMethod(te, reference));
		}
		makeRunnable(typeBuilder);
		write(filer, className, typeBuilder);
	}

	private TypeSpec.Builder getType(TypeElement te, ClassName className, String onlyLastOf) {
		MethodSpec.Builder getMessagesMethod = MethodSpec.methodBuilder("getMessages")
				.returns(ParameterizedTypeName.get(
					ClassName.get(java.util.Collection.class),
					TypeName.get(te.asType())
				));
		getMessagesMethod.addStatement("$T stream = $L.stream().filter(v->v instanceof $T)",
			ParameterizedTypeName.get(
				ClassName.get(Stream.class),
				TypeName.get(te.asType())),
			fromSystemUnderTest,
			TypeName.get(te.asType()));
		if (onlyLastOf != null && !onlyLastOf.equals(""))
			getMessagesMethod.addStatement("return (($T)stream.collect($T.toMap(v->v.$L, v->v))).values()",
				Map.class, Collectors.class, onlyLastOf);
		else
			getMessagesMethod.addStatement("return stream.collect($T.toList())", Collectors.class);

		return TypeSpec.classBuilder(className.simpleName())
    			.addModifiers(Modifier.PUBLIC)
    			.addField(FieldSpec.builder(
				ParameterizedTypeName.get(
					ClassName.get(java.util.Collection.class),
					ParameterizedTypeName.get(
						ClassName.get(java.util.function.Predicate.class),
						TypeName.get(te.asType())
					)
				), conditions)
				.initializer("new $T<>()", java.util.ArrayList.class)
				.build())
    			.addField(FieldSpec.builder(ClassName.get(java.util.Collection.class), fromSystemUnderTest)
				.build())
    			.addMethod(MethodSpec.constructorBuilder()
    				.addModifiers(Modifier.PUBLIC)
				.addParameter(java.util.Collection.class, fromSystemUnderTest)
				.addStatement("$T.set(this)", LastRunnable.class)
				.addStatement("this.$L = $L", fromSystemUnderTest, fromSystemUnderTest)
				.build())
    			.addMethod(getMessagesMethod.build())
    			.addMethod(MethodSpec.methodBuilder("expect")
        			.addModifiers(Modifier.PUBLIC)
				.addStatement("$T.unset()", LastRunnable.class)
        			.addStatement("$T.match($L, getMessages())", ExpecterUtil.class, conditions)
        			.build());
	}

	private MethodSpec getEnrichReferenceMethod(TypeElement te, TypeElement reference, Map<String, String> referenceKeys) {
		String message = "message";
		MethodSpec.Builder builder = MethodSpec.methodBuilder("enrichReference")
			.addModifiers(Modifier.PUBLIC)
			.addModifiers(Modifier.STATIC)
			.addParameter(ClassName.get(Map.class), references)
			.addParameter(TypeName.get(te.asType()), message);
		if (isReference(reference)) {
			String refType = reference.getQualifiedName().toString();
			String getter = "get"+capitalize(referenceKeys.get(refType));
			builder.addStatement("$L ref = ($L)$L.get($L.$L())", refType, refType, references, message, getter);
			builder.addStatement("if (ref != null)\nenrichParticularReference(ref, message)");
		}
		return builder.build();
	}

	private MethodSpec getGetReferenceMethod(TypeElement te, TypeElement reference) {
		String message = "message";
		String ref = "ref";
		TypeName refType = TypeName.get(reference.asType());
		MethodSpec.Builder builder = MethodSpec.methodBuilder("getReference")
			.addModifiers(Modifier.PUBLIC)
			.addStatement("$T.unset()", LastRunnable.class)
        		.addStatement("$T $L = $T.matchOne($L, getMessages())",
				TypeName.get(te.asType()),
				message,
				ExpecterUtil.class,
				conditions)
        		.addStatement("$T $L = new $T()", refType, ref, refType)
        		.addStatement("enrichParticularReference($L, $L)", ref, message)
			.addStatement("return $L", ref)
			.returns(TypeName.get(reference.asType()));
		return builder.build();
	}

	private MethodSpec getEnrichParticularReferenceMethod(TypeElement te, TypeElement reference) {
		String message = "message";
		String ref = "ref";
		MethodSpec.Builder builder = MethodSpec.methodBuilder("enrichParticularReference")
			.addModifiers(Modifier.PRIVATE)
			.addModifiers(Modifier.STATIC)
			.addParameter(TypeName.get(reference.asType()), ref)
			.addParameter(TypeName.get(te.asType()), message);
		for (Element element : elementUtils.getAllMembers(reference)) {
			String name = element.getSimpleName().toString();
			if (element.getKind() == ElementKind.FIELD && getters.containsKey(name)) {
				builder.addStatement("$L.$L = $L.$L()", ref, name, message, getters.get(name));
			}
		}
		return builder.build();
	}

	private void makeRunnable(TypeSpec.Builder typeBuilder) {
    		typeBuilder
			.addSuperinterface(ClassName.get(Runnable.class))
                        .addMethod(MethodSpec.methodBuilder("run")
        			.addModifiers(Modifier.PUBLIC)
        			.addStatement("expect()")
        			.build());
	}

	private MethodSpec getMethod(Element element, ClassName className) {
		if (element.getKind() != ElementKind.METHOD
			|| element.getModifiers().contains(Modifier.NATIVE)
			|| element.getModifiers().contains(Modifier.FINAL)) //TODO shouldn't use overriding
			return null;
		MethodSpec u = MethodSpec.overriding((ExecutableElement)element).build();
		if (u.parameters.size() != 0 || u.returnType == TypeName.VOID)
			return null;
		String name = element.getSimpleName().toString();
		int lengthOfIs = "is".length();
		int lengthOfGet = "get".length();
		if (name.startsWith("isIs")) {
			name = Character.toLowerCase(lengthOfIs) + name.substring(lengthOfIs+1);
		}
		if (name.startsWith("get") && Character.isUpperCase(name.charAt(lengthOfGet))) {
			name = Character.toLowerCase(name.charAt(lengthOfGet)) + name.substring(lengthOfGet+1);
		}
		getters.put(name, u.name); //TODO refactor
		return MethodSpec.methodBuilder(name)
    			.addModifiers(u.modifiers)
    			.returns(className)
    			.addParameter(u.returnType, "y")
    			.addStatement("$L.add(x -> $T.equals(x.$L(), y))",
				conditions,
				java.util.Objects.class,
				element.getSimpleName().toString())
    			.addStatement("return this")
    			.build();
	}
}
