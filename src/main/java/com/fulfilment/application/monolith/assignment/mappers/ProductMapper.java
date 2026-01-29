package com.fulfilment.application.monolith.assignment.mappers;

import com.fulfilment.application.monolith.assignment.domain.models.ProductDto;
import com.fulfilment.application.monolith.products.Product;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductMapper {
    public ProductDto toDto(Product product) {
        if (product == null) return null;
        return new ProductDto(
                product.id,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock()
        );
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) return null;
        Product product = new Product();
        product.id = dto.id();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        return product;
    }
}
