package org.opensearchhealth.dagger;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import org.opensearchhealth.util.HealthScoreCalculation;

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
public final class CommonModule_GetHealthScoreCalculationFactory implements Factory<HealthScoreCalculation> {
  private final CommonModule module;

  public CommonModule_GetHealthScoreCalculationFactory(CommonModule module) {
    this.module = module;
  }

  @Override
  public HealthScoreCalculation get() {
    return getHealthScoreCalculation(module);
  }

  public static CommonModule_GetHealthScoreCalculationFactory create(CommonModule module) {
    return new CommonModule_GetHealthScoreCalculationFactory(module);
  }

  public static HealthScoreCalculation getHealthScoreCalculation(CommonModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.getHealthScoreCalculation());
  }
}
