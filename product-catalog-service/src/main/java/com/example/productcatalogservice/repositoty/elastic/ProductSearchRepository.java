package com.example.productcatalogservice.repositoty.elastic;

import com.example.productcatalogservice.elastic_document.ProductDoc;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDoc, Long> {
    Optional<ProductDoc> findByPublicId(String publicId);

    @Query("""
            {
              "query": {
                "multi_match": {
                  "query": "?0",
                  "type": "best_fields",
                  "operator": "and",
                  "fuzziness": "AUTO:4,6",
                  "minimum_should_match": "70%",
                  "fields": [
                    "name^4",
                    "name.edge^5",
                    "name.raw^6",
                    "name.kw^6",
            
                    "brand.name^3",
                    "brand.name.edge^4",
                    "brand.name.raw^5",
                    "brand.name.kw^5",
            
                    "description^2",
                    "long_description^2",
            
                    "search_text^2"
                  ]
                }
              }
            }
            """)
    List<ProductDoc> searchByText(String name);
}
