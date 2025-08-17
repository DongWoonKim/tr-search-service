package com.trevari.spring.trsearchservice.infrastructure.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfig {
    @Value("${spring.elasticsearch.host:localhost}")
    private String host;

    @Value("${spring.elasticsearch.port:9200}")
    private int port;

    @Value("${spring.elasticsearch.scheme:http}")
    private String scheme;

    /** Java 8 날짜 타입(LocalDate 등) 직렬화/역직렬화 지원 */
    @Bean
    public ObjectMapper esObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // "yyyy-MM-dd"
        return om;
    }

    /** Low-level RestClient (공용, 종료 시 close) */
    @Bean(destroyMethod = "close")
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(host, port, scheme))
                .setRequestConfigCallback(b -> b
                        .setConnectTimeout(2000)
                        .setSocketTimeout(5000)
                )
                .build();
    }

    /** Transport: RestClient + JacksonJsonpMapper(ObjectMapper 주입) */
    @Bean
    public ElasticsearchTransport transport(RestClient restClient, ObjectMapper esObjectMapper) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper(esObjectMapper));
    }

    /** High-level 클라이언트 */
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
