package taxi.lib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Injector {
    private static final Map<String, Injector> injectors = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(Injector.class);
    private final Map<Class<?>, Object> instanceOfClasses = new HashMap<>();
    private final List<Class<?>> classes = new ArrayList<>();

    private Injector(String mainPackageName) {
        logger.debug("start Injector method");
        try {
            classes.addAll(getClasses(mainPackageName));
            logger.debug("Injector method complete");
        } catch (IOException | ClassNotFoundException e) {
            logger.error("method Injector error ", e);
            throw new RuntimeException("Can't get information about all classes", e);
        }
    }

    public static Injector getInstance(String mainPackageName) {
        logger.debug("start getInstance return Injector method");
        if (injectors.containsKey(mainPackageName)) {
            return injectors.get(mainPackageName);
        }
        Injector injector = new Injector(mainPackageName);
        injectors.put(mainPackageName, injector);
        logger.debug("getInstance method return Injector complete");
        return injector;
    }

    public Object getInstance(Class<?> certainInterface) {
        logger.debug("start getInstance return Object method");
        Object newInstanceOfClass = null;
        Class<?> clazz = findClassExtendingInterface(certainInterface);
        Object instanceOfCurrentClass = createInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (isFieldInitialized(field, instanceOfCurrentClass)) {
                continue;
            }
            if (field.getDeclaredAnnotation(Inject.class) != null) {
                Object classToInject = getInstance(field.getType());
                newInstanceOfClass = getNewInstance(clazz);
                setValueToField(field, newInstanceOfClass, classToInject);
            } else {
                logger.error("Error! Class " + field.getName() + " in class "
                        + clazz.getName() + " hasn't annotation Inject");
                throw new RuntimeException("Class " + field.getName() + " in class "
                        + clazz.getName() + " hasn't annotation Inject");
            }
        }
        if (newInstanceOfClass == null) {
            return getNewInstance(clazz);
        }
        logger.debug("getInstance return Object method complete");
        return newInstanceOfClass;
    }

    private Class<?> findClassExtendingInterface(Class<?> certainInterface) {
        logger.debug("start findClassExtendingInterface method");
        for (Class<?> clazz : classes) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> singleInterface : interfaces) {
                if (singleInterface.equals(certainInterface)
                        && (clazz.isAnnotationPresent(Service.class)
                        || clazz.isAnnotationPresent(Dao.class))) {
                    logger.debug("findClassExtendingInterface method complete");
                    return clazz;
                }
            }
        }
        logger.error("Can't find class which implements "
                + certainInterface.getName()
                + " interface and has valid annotation (Dao or Service)");
        throw new RuntimeException("Can't find class which implements "
                + certainInterface.getName()
                + " interface and has valid annotation (Dao or Service)");
    }

    private Object getNewInstance(Class<?> certainClass) {
        logger.debug("start getNewInstance method");
        if (instanceOfClasses.containsKey(certainClass)) {
            return instanceOfClasses.get(certainClass);
        }
        Object newInstance = createInstance(certainClass);
        instanceOfClasses.put(certainClass, newInstance);
        logger.debug("getNewInstance method complete");
        return newInstance;
    }

    private boolean isFieldInitialized(Field field, Object instance) {
        logger.debug("start isFieldInitialized method");
        field.setAccessible(true);
        try {
            logger.debug("isFieldInitialized method complete");
            return field.get(instance) != null;
        } catch (IllegalAccessException e) {
            logger.error("Can't get access to field");
            throw new RuntimeException("Can't get access to field");
        }
    }

    private Object createInstance(Class<?> clazz) {
        logger.debug("start createInstance method");
        Object newInstance;
        try {
            Constructor<?> classConstructor = clazz.getConstructor();
            newInstance = classConstructor.newInstance();
        } catch (Exception e) {
            logger.error("Can't create object of the class", e);
            throw new RuntimeException("Can't create object of the class", e);
        }
        logger.debug("createInstance method complete");
        return newInstance;
    }

    private void setValueToField(Field field, Object instanceOfClass, Object classToInject) {
        logger.debug("start setValueToField method");
        try {
            field.setAccessible(true);
            field.set(instanceOfClass, classToInject);
            logger.debug("setValueToField method complete");
        } catch (IllegalAccessException e) {
            logger.error("Can't set value to field ", e);
            throw new RuntimeException("Can't set value to field ", e);
        }
    }
    /**
     * Scans all classes accessible from the context class loader which
     * belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException if the class cannot be located
     * @throws IOException            if I/O errors occur
     */

    private static List<Class<?>> getClasses(String packageName)
            throws IOException, ClassNotFoundException {
        logger.debug("start getClasses method");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            logger.error("Class loader is null");
            throw new RuntimeException("Class loader is null");
        }
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        logger.debug("getClasses method complete");
        return classes;
    }
    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException if the class cannot be located
     */

    private static List<Class<?>> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        logger.debug("start findClasses method");
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().contains(".")) {
                        logger.error("File name shouldn't consist point.");
                        throw new RuntimeException("File name shouldn't consist point.");
                    }
                    classes.addAll(findClasses(file, packageName + "."
                            + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.'
                            + file.getName().substring(0, file.getName().length() - 6)));
                }
            }
        }
        logger.debug("findClasses method complete");
        return classes;
    }
}
