import java.applet.Applet;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.InstructionPrinter;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;

public class App {

    public static void main(String[] args) {

        List<Class> classes = Arrays
            .asList(Component.class, Container.class, Window.class, Panel.class,
                JComponent.class, Frame.class, Dialog.class, JWindow.class,
                Applet.class, JFrame.class, JDialog.class, JApplet.class);

//        for (Class aClass : classes) {
//            List<CtMethod> publicMethods = getPublicMethods(aClass.getName());
//            Map<CtMethod, Set<String>> methodsWithFields = publicMethods.stream()
//                .collect(Collectors.toMap(method -> method, App::getUsedFields));
//            int count = 0;
//            for (int i = 0; i < publicMethods.size(); i++) {
//                for (int j = i + 1; j < publicMethods.size(); j++) {
//                    Set<String> fields1 = new HashSet<>(methodsWithFields.get(publicMethods.get(i)));
//                    Set<String> fields2 = new HashSet<>(methodsWithFields.get(publicMethods.get(j)));
//                    fields1.retainAll(fields2);
//                    if (fields1.size() > 0) {
//                        count++;
//                    }
//                }
//            }
//            System.out.println(aClass.getName() + "\t" + publicMethods.size() + "\t" + count);
//        }

//        List<CtMethod> publicMethods = getPublicMethods("java.awt.Panel");
//        System.out.println(publicMethods.size());

//        Map<CtMethod, Set<String>> methodsWithFields = publicMethods.stream()
//            .collect(Collectors.toMap(method -> method, App::getUsedFields));
//
//        methodsWithFields.forEach((k, v) -> System.out.println(k.getName() + "\t: " + v));

        List<CtMethod> publicMethods = getPublicMethods("java.awt.Dialog");
        publicMethods.forEach(App::getInvokedMethods);
    }

    private static Set<String> getUsedFields(CtMethod method) {
        String content = getMethodContent(method);

        return Arrays.stream(content.split("\\r?\\n"))
            .filter(string -> string.contains("field"))
            .map(string -> string.replaceAll(".*Field ", ""))
            .map(string -> string.replaceAll("\\(.+\\)", ""))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<CtMethod> getPublicMethods(String className) {
        ClassPool pool = ClassPool.getDefault();
        try {
            CtClass ctClass = pool.get(className);
            CtMethod[] ctMethods = ctClass.getDeclaredMethods(); // should be getMethods() since inherited methods are also needed
            return Arrays.stream(ctMethods)
                .filter(ctMethod -> Modifier.isPublic(ctMethod.getModifiers()))
                .collect(Collectors.toList());
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private static void getInvokedMethods(CtMethod method) {
        String content = getMethodContent(method);

        System.out.println(method.getName());
        System.out.println();

        Arrays.stream(content.split("\\r?\\n"))
            .filter(string -> string.contains("invoke"))
            .filter(string -> !string.contains("<init>"))
            .forEach(System.out::println);
//            .map(string -> string.replaceAll(".*Field ", ""))
//            .map(string -> string.replaceAll("\\(.+\\)", ""))
//            .collect(Collectors.toCollection(LinkedHashSet::new));
        System.out.println("-----------------------");

    }

    private static String getMethodContent(CtMethod method) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        InstructionPrinter i = new InstructionPrinter(ps);
        i.print(method);
        return baos.toString();
    }
}
