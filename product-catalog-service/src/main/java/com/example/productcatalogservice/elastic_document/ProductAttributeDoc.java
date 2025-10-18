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
public class ProductAttributeDoc {
    @Field(name = "attribute_id", type = FieldType.Long)
    private Long attributeId;

    @Field(name = "attribute_public_id", type = FieldType.Keyword)
    private String attributePublicId;

    @Field(name = "attribute_name", type = FieldType.Text)
    private String attributeName;

    @Field(name = "attribute_slug", type = FieldType.Keyword)
    private String attributeSlug;

    @Field(name = "value_id", type = FieldType.Long)
    private Long valueId;

    @Field(name = "value_public_id", type = FieldType.Keyword)
    private String valuePublicId;

    @Field(name = "value", type = FieldType.Text)
    private String value;

    @Field(name = "value_slug", type = FieldType.Keyword)
    private String valueSlug;

    @Field(name = "filterable", type = FieldType.Boolean)
    private Boolean filterable;

    @Field(name = "is_active", type = FieldType.Boolean)
    private Boolean isActive;
}
