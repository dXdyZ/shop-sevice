package com.example.productcatalogservice.elastic_document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryCategoryDoc {
    @Field(type = FieldType.Long)
    private Long id;

    @Field(name = "public_id", type = FieldType.Keyword)
    private String publicId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(name = "path_ids", type = FieldType.Long)
    private List<Long> pathIds;

    @Field(name = "path_slug", type = FieldType.Keyword)
    private List<String> pathSlug;
}
