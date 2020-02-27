package iockids;

import utils.Scanner;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 连锁注入:
 * 1. 通过getInstance()函数或连锁注入被注入的对象，其构造方法的参数也将被注入
 * 2. 构造类的参数符合注入条件的（@Inject)
 * 两种注入对象将被缓存:
 * 1. @Singleton，1个类->1个单例
 * 2. @Named("xx")，1个类（通常为接口)->N个名称->N个单例
 * 获取缓存对象(单例):
 * injector.getInstance(XX.class)
 * @time: 2020/2/15 0:38
 */
public class Injector {

	private Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();
	private Map<Class<?>, Map<Annotation, Object>> qualifiedInstances = new ConcurrentHashMap<>();
	{
		singletonInstances.put(Injector.class, this);
	}

	private Map<Class<?>, Class<?>> singletonClasses = new ConcurrentHashMap<>();
	private Map<Class<?>, Map<Annotation, Class<?>>> qualifiedClasses = new ConcurrentHashMap<>();

	private Set<Class<?>> readyClasses = ConcurrentHashMap.newKeySet();

	/**
	 * default construction
	 */
	public Injector() {
		scanInit();
	}

	/**
	 * construction with param of whether scan at initiation
	 * @param scan whether to scan all workspace of projection
	 */
	public Injector(boolean scan) {
		if (!scan) {
			return;
		}
		scanInit();
	}

	/**
	 * scan (usually called at initiation) all workspace of projection
	 */
	public void scanInit() {
		scanInit("");
	}

