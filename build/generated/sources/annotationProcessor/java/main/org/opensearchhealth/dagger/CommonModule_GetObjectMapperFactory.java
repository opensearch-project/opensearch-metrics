package org.opensearchhealth.dagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class CommonModule_GetObjectMapperFactory implements Factory<ObjectMapper> {
  private final CommonModule module;

  public CommonModule_GetObjectMapperFactory(CommonModule module) {
    this.module = module;
  }

  @Override
  public ObjectMapper get() {
    return getObjectMapper(module);
  }

  public static CommonModule_GetObjectMapperFactory create(CommonModule module) {
    return new CommonModule_GetObjectMapperFactory(module);
  }

  public static ObjectMapper getObjectMapper(CommonModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.getObjectMapper());
  }
}
