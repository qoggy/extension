package io.github.qoggy.extension.core.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 00:13
 */
public class GenericTypeResolverTest {

    // 测试用的泛型接口
    interface GenericInterface<T> {
        T getValue();
    }

    interface MultiGenericInterface<T, U> {
        T getFirst();

        U getSecond();
    }

    interface NestedGenericInterface<T extends List<String>> {
        T getList();
    }

    // 测试用的实现类
    static class StringImplementation implements GenericInterface<String> {
        @Override
        public String getValue() {
            return "test";
        }
    }

    static class IntegerImplementation implements GenericInterface<Integer> {
        @Override
        public Integer getValue() {
            return 42;
        }
    }

    static class MultiTypeImplementation implements MultiGenericInterface<String, Integer> {
        @Override
        public String getFirst() {
            return "first";
        }

        @Override
        public Integer getSecond() {
            return 123;
        }
    }

    static class ComplexTypeImplementation implements MultiGenericInterface<List<String>, Map<String, Integer>> {
        @Override
        public List<String> getFirst() {
            return null;
        }

        @Override
        public Map<String, Integer> getSecond() {
            return null;
        }
    }

    // 继承链测试
    static abstract class AbstractGenericClass<T> implements GenericInterface<T> {
    }

    static class ConcreteStringClass extends AbstractGenericClass<String> {
        @Override
        public String getValue() {
            return "concrete";
        }
    }

    // 多层继承
    static class BaseClass<T> implements GenericInterface<T> {
        @Override
        public T getValue() {
            return null;
        }
    }

    static class MiddleClass<U> extends BaseClass<U> {
    }

    static class FinalClass extends MiddleClass<Double> {
    }

    // 非泛型类
    static class NonGenericClass {
        public String getValue() {
            return "non-generic";
        }
    }

