package org.opensearchhealth.dagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Component;
import org.opensearchhealth.metrics.MetricsCalculation;
import org.opensearchhealth.util.OpenSearchUtil;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CommonModule.class})
public interface ServiceComponent {

    ObjectMapper getObjectMapper();

    OpenSearchUtil getOpenSearchUtil();

    MetricsCalculation getGitHubHealthCalculation();

}
