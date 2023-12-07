package org.opensearchhealth.dagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.opensearchhealth.health.HealthCalculation;
import org.opensearchhealth.util.OpenSearchUtil;

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
public final class CommonModule_GetGitHubHealthCalculationFactory implements Factory<HealthCalculation> {
  private final CommonModule module;

  private final Provider<OpenSearchUtil> openSearchUtilProvider;

  private final Provider<ObjectMapper> objectMapperProvider;

  public CommonModule_GetGitHubHealthCalculationFactory(CommonModule module,
      Provider<OpenSearchUtil> openSearchUtilProvider,
      Provider<ObjectMapper> objectMapperProvider) {
    this.module = module;
    this.openSearchUtilProvider = openSearchUtilProvider;
    this.objectMapperProvider = objectMapperProvider;
  }

  @Override
  public HealthCalculation get() {
    return getGitHubHealthCalculation(module, openSearchUtilProvider.get(), objectMapperProvider.get());
  }

  public static CommonModule_GetGitHubHealthCalculationFactory create(CommonModule module,
      Provider<OpenSearchUtil> openSearchUtilProvider,
      Provider<ObjectMapper> objectMapperProvider) {
    return new CommonModule_GetGitHubHealthCalculationFactory(module, openSearchUtilProvider, objectMapperProvider);
  }

  public static HealthCalculation getGitHubHealthCalculation(CommonModule instance,
      OpenSearchUtil openSearchUtil, ObjectMapper objectMapper) {
    return Preconditions.checkNotNullFromProvides(instance.getGitHubHealthCalculation(openSearchUtil, objectMapper));
  }
}
