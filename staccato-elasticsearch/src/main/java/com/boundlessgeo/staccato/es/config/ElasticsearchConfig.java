package com.boundlessgeo.staccato.es.config;

import com.boundlessgeo.staccato.collection.CollectionMetadata;
import com.boundlessgeo.staccato.es.initializer.ElasticsearchIndexInitializer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Elasticsearch configuration.
 *
 * @author joshfix
 * Created on 12/6/17
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class ElasticsearchConfig {

    private final ElasticsearchConfigProps configProps;


    /**
     * Registers an instance of the high level client for Elasticsearch.
     *
     * @return An instance of Elasticsearch's high level rest client
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(configProps.getEs().getHost(), configProps.getEs().getPort(), configProps.getEs().getScheme()));
        RestClientBuilder.HttpClientConfigCallback httpClientConfigCallback = httpAsyncClientBuilder -> {
            httpAsyncClientBuilder
                    .setMaxConnTotal(configProps.getEs().getRestClientMaxConnectionsTotal())
                    .setMaxConnPerRoute(configProps.getEs().getRestClientMaxConnectionsPerRoute());

            if (null != configProps.getEs().getUser() && !configProps.getEs().getUser().isEmpty()) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(configProps.getEs().getUser(), configProps.getEs().getPassword()));
                httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }

            return httpAsyncClientBuilder;
        };

        builder.setHttpClientConfigCallback(httpClientConfigCallback);
        builder.setMaxRetryTimeoutMillis(configProps.getEs().getRestClientMaxRetryTimeoutMillis());

        //return new RestHighLevelClient(builder.build());
        return new RestHighLevelClient(builder);
    }

    /**
     * A custom parallel scheduler for executing non-reactive, blocking operations in a reactive threadpool.
     *
     * @return The scheduler
     */
    @Bean
    public Scheduler scheduler() {
        return Schedulers.newParallel("async-bridge",
                configProps.getEs().getRestClientMaxConnectionsTotal(), configProps.getAsyncBridgeThreadPool().isDaemon());
    }

    @Bean
    @ConditionalOnProperty(prefix = "staccato.es.index", value = "auto-initialize", havingValue = "true")
    public ElasticsearchIndexInitializer initializationService(RestHighLevelClient client,
                                                               List<CollectionMetadata> collectionMetadataList) {
        return new ElasticsearchIndexInitializer(collectionMetadataList, configProps, client);
    }

}
