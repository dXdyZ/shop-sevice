package com.example.productcatalogservice.service.elastic;

import com.example.productcatalogservice.dto.event.ProductCreateEvent;
import com.example.productcatalogservice.elastic_document.ProductDoc;
import com.example.productcatalogservice.exception.elastic.ProductDocumentNotFoundException;
import com.example.productcatalogservice.repositoty.elastic.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ProductSearchService {
    private final ProductSearchRepository productSearchRepository;
    private final ProductDataProvider productDataProvider;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public ProductDoc createProductSearchDocument(ProductCreateEvent event) {
        ProductDoc productDoc = productDataProvider.getProductData(event.productId(), event.inventoryId());

        return productSearchRepository.save(productDoc);
    }

    public ProductDoc getProductDocById(Long id) {
        return productSearchRepository.findById(id).orElseThrow(
                () -> new ProductDocumentNotFoundException("Product document by id: %s not found".formatted(id)));
    }

    public ProductDoc getProductDocByPublicId(String publicId) {
        return productSearchRepository.findByPublicId(publicId).orElseThrow(
                () -> new ProductDocumentNotFoundException("Product document by public id: %s not found".formatted(publicId)));
    }
}
