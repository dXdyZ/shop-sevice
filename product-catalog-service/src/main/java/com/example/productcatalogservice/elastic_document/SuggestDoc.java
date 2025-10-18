package com.example.productcatalogservice.elastic_document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.core.suggest.Completion;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestDoc {
    @CompletionField
    private Completion name;

    @CompletionField
    private Completion brand;
}
