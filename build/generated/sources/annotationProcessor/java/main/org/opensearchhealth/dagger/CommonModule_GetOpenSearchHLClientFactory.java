package org.opensearchhealth.dagger;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import org.opensearch.client.RestHighLevelClient;

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
public final class CommonModule_GetOpenSearchHLClientFactory implements Factory<RestHighLevelClient> {
  private final CommonModule module;

  public CommonModule_GetOpenSearchHLClientFactory(CommonModule module) {
    this.module = module;
  }

  @Override
  public RestHighLevelClient get() {
    return getOpenSearchHLClient(module);
  }

  public static CommonModule_GetOpenSearchHLClientFactory create(CommonModule module) {
    return new CommonModule_GetOpenSearchHLClientFactory(module);
  }

  public static RestHighLevelClient getOpenSearchHLClient(CommonModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.getOpenSearchHLClient());
  }
}
