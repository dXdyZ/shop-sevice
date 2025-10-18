package com.example.productcatalogservice.util.mapper;

import com.example.productcatalogservice.elastic_document.*;
import com.example.productcatalogservice.entity.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductDocumentMapper {

    public ProductDoc toProduct(Product product, Optional<Inventory> inventoryOpt) {
        Brand brand = product.getBrand();
        Category primary = product.getPrimaryCategory();
        List<CategoryDoc> categories = product.getCategories().stream()
                .map(this::toCategoryDoc)
                .toList();

        List<ProductAttributeDoc> attributes = product.getAttributeValues().stream()
                .map(this::toAttributeDoc)
                .toList();

        List<CustomAttributeDoc> customAttribute = product.getCustomAttributes().stream()
                .map(custom -> CustomAttributeDoc.builder()
                        .name(custom.getName())
                        .value(custom.getValue())
                        .build())
                .toList();

        InventoryDoc inventory = inventoryOpt
                .map(inv -> InventoryDoc.builder()
                        .quantity(inv.getQuantity())
                        .inStock(inv.getQuantity() > 0)
                        .lowStock(inv.getLowStockThreshold() != null && inv.getQuantity() != null && inv.getQuantity() > 0 && inv.getQuantity() <= inv.getLowStockThreshold())
                        .build())
                .orElse(null);

        SuggestDoc suggest = SuggestDoc.builder()
                .name(new Completion(List.of(product.getName())))
                .brand(new Completion(List.of(brand.getName())))
                .build();

        String searchText = buildSearchText(product, brand, primary, categories, attributes);

        return ProductDoc.builder()
                .id(product.getId())
                .version(product.getVersion())
                .publicId(product.getPublicId().toString())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .longDescription(product.getLongDescription())
                .brand(toBrandDoc(brand))
                .primaryCategory(toPrimaryCategoryDo(primary))
                .categories(categories)
                .attributes(attributes)
                .customAttributes(customAttribute)
                .rating(product.getRating())
                .basePrice(product.getBasePrice().doubleValue())
                .currency(product.getCurrency())
                .ratingCount(product.getRatingCount())
                .isActive(product.getIsActive())
                .isAvailable(product.getIsAvailable())
                .inventory(inventory)
                .suggest(suggest)
                .searchText(searchText)
                .build();
    }

    public BrandDoc toBrandDoc(Brand b) {
        return BrandDoc.builder()
                .id(b.getId())
                .publicId(b.getPublicId().toString())
                .name(b.getName())
                .slug(b.getSlug())
                .isActive(b.getIsActive())
                .build();
    }

    public PrimaryCategoryDoc toPrimaryCategoryDo(Category c) {
        List<Long> pathIds = new ArrayList<>();
        List<String> pathSlugs = new ArrayList<>();
        Category cur = c;
        while (cur != null) {
            if (cur.getId() != null) {
                pathIds.add(cur.getId());
            }
            if (cur.getSlug() != null) {
                pathSlugs.add(cur.getSlug());
            }
            cur = cur.getParent();
        }
        Collections.reverse(pathIds);
        Collections.reverse(pathSlugs);

        return PrimaryCategoryDoc.builder()
                .id(c.getId())
                .publicId(c.getPublicId().toString())
                .name(c.getName())
                .pathIds(pathIds)
                .pathSlug(pathSlugs)
                .build();
    }

    public CategoryDoc toCategoryDoc(Category c) {
        return CategoryDoc.builder()
                .id(c.getId())
                .publicId(c.getPublicId().toString())
                .name(c.getName())
                .slug(c.getSlug())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .build();
    }

    public ProductAttributeDoc toAttributeDoc(ProductAttributeValue pav) {
        AttributeValue value = pav.getAttributeValue();
        Attribute attribute = value.getAttribute();

        return ProductAttributeDoc.builder()
                .attributeId(attribute.getId())
                .attributePublicId(attribute.getPublicId().toString())
                .attributeName(attribute.getName())
                .attributeSlug(attribute.getSlug())
                .valueId(value.getId())
                .valuePublicId(value.getPublicId().toString())
                .value(value.getValue())
                .valueSlug(value.getSlug())
                .filterable(attribute.getFilterable())
                .isActive(attribute.getIsActive())
                .build();
    }

    private String buildSearchText(Product product, Brand brand, Category primary, List<CategoryDoc> categories, List<ProductAttributeDoc> attributes) {
        List<String> parts = new ArrayList<>();
        parts.add(product.getName());
        parts.add(product.getDescription());
        parts.add(product.getLongDescription());
        parts.add(brand.getName());
        parts.add(primary.getName());
        parts.addAll(categories.stream().map(CategoryDoc::getName).toList());
        parts.addAll(attributes.stream().map(ProductAttributeDoc::getAttributeName).toList());
        return parts.stream().filter(Objects::nonNull).collect(Collectors.joining(" "));
    }
}
