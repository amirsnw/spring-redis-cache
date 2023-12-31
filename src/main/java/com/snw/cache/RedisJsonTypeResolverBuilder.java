package com.snw.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import java.util.Collection;

/**
 * This builder just installs the custom type resolver that will encode the class name + serialVersionUid
 */
public class RedisJsonTypeResolverBuilder extends StdTypeResolverBuilder {

    public RedisJsonTypeResolverBuilder() {
        super();
    }

    @Override
    protected TypeIdResolver idResolver(MapperConfig<?> config,
                                        JavaType baseType,
                                        PolymorphicTypeValidator subtypeValidator,
                                        Collection<NamedType> subtypes,
                                        boolean forSer,
                                        boolean forDeser) {
        TypeIdResolver idResolver = super.idResolver(config, baseType, subtypeValidator, subtypes, forSer, forDeser);
        if (idResolver instanceof ClassNameIdResolver) {
            return new RedisJsonTypeIdResolver(baseType, config.getTypeFactory(), subtypeValidator);
        }
        return idResolver;
    }
}
