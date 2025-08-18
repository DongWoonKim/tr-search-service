package com.trevari.spring.trsearchservice.init;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trevari.spring.trsearchservice.infrastructure.persistence.BookDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookIndexInitializer implements CommandLineRunner {
    private static final String INDEX = "books";

    private final ElasticsearchClient es;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {

        boolean exists = es.indices().exists(e -> e.index(INDEX)).value();
        if (exists) return;

        es.indices().create(c -> c
                .index(INDEX)
                .settings(s -> s.numberOfShards("1").numberOfReplicas("0"))
                .mappings(m -> m
                        .properties("isbn", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(t -> t))
                        .properties("subtitle", p -> p.text(t -> t))
                        .properties("author", p -> p.text(t -> t))
                        .properties("publisher", p -> p.text(t -> t))
                        .properties("publishedDate", p -> p.date(d -> d.format("yyyy-MM-dd")))
                )
        );

        try (InputStream is = getClass().getResourceAsStream("/books.json")) {
            if (is == null) throw new IllegalStateException("books.json not found in classpath");
            List<BookDoc> books = objectMapper.readValue(
                    is, new com.fasterxml.jackson.core.type.TypeReference<List<BookDoc>>() {}
            );
            for (BookDoc b : books) {
                es.index(i -> i.index(INDEX).id(b.isbn()).document(b));
            }
        }


    }

}
