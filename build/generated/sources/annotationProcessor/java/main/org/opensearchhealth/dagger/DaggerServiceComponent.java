package org.opensearchhealth.dagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.opensearch.client.RestHighLevelClient;
import org.opensearchhealth.health.HealthCalculation;
import org.opensearchhealth.util.OpenSearchUtil;

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
public final class DaggerServiceComponent {
  private DaggerServiceComponent() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static ServiceComponent create() {
    return new Builder().build();
  }

  public static final class Builder {
    private CommonModule commonModule;

    private Builder() {
    }

    public Builder commonModule(CommonModule commonModule) {
      this.commonModule = Preconditions.checkNotNull(commonModule);
      return this;
    }

    public ServiceComponent build() {
      if (commonModule == null) {
        this.commonModule = new CommonModule();
      }
      return new ServiceComponentImpl(commonModule);
    }
  }

  private static final class ServiceComponentImpl implements ServiceComponent {
    private final ServiceComponentImpl serviceComponentImpl = this;

    private Provider<ObjectMapper> getObjectMapperProvider;

    private Provider<RestHighLevelClient> getOpenSearchHLClientProvider;

    private Provider<OpenSearchUtil> getOpenSearchUtilProvider;

    private Provider<HealthCalculation> getGitHubHealthCalculationProvider;

    private ServiceComponentImpl(CommonModule commonModuleParam) {

      initialize(commonModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final CommonModule commonModuleParam) {
      this.getObjectMapperProvider = DoubleCheck.provider(CommonModule_GetObjectMapperFactory.create(commonModuleParam));
      this.getOpenSearchHLClientProvider = DoubleCheck.provider(CommonModule_GetOpenSearchHLClientFactory.create(commonModuleParam));
      this.getOpenSearchUtilProvider = DoubleCheck.provider(CommonModule_GetOpenSearchUtilFactory.create(commonModuleParam, getOpenSearchHLClientProvider));
      this.getGitHubHealthCalculationProvider = DoubleCheck.provider(CommonModule_GetGitHubHealthCalculationFactory.create(commonModuleParam, getOpenSearchUtilProvider, getObjectMapperProvider));
    }

    @Override
    public ObjectMapper getObjectMapper() {
      return getObjectMapperProvider.get();
    }

    @Override
    public OpenSearchUtil getOpenSearchUtil() {
      return getOpenSearchUtilProvider.get();
    }

    @Override
    public HealthCalculation getGitHubHealthCalculation() {
      return getGitHubHealthCalculationProvider.get();
    }
  }
}
