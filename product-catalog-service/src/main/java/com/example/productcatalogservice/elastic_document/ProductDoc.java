package com.example.productcatalogservice.elastic_document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "product_v1")
public class ProductDoc {
    @Id
    private Long id;

    @Version
    private Long version;

    @Field(name = "public_id", type = FieldType.Keyword)
    private String publicId;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(name = "name", type = FieldType.Text)
    private String name;

    @Field(name = "description", type = FieldType.Text)
    private String description;

    @Field(name = "long_description", type = FieldType.Text)
    private String longDescription;

    @Field(type = FieldType.Object)
    private BrandDoc brand;

    @Field(name = "primary_category", type = FieldType.Object)
    private PrimaryCategoryDoc primaryCategory;

    @Field(type = FieldType.Nested)
    private List<CategoryDoc> categories;

    @Field(type = FieldType.Nested)
    private List<ProductAttributeDoc> attributes;

    @Field(name = "custom_attributes", type = FieldType.Nested)
    private List<CustomAttributeDoc> customAttributes;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(name = "base_price", type = FieldType.Scaled_Float, scalingFactor = 100)
    private Double basePrice;

    @Field(type = FieldType.Keyword)
    private String currency;

    @Field(name = "rating_count", type = FieldType.Integer)
    private Long ratingCount;

    @Field(name = "is_active", type = FieldType.Boolean)
    private Boolean isActive;

    @Field(name = "is_available", type = FieldType.Binary)
    private Boolean isAvailable;

    @Field(type = FieldType.Object)
    private InventoryDoc inventory;

    @Field(type = FieldType.Object)
    private SuggestDoc suggest;

    @Field(name = "search_text", type = FieldType.Text)
    private String searchText;
}