	/**
	 * scan specific path
	 * @param packageName the name of scanning root
	 */
	public void scanInit(String packageName) {
		List<Class<?>> set = Scanner.getClasses(packageName, true);
		for (Class<?> clazz : set) {
			Annotation[] annotations = clazz.getAnnotations();
			Annotation namedAnnotation = null;
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
					namedAnnotation = annotation;
				}
			}
			if (namedAnnotation == null) {
				continue;
			}
			Class<?>[] inters = clazz.getInterfaces();
			for (Class<?> inter : inters) {
				registerQualifiedClass(inter, namedAnnotation, clazz);
			}
		}
	}

	/**
	 *
	 * @param clazz Class for putting in qualifiedInstances Dict
	 * @param obj a instance for putting in qualifiedInstances Dict
	 * @return this
	 */
	public <T> Injector putSingleton(Class<T> clazz, T obj) {
		if (singletonInstances.put(clazz, obj) != null) {
			throw new InjectException("duplicated singleton object for the same class " + clazz.getCanonicalName());
		}
		return this;
	}

	/**
	 *
	 * @param clazz Class for putting in qualifiedInstances Dict
	 * @param annotation an annotation meeting JSR-330 qualified specification, which can be used to identify concrete
	 *        implement when injection
	 * @param obj a instance for putting in qualifiedInstances Dict
	 * @return this
	 */
	public <T> Injector putQualified(Class<T> clazz, Annotation annotation, T obj) {
		if (!annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
			throw new InjectException(
					"annotation must be decorated with Qualifier " + annotation.annotationType().getCanonicalName());
		}
		var os = qualifiedInstances.get(clazz);
		if (os == null) {
			os = new ConcurrentHashMap<>();
			qualifiedInstances.put(clazz, os);
		}
		if (os.put(annotation, obj) != null) {
			throw new InjectException(
					String.format("duplicated qualified object with the same qualifier %s with the class %s",
							annotation.annotationType().getCanonicalName(), clazz.getCanonicalName()));
		}
		return this;
	}

	/**
	 *
	 * @param clazz Class for registering in singletonClasses Dict
	 * @return this
	 */
	public <T> Injector registerSingletonClass(Class<T> clazz) {
		return this.registerSingletonClass(clazz, clazz);
	}

	/**
	 *
	 * @param parentType a Class or an interface
	 * @param clazz Class for registering in singletonClasses Dict
	 * @return this
	 */
	public <T> Injector registerSingletonClass(Class<?> parentType, Class<T> clazz) {
		if (singletonClasses.put(parentType, clazz) != null) {
			throw new InjectException("duplicated singleton class " + parentType.getCanonicalName());
		}
		return this;
	}

	/**
	 *
	 * @param parentType a Class or an interface
	 * @param clazz Class for registering in qualifiedClasses Dict
	 * @return this
	 */
	public <T> Injector registerQualifiedClass(Class<?> parentType, Class<T> clazz) {
		for (Annotation anno : clazz.getAnnotations()) {
			if (anno.annotationType().isAnnotationPresent(Qualifier.class)) {
				return this.registerQualifiedClass(parentType, anno, clazz);
			}
		}
		throw new InjectException("class should decorated with annotation tagged by Qualifier");
	}

	/**
	 *
	 * @param parentType a Class or an interface
	 * @param annotation an annotation meeting JSR-330 qualified specification, which can be used to identify concrete
	 * 	      implement when injection
	 * @param clazz Class for registering in qualifiedClasses Dict
	 * @return this
	 */
	public <T> Injector registerQualifiedClass(Class<?> parentType, Annotation annotation, Class<T> clazz) {
		if (!annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
			throw new InjectException(
					"annotation must be decorated with Qualifier " + annotation.annotationType().getCanonicalName());
		}
		var annos = qualifiedClasses.get(parentType);
		if (annos == null) {
			annos = new ConcurrentHashMap<>();
			qualifiedClasses.put(parentType, annos);
		}
		if (annos.put(annotation, clazz) != null) {
			throw new InjectException(String.format("duplicated qualifier %s with the same class %s",
					annotation.annotationType().getCanonicalName(), parentType.getCanonicalName()));
		}
		return this;
	}

	/**
	 * print qualifiedClasses dict for testing
	 */
	public void printQualifiedClasses() {
		for (Class<?> clazz : qualifiedClasses.keySet()) {
			Map<Annotation, Class<?>> value = qualifiedClasses.get(clazz);
			for (Annotation annotation : value.keySet()) {
				System.out.println(clazz + " : { " + annotation.toString() + " -> " + value.get(annotation).getSimpleName() + "}");
			}
		}
	}

	/**
	 *
	 * @param declaringClazz the Class calling this function
	 * @param clazz Class type for creating new instance from qualifiedInstances or qualifiedClasses Dict
	 * @param annotations annotation for identifying (usually by name) target corresponding clazz
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createFromQualified(Class<?> declaringClazz, Class<?> clazz, Annotation[] annotations) {
		var qs = qualifiedInstances.get(clazz);
		if (qs != null) {
			Set<Object> os = new HashSet<>();
			for (var annotation : annotations) {
				var obj = qs.get(annotation);
				if (obj != null) {
					os.add(obj);
				}
			}
			if (os.size() > 1) {
				throw new InjectException(String.format("duplicated qualified object for field %s@%s",
						clazz.getCanonicalName(), declaringClazz.getCanonicalName()));
			}
			if (!os.isEmpty()) {
				return (T) (os.iterator().next());
			}
		}
		// 此处需要预处理，先将相关类扫描进qualifiedClasses哈希表中
		var qz = qualifiedClasses.get(clazz);
		if (qz != null) {
			Set<Class<?>> oz = new HashSet<>();
			Annotation annoz = null;
			for (var annotation : annotations) {
				var z = qz.get(annotation);
				if (z != null) {
					oz.add(z);
					annoz = annotation;
				}
			}
			if (oz.size() > 1) {
				throw new InjectException(String.format("duplicated qualified classes for field %s@%s",
						clazz.getCanonicalName(), declaringClazz.getCanonicalName()));
			}
			if (!oz.isEmpty()) {
				final var annozRead = annoz;
				// 生成新的对象以后放到qualified队列里
				var t = (T) createNew(oz.iterator().next(), (o) -> {
					this.putQualified((Class<T>) clazz, annozRead, (T) o);
				});
				return t;
			}
		}
		return null;
	}

	/**
	 *
	 * @param clazz Class type for creating new instance
	 * @return new instance
	 */
	private <T> T createNew(Class<T> clazz) {
		return this.createNew(clazz, null);
	}

	/**
	 *
	 * @param clazz Class type for creating new instance
	 * @param consumer operation after creating new instance
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createNew(Class<T> clazz, Consumer<T> consumer) {
		var o = singletonInstances.get(clazz);
		if (o != null) {
			return (T) o;
		}
		var cons = new ArrayList<Constructor<T>>();
		T target = null;
		// 1. 创建对象
		for (var con : clazz.getDeclaredConstructors()) {
			// 默认和无参构造器不需要"@Inject"注解
			if (!con.isAnnotationPresent(Inject.class) && con.getParameterCount() > 0) {
				continue;
			}
			if (!con.trySetAccessible()) {
				continue;
			}
			cons.add((Constructor<T>) con);
		}
		if (cons.size() > 1) {
			throw new InjectException("duplicated constructor for injection class " + clazz.getCanonicalName()); // 按规范不允许有超过一个构造器添加"@Inject"标签
		}
		if (cons.size() == 0) {
			throw new InjectException("no accessible constructor for injection class " + clazz.getCanonicalName());
		}
		readyClasses.add(clazz); // 放入表示未完成的容器

		target = createFromConstructor(cons.get(0)); // -> 核心步骤，构造器注入

		readyClasses.remove(clazz); // 从未完成的容器取出

		// 2. 创建完成，判断是否为Singleton
		var isSingleton = clazz.isAnnotationPresent(Singleton.class);
		if (!isSingleton) {
			isSingleton = this.singletonClasses.containsKey(clazz);
		}
		if (isSingleton) {
			singletonInstances.put(clazz, target);
		}

		// 3. 递归注入该类中带@Inject注解的属性
		injectMembers(target);

		return target;
	}

	/**
	 *
	 * @param constructor the constructor used for creating new instance
	 * @return new instance
	 */
	private <T> T createFromConstructor(Constructor<T> constructor) {
		var params = new Object[constructor.getParameterCount()];
		var i = 0;
		for (Parameter parameter : constructor.getParameters()) {
			if (parameter.getClass().isInterface()) {
				throw new InjectException(String.format("can not create instance form Interface, the root class is %s",constructor.getDeclaringClass().getCanonicalName()));
			}
			if (readyClasses.contains(parameter.getType()) ){
				throw new InjectException(String.format("circular dependency on constructor , the root class is %s",constructor.getDeclaringClass().getCanonicalName()));
			}
			var param = createFromParameter(parameter);
			params[i++] = param;
		}
		try {
			return constructor.newInstance(params);
		} catch (Exception e) {
			throw new InjectException("create instance from constructor error", e);
		}
	}

	/**
	 *
	 * @param parameter from this parameter creating new instance
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createFromParameter(Parameter parameter) {
		var clazz = parameter.getType();
		// 从缓存队列中创建
		T t = createFromQualified(parameter.getDeclaringExecutable().getDeclaringClass(), clazz,
				parameter.getAnnotations());
		if (t != null) {
			return t;
		}
		return (T) createNew(clazz);
	}

	/**
	 *
	 * @param field from this field creating new instance
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createFromField(Field field) {
		var clazz = field.getType();
		// 从缓存队列中创建
		T t = createFromQualified(field.getDeclaringClass(), field.getType(), field.getAnnotations());
		if (t != null) {
			return t;
		}
		else
			return (T) createNew(clazz);
	}

	/**
	 * inject fields of a new instance after that being created
	 * @param instance an instance whose fields waiting for injection
	 */
	public <T> void injectMembers(T instance) {
		List<Field> fields = new ArrayList<>();
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Inject.class) && field.trySetAccessible()) {
				fields.add(field);
			}
		}
		for (Field field : fields) {
			try {
				if (field.get(instance) != null && field.isAnnotationPresent(Singleton.class)) {
					// 该成员变量为单例且已经被构造器创建（于目标对象初始化时），跳过
					continue;
				}
				Class<?> clazz = field.getType();
				Annotation[] annotations = field.getDeclaredAnnotations();
				Annotation namedAnnotation = null;
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
						namedAnnotation = annotation;
					}
				}
				// 1. 尝试从singletonInstances队列中获取
				Object obj = singletonInstances.get(clazz);
				if (obj == null) {
					// 2. 尝试从qualifiedInstances队列中获取
					Map<Annotation, Object> objectMap = qualifiedInstances.get(clazz);
					if (objectMap != null) {
						obj = objectMap.get(namedAnnotation);
					}
				}
				if (obj == null) {
					// 3. 都没有，重新创建一个
					obj = createFromField(field);
				}
				// 将生成的实例放入singletonInstances队列
				if (field.isAnnotationPresent(Singleton.class)) {
					singletonInstances.put(clazz, obj);
				}
				// 将生成的实例放入qualifiedInstances队列
				if (namedAnnotation != null) {
					Map<Annotation, Object> instanceMap = qualifiedInstances.getOrDefault(clazz, new HashMap<>());
					instanceMap.put(namedAnnotation, obj);
					qualifiedInstances.put(clazz, instanceMap);
				}
				field.set(instance, obj);
			} catch (Exception e) {
				throw new InjectException(
						String.format("set field for %s@%s error", instance.getClass().getCanonicalName(), field.getName()), e);
			}
		}
	}

	/**
	 * get a new instance of clazz
	 * @param clazz class type of target
	 * @return new instance
	 */
	public <T> T getInstance(Class<T> clazz) {
		return createNew(clazz);
	}


}