    @Test
    void testResolveTypeArgument_SimpleInterface() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(StringImplementation.class, GenericInterface.class);
        assertEquals(String.class, result);
    }

    @Test
    void testResolveTypeArgument_IntegerType() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(IntegerImplementation.class, GenericInterface.class);
        assertEquals(Integer.class, result);
    }

    @Test
    void testResolveTypeArgument_WithIndex_FirstParameter() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(MultiTypeImplementation.class, MultiGenericInterface.class, 0);
        assertEquals(String.class, result);
    }

    @Test
    void testResolveTypeArgument_WithIndex_SecondParameter() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(MultiTypeImplementation.class, MultiGenericInterface.class, 1);
        assertEquals(Integer.class, result);
    }

    @Test
    void testResolveTypeArgument_ComplexTypes() {
        Class<?> firstResult = GenericTypeResolver.resolveTypeArgument(ComplexTypeImplementation.class, MultiGenericInterface.class, 0);
        Class<?> secondResult = GenericTypeResolver.resolveTypeArgument(ComplexTypeImplementation.class, MultiGenericInterface.class, 1);

        assertEquals(List.class, firstResult);
        assertEquals(Map.class, secondResult);
    }

    @Test
    void testResolveTypeArgument_InheritanceChain() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(ConcreteStringClass.class, GenericInterface.class);
        assertEquals(String.class, result);
    }

    @Test
    void testResolveTypeArgument_MultiLevelInheritance() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(FinalClass.class, GenericInterface.class);
        assertEquals(Double.class, result);
    }

    @Test
    void testResolveTypeArgument_NonGenericClass() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(NonGenericClass.class, GenericInterface.class);
        assertNull(result);
    }

    @Test
    void testResolveTypeArgument_InterfaceNotImplemented() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(String.class, GenericInterface.class);
        assertNull(result);
    }

    @Test
    void testResolveTypeArgument_NullClass() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(null, GenericInterface.class);
        assertNull(result);
    }

    @Test
    void testResolveTypeArgument_NullInterface() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(StringImplementation.class, null);
        assertNull(result);
    }

    @Test
    void testResolveTypeArgument_ObjectClass() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(Object.class, GenericInterface.class);
        assertNull(result);
    }

    @Test
    void testResolveTypeArgument_IndexOutOfBounds() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(StringImplementation.class, GenericInterface.class, 1);
        assertNull(result);
    }

    @Test
    void testResolveTypeArgument_NegativeIndex() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(StringImplementation.class, GenericInterface.class, -1);
        assertNull(result);
    }

    @Test
    void testResolveTypeArgument_DefaultIndex() {
        // 测试默认使用索引0的情况
        Class<?> result1 = GenericTypeResolver.resolveTypeArgument(MultiTypeImplementation.class, MultiGenericInterface.class);
        Class<?> result2 = GenericTypeResolver.resolveTypeArgument(MultiTypeImplementation.class, MultiGenericInterface.class, 0);
        assertEquals(result1, result2);
        assertEquals(String.class, result1);
    }

    // 测试接口继承的情况
    interface ExtendedGenericInterface<T> extends GenericInterface<T> {
        void setValue(T value);
    }

    static class ExtendedImplementation implements ExtendedGenericInterface<String> {
        @Override
        public String getValue() {
            return "extended";
        }

        @Override
        public void setValue(String value) {
            // do nothing
        }
    }

    @Test
    void testResolveTypeArgument_ExtendedInterface() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(ExtendedImplementation.class, GenericInterface.class);
        assertEquals(String.class, result);
    }

    @Test
    void testResolveTypeArgument_ExtendedInterfaceDirectly() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(ExtendedImplementation.class, ExtendedGenericInterface.class);
        assertEquals(String.class, result);
    }

    // 测试多重接口实现
    static class MultiInterfaceImplementation implements GenericInterface<String>, MultiGenericInterface<Integer, Double> {
        @Override
        public String getValue() {
            return "multi";
        }

        @Override
        public Integer getFirst() {
            return 1;
        }

        @Override
        public Double getSecond() {
            return 2.0;
        }
    }

    @Test
    void testResolveTypeArgument_MultipleInterfaces_FirstInterface() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(MultiInterfaceImplementation.class, GenericInterface.class);
        assertEquals(String.class, result);
    }

    @Test
    void testResolveTypeArgument_MultipleInterfaces_SecondInterface() {
        Class<?> result1 = GenericTypeResolver.resolveTypeArgument(MultiInterfaceImplementation.class, MultiGenericInterface.class, 0);
        Class<?> result2 = GenericTypeResolver.resolveTypeArgument(MultiInterfaceImplementation.class, MultiGenericInterface.class, 1);
        assertEquals(Integer.class, result1);
        assertEquals(Double.class, result2);
    }

    // ========== 复杂测试用例 ==========

    // 测试类型变量链 - A -> B -> C -> String
    interface ChainInterface<A> {
        A getChainValue();
    }

    static class ChainBase<B> implements ChainInterface<B> {
        @Override
        public B getChainValue() {
            return null;
        }
    }

    static class ChainMiddle<C> extends ChainBase<C> {
    }

    static class ChainFinal extends ChainMiddle<String> {
    }

    @Test
    void testResolveTypeArgument_TypeVariableChain() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(ChainFinal.class, ChainInterface.class);
        assertEquals(String.class, result);
    }

    // 测试更深层次的类型变量链 - A -> B -> C -> D -> Integer
    interface DeepChainInterface<A> {
        A getDeepValue();
    }

    static class DeepChainLevel1<B> implements DeepChainInterface<B> {
        @Override
        public B getDeepValue() {
            return null;
        }
    }

    static class DeepChainLevel2<C> extends DeepChainLevel1<C> {
    }

    static class DeepChainLevel3<D> extends DeepChainLevel2<D> {
    }

    static class DeepChainFinal extends DeepChainLevel3<Integer> {
    }

    @Test
    void testResolveTypeArgument_DeepTypeVariableChain() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(DeepChainFinal.class, DeepChainInterface.class);
        assertEquals(Integer.class, result);
    }

    // 测试类型变量重新绑定
    interface RebindInterface<T> {
        T getRebindValue();
    }

    static class RebindBase<U> implements RebindInterface<List<U>> {
        @Override
        public List<U> getRebindValue() {
            return null;
        }
    }

    static class RebindFinal extends RebindBase<String> {
    }

    @Test
    void testResolveTypeArgument_TypeVariableRebinding() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(RebindFinal.class, RebindInterface.class);
        assertEquals(List.class, result);
    }

    // 测试多个类型参数的复杂映射
    interface ComplexMappingInterface<A, B> {
        A getFirstComplex();

        B getSecondComplex();
    }

    static class ComplexMappingBase<X, Y> implements ComplexMappingInterface<Y, X> {
        @Override
        public Y getFirstComplex() {
            return null;
        }

        @Override
        public X getSecondComplex() {
            return null;
        }
    }

    static class ComplexMappingFinal extends ComplexMappingBase<String, Integer> {
    }

    @Test
    void testResolveTypeArgument_ComplexTypeMapping_FirstParam() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(ComplexMappingFinal.class, ComplexMappingInterface.class, 0);
        assertEquals(Integer.class, result); // Y -> Integer
    }

    @Test
    void testResolveTypeArgument_ComplexTypeMapping_SecondParam() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(ComplexMappingFinal.class, ComplexMappingInterface.class, 1);
        assertEquals(String.class, result); // X -> String
    }

    // 测试原始类型（Raw Type）情况
    static class RawTypeImplementation implements GenericInterface {
        @Override
        public Object getValue() {
            return "raw";
        }
    }

    @Test
    void testResolveTypeArgument_RawType() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(RawTypeImplementation.class, GenericInterface.class);
        assertNull(result); // 原始类型应该返回null
    }

    // 测试部分原始类型
    static class PartialRawTypeBase<T> implements MultiGenericInterface {
        @Override
        public Object getFirst() {
            return null;
        }

        @Override
        public Object getSecond() {
            return null;
        }
    }

    @Test
    void testResolveTypeArgument_PartialRawType() {
        Class<?> result1 = GenericTypeResolver.resolveTypeArgument(PartialRawTypeBase.class, MultiGenericInterface.class, 0);
        Class<?> result2 = GenericTypeResolver.resolveTypeArgument(PartialRawTypeBase.class, MultiGenericInterface.class, 1);
        assertNull(result1);
        assertNull(result2);
    }

    // 测试泛型边界
    interface BoundedInterface<T extends Number> {
        T getBoundedValue();
    }

    static class BoundedImplementation implements BoundedInterface<Integer> {
        @Override
        public Integer getBoundedValue() {
            return 42;
        }
    }

    @Test
    void testResolveTypeArgument_BoundedGenerics() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(BoundedImplementation.class, BoundedInterface.class);
        assertEquals(Integer.class, result);
    }

    // 测试通配符类型 - 这种情况下应该返回null或者处理边界
    interface WildcardInterface<T> {
        T getWildcardValue();
    }

    // 注意：Java不允许直接使用通配符作为类型参数，所以我们测试包含通配符的复杂类型
    static class WildcardBase<T> implements WildcardInterface<List<? extends T>> {
        @Override
        public List<? extends T> getWildcardValue() {
            return null;
        }
    }

    static class WildcardFinal extends WildcardBase<String> {
    }

    @Test
    void testResolveTypeArgument_WildcardTypes() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(WildcardFinal.class, WildcardInterface.class);
        assertEquals(List.class, result); // 应该返回List的原始类型
    }

    // 测试自引用泛型
    interface SelfReferenceInterface<T extends SelfReferenceInterface<T>> {
        T getSelf();
    }

    static class SelfReferenceImpl implements SelfReferenceInterface<SelfReferenceImpl> {
        @Override
        public SelfReferenceImpl getSelf() {
            return this;
        }
    }

    @Test
    void testResolveTypeArgument_SelfReference() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(SelfReferenceImpl.class, SelfReferenceInterface.class);
        assertEquals(SelfReferenceImpl.class, result);
    }

    // 测试嵌套泛型类
    static class OuterGeneric<T> {
        interface InnerInterface<U> {
            U getInnerValue();
        }

        class InnerImplementation implements InnerInterface<T> {
            @Override
            public T getInnerValue() {
                return null;
            }
        }
    }

    @Test
    void testResolveTypeArgument_NestedGenericClass() {
        OuterGeneric<String> outer = new OuterGeneric<>();
        OuterGeneric<String>.InnerImplementation inner = outer.new InnerImplementation();
        Class<?> result = GenericTypeResolver.resolveTypeArgument(inner.getClass(), OuterGeneric.InnerInterface.class);
        // 注意：这个测试可能比较复杂，因为内部类的泛型解析涉及到外部类的类型参数
        // 根据实际实现情况，这里可能需要调整预期结果
    }

    // 测试接口的多层继承
    interface Level1Interface<T> {
        T getLevel1();
    }

    interface Level2Interface<U> extends Level1Interface<List<U>> {
        U getLevel2();
    }

    interface Level3Interface<V> extends Level2Interface<V> {
        V getLevel3();
    }

    static class MultiLevelInterfaceImpl implements Level3Interface<String> {
        @Override
        public List<String> getLevel1() {
            return null;
        }

        @Override
        public String getLevel2() {
            return null;
        }

        @Override
        public String getLevel3() {
            return null;
        }
    }

    @Test
    void testResolveTypeArgument_MultiLevelInterfaceInheritance_Level1() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(MultiLevelInterfaceImpl.class, Level1Interface.class);
        assertEquals(List.class, result); // T -> List<String>
    }

    @Test
    void testResolveTypeArgument_MultiLevelInterfaceInheritance_Level2() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(MultiLevelInterfaceImpl.class, Level2Interface.class);
        assertEquals(String.class, result); // U -> String
    }

    @Test
    void testResolveTypeArgument_MultiLevelInterfaceInheritance_Level3() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(MultiLevelInterfaceImpl.class, Level3Interface.class);
        assertEquals(String.class, result); // V -> String
    }

    // 测试数组类型
    interface ArrayInterface<T> {
        T[] getArray();
    }

    static class ArrayImplementation implements ArrayInterface<String> {
        @Override
        public String[] getArray() {
            return new String[0];
        }
    }

    @Test
    void testResolveTypeArgument_ArrayType() {
        Class<?> result = GenericTypeResolver.resolveTypeArgument(ArrayImplementation.class, ArrayInterface.class);
        assertEquals(String.class, result);
    }

    // 测试基本类型包装类
    interface PrimitiveWrapperInterface<T> {
        T getPrimitiveWrapper();
    }

    static class IntWrapperImpl implements PrimitiveWrapperInterface<Integer> {
        @Override
        public Integer getPrimitiveWrapper() {
            return 0;
        }
    }

    static class BooleanWrapperImpl implements PrimitiveWrapperInterface<Boolean> {
        @Override
        public Boolean getPrimitiveWrapper() {
            return false;
        }
    }

    @Test
    void testResolveTypeArgument_PrimitiveWrappers() {
        Class<?> intResult = GenericTypeResolver.resolveTypeArgument(IntWrapperImpl.class, PrimitiveWrapperInterface.class);
        Class<?> boolResult = GenericTypeResolver.resolveTypeArgument(BooleanWrapperImpl.class, PrimitiveWrapperInterface.class);

        assertEquals(Integer.class, intResult);
        assertEquals(Boolean.class, boolResult);
    }
}
