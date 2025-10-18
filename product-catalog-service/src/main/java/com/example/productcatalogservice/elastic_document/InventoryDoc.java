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
public class InventoryDoc {
    @Field(type = FieldType.Integer)
    private Integer quantity;

    @Field(name = "in_stock", type = FieldType.Boolean)
    private Boolean inStock;

    @Field(name = "low_stock", type = FieldType.Boolean)
    private Boolean lowStock;
}
