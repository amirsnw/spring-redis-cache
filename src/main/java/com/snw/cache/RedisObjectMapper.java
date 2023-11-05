package com.snw.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


/**
 * This object mapper is used to serialize/deserialize objects to/from Redis. The strategy used by this mapper encodes both the type (class name) along
 * with the type's serialVersionUID. The serialization will fail if a cached object's serialVersionUid (or any of its nested objects' serialVersionUid) does
 * NOT match that of the serialVersionUid in memory. This mapper has been configured to bubble up those serialization exceptions to the caller (Redis Client)
 * which are then caught in the "UnifiedRedisCache". See the overridden logic within the Cache.get() method.
 * <p>
 * This specialized mapper is installed into the RedisOperations within the "CacheAutoConfiguration" class.
 * <p>
 * NOTE: This mapper is NOT registered as a Spring bean because we do not want to inadvertently inject it to other places where we do JSON serialization.
 * <p>
 * NOTE: This mapper will recursively serialize/deserialize child objects and a mismatched UID in a child object will
 * also cause deserialization to fail.
 */
public class RedisObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	public RedisObjectMapper() {
		super();

		//Install the "type" resolver that examines serialVersionUids.
		RedisJsonTypeResolverBuilder typer = new RedisJsonTypeResolverBuilder();

		// specifies that the type information should be included using
		// the class name of the actual object's type. This means that
		// when serializing an object, the class name will be added as
		// part of the JSON data to indicate the actual class type.
		// null is used for the typeId property, which means the default
		// type identifier property name (usually "$type") will be used.
		typer.init(JsonTypeInfo.Id.CLASS, null);

		// specifies that the type information will be included as a
		// wrapper array. This means that the serialized JSON data will
		// contain an array where the first element is the type information
		// (usually the class name), and the second element is the actual object data
		typer.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
		setDefaultTyping(typer);

		// We want serialization exceptions to bubble up to the caller.
		configure(DeserializationFeature.WRAP_EXCEPTIONS, false);
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

		// The JavaTimeModule is a module provided by Jackson specifically
		// for handling Java Date and Time classes introduced in Java 8 and
		// later, like java.time.LocalDate, java.time.LocalDateTime, and
		// java.time.ZonedDateTime.
		// add support for serializing and deserializing Java Date and Time objects in JSON.
		registerModule(new JavaTimeModule());
	}
}
