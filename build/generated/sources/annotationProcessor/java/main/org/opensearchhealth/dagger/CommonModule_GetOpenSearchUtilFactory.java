package org.opensearchhealth.dagger;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.opensearch.client.RestHighLevelClient;
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
public final class CommonModule_GetOpenSearchUtilFactory implements Factory<OpenSearchUtil> {
  private final CommonModule module;

  private final Provider<RestHighLevelClient> clientProvider;

  public CommonModule_GetOpenSearchUtilFactory(CommonModule module,
      Provider<RestHighLevelClient> clientProvider) {
    this.module = module;
    this.clientProvider = clientProvider;
  }

  @Override
  public OpenSearchUtil get() {
    return getOpenSearchUtil(module, clientProvider.get());
  }

  public static CommonModule_GetOpenSearchUtilFactory create(CommonModule module,
      Provider<RestHighLevelClient> clientProvider) {
    return new CommonModule_GetOpenSearchUtilFactory(module, clientProvider);
  }

  public static OpenSearchUtil getOpenSearchUtil(CommonModule instance,
      RestHighLevelClient client) {
    return Preconditions.checkNotNullFromProvides(instance.getOpenSearchUtil(client));
  }
}
