package com.example.productcatalogservice.entity.elastic;

import com.example.productcatalogservice.entity.Brand;
import com.example.productcatalogservice.entity.Category;
import com.example.productcatalogservice.entity.CustomAttribute;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Getter
@Setter
@Builder
@Document(indexName = "product_v1")
@NoArgsConstructor
@AllArgsConstructor
public class ProductEsDocument {
    @Id
    private Long id;
    private String publicId;
    private String sku;
    private String slug;

    @Field(type = FieldType.Text, analyzer = "fulltext_ru_en", searchAnalyzer = "fulltext_ru_en")
    private String name;

    @Field(type = FieldType.Text, analyzer = "fulltext_ru_en")
    private String description;

    @Field(type = FieldType.Text, analyzer = "fulltext_ru_en")
    private String longDescription;

    @Field(type = FieldType.Object)
    private Brand brand;

    @Field(type = FieldType.Object)
    private Category primaryCategory;

    @Field(type = FieldType.Nested)
    private List<Category> categories;

    @Field(type = FieldType.Nested)
    private List<CustomAttribute> customAttributes;

    private Long basePrice;
    private String currency;
//    private
}









