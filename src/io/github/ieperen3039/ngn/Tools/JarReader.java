package io.github.ieperen3039.ngn.Tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A utility class for dynamic loading of JAR files on runtime
 * @author Geert van Ieperen. Created on 19-9-2018.
 */
public final class JarReader<T> {

    private URLClassLoader classLoader;
    private Class<T> classType;
    private File[] files;

    public JarReader(Path path, Class<T> classType) throws Exception{
        this.classType = classType;
        files = path.toFile().listFiles();

        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }

        this.classLoader = new URLClassLoader(urls);
    }

    public ClassLoader getClassloader() {
        return classLoader;
    }

    /**
     * loads a specific jarfile, and loads all classes of type T
     * @param jarFile the jarfile to search
     * @param loader  a class loader that has access to the classes in the file
     * @return one instance of every classes found in the jar file that implement {@link T}
     * @throws IOException                  if the jar could not be properly loaded
     * @throws ReflectiveOperationException if the Classloader is nat able to load the classes in this file, or if the
     *                                      script does not have a constructor with one Game parameter
     * @throws IllegalArgumentException     if the provided file does not exist or is not a jar file
     */
    public List<T> loadFile(File jarFile) throws IOException, ReflectiveOperationException {
        if (jarFile == null || !jarFile.exists()) {
            throw new IOException("Invalid jar file provided: " + jarFile);
        }

        List<Class<?>> classes = loadClassesFromJar(jarFile, classLoader);

        List<T> list = new ArrayList<>(classes.size());
        for (Class<?> c : classes) {
            // check if the class implements the provided interface
            if (classType.isAssignableFrom(c)) {
                Constructor<?> constructor = c.getConstructor();
                @SuppressWarnings("unchecked")
                T inst = (T) constructor.newInstance();
                list.add(inst);
            }
        }

        int nrOfClasses = list.size();
        Logger.DEBUG.printf("%d %s found in %s", nrOfClasses, (nrOfClasses == 1) ? "class" : "classes", jarFile.getName());
        return list;
    }

    /**
     * loads all classes in all jars in the given directory, collecting all implementations of {@link T}.
     * @return a list of all loaded scripts
     */
    public List<T> loadPlugins() throws IOException {
        String tName = classType.getCanonicalName();
        List<T> instances = new ArrayList<>();
        for (File jar : files) {
            try {
                Logger.DEBUG.print("Searching " + jar.getName() + " for " + tName + "...");
                List<T> fileScripts = loadFile(jar);
                instances.addAll(fileScripts);

            } catch (ReflectiveOperationException ex) {
                Logger.WARN.print("Could not load plugins from " + jar);

            } catch (IOException ex) {
                Logger.WARN.print("Could not open " + jar);
            }
        }

        Logger.INFO.print("Loaded " + instances.size() + " instances of " + tName + "\n");
        return instances;
    }

    /**
     * Scans a JAR file for .class-files and load all classes found. Return a list of loaded classes
     * @param file   JAR-file which should be searched for .class-files
     * @return Returns all found class-files with their full-name as a List of Strings
     * @throws IOException              If during processing of the Jar-file an error occurred
     * @throws IllegalArgumentException If either the provided file is null, does not exist or is no Jar file
     */
    private static List<Class<?>> loadClassesFromJar(File file, ClassLoader loader) throws IOException, IllegalArgumentException, ClassNotFoundException {
        if (file == null || !file.exists())
            throw new IllegalArgumentException("Invalid file: " + file);

        if (!file.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Provided file was not a jar file: " + file);
        }

        // get a classloader and load all provided classes
        List<Class<?>> implementations = new ArrayList<>();
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                String fileName = entries.nextElement().getName();
                if (!fileName.endsWith(".class")) continue;

                // load all classes
                Class<?> aClass = loadClass(loader, fileName);
                implementations.add(aClass);
            }
        }

        return implementations;
    }

    /**
     * loads a class given by filename.
     * @param loader   a classloader, or null for the Bootstrap loader
     * @param fileName a file pointing to a .class file
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be located
     */
    public static Class<?> loadClass(ClassLoader loader, String fileName) throws ClassNotFoundException {
        String classFile = fileName.substring(0, fileName.lastIndexOf(".class"));

        if (classFile.contains("/"))
            classFile = classFile.replaceAll("/", ".");
        if (classFile.contains("\\"))
            classFile = classFile.replaceAll("\\\\", ".");

        Class<?> clazz;
        // now try to load the class
        if (loader == null)
            clazz = Class.forName(classFile);
        else
            clazz = Class.forName(classFile, true, loader);

        io.github.ieperen3039.ngn.Tools.Logger.DEBUG.print("Loaded class " + classFile);
        return clazz;
    }
}