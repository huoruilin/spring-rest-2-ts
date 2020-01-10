package com.blueveery.springrest2ts.converters;

import com.blueveery.springrest2ts.implgens.ImplementationGenerator;
import com.blueveery.springrest2ts.tsmodel.TSComplexType;
import com.blueveery.springrest2ts.tsmodel.TSField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public interface ObjectMapper {
    List<TSField> addTypeLevelSpecificFields(Class javaType, TSComplexType tsComplexType);

    boolean filterClass(Class clazz);

    boolean filter(Field field);
    boolean filter(Method method, boolean isGetter);

    List<TSField> mapJavaPropertyToField(Property property, TSComplexType tsComplexType,
                                         ComplexTypeConverter complexTypeConverter, ImplementationGenerator implementationGenerator, NullableTypesStrategy nullableTypesStrategy);


    String getPropertyName(Field field);

    String getPropertyName(Method method, boolean isGetter);
}
