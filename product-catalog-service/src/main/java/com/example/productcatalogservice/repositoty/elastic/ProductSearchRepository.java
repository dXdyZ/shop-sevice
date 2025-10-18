package com.example.productcatalogservice.repositoty.elastic;

import com.example.productcatalogservice.elastic_document.ProductDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDoc, Long> {
}
