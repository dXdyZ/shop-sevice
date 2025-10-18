package com.example.productcatalogservice.elastic_document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDoc {
    @Field(type = FieldType.Long)
    private Long id;

    @Field(name = "public_id", type = FieldType.Keyword)
    private String publicId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(name = "parent_id", type = FieldType.Long)
    private Long parentId;
}
