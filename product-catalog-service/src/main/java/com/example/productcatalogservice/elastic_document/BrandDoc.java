package com.example.productcatalogservice.elastic_document;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class
BrandDoc {
    @Field(type = FieldType.Long)
    private Long id;

    @Field(name = "public_id", type = FieldType.Keyword)
    private String publicId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;
}
