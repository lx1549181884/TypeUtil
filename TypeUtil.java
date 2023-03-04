package com.xxx.xxx;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 类型工具
 */
public class TypeUtil {

    /**
     * 获取父类泛型的具体类
     *
     * @param child       子类实例
     * @param parentClass 父类
     * @param index       泛型索引
     */
    public static <C extends P, P> Class<?> getClass(C child, Class<P> parentClass, int index) {
        // 获取第一个节点
        Node node = new Node(child, parentClass, index, true);
        // 循环判断
        while (true) {
            // 如果节点的参数化类型为具体类型，则返回具体类型
            if (node.type instanceof Class) {
                return (Class<?>) node.type;
            } else {
                // 否则继续寻找下一个节点
                node = node.next();
            }
            // 若没有节点了，则抛出异常
            if (node == null) throw new RuntimeException("not found class");
        }
    }

    /**
     * 节点
     */
    private static class Node {

        Object obj; // 子类实例
        Class<?> clazz; // 具体类型
        Type type; // 参数化类型

        /**
         * @param obj                  子类实例
         * @param clazz                具体类型
         * @param index                参数化类型索引
         * @param isCurrentOrLastIndex 是当前或上一个参数化类型索引。
         *                             因为首个节点比较特殊，用的是当前索引，需要区分
         */
        Node(Object obj, Class<?> clazz, int index, boolean isCurrentOrLastIndex) {
            this.obj = obj;
            this.clazz = clazz;
            if (isCurrentOrLastIndex) {
                this.type = clazz.getTypeParameters()[index];
            } else {
                this.type = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[index];
            }
            System.out.printf("节点 class=%s type=%s\n", clazz.getSimpleName(), type);
        }

        /**
         * 获取下一个节点
         */
        TypeUtil.Node next() {
            return next(obj, clazz, type);
        }

        /**
         * 获取下一个节点
         */
        TypeUtil.Node next(Object obj, Class<?> clazz, Type type) {
            TypeUtil.Node node = null;
            // 若参数化类型是泛型
            if (!(type instanceof Class)) {
                // 从类声明的参数化类型中查找
                for (int i = 0; i < clazz.getTypeParameters().length; i++) {
                    TypeVariable<? extends Class<?>> typeParameter = clazz.getTypeParameters()[i];
                    // 若有相同名称参数化类型，表示下一级子类重声明了泛型
                    if (type.equals(typeParameter)) {
                        // 返回下一级子类节点
                        node = new TypeUtil.Node(obj, getSubClass(obj, clazz), i, false);
                        break;
                    }
                }

                // 若下一级子类没有重声明泛型，则从外部类查找
                if (node == null) {
                    // 通过 Class 遍历 Field 查找外部类引用变量
                    for (Field field : obj.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        // 外部类引用变量名为"this$0"开头
                        if (field.getName().startsWith("this$0")) {
                            try {
                                // 用外部类的实例与类型获取节点
                                node = next(field.get(obj), field.getType(), type);
                                break;
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return node;
        }

        /**
         * 获取父类下一级子类
         */
        Class<?> getSubClass(Object child, Class<?> parentClass) {
            Class<?> clazz = child.getClass();
            while (true) {
                if (clazz.getSuperclass() == null || clazz.getSuperclass().isAssignableFrom(parentClass)) {
                    return clazz;
                } else {
                    clazz = clazz.getSuperclass();
                }
            }
        }
    }
}
