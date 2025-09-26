package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.AddAttributeValueDto;
import com.example.productcatalogservice.dto.create.CreateAttributeDto;
import com.example.productcatalogservice.entity.Attribute;
import com.example.productcatalogservice.exception.AttributeNotFoundException;
import com.example.productcatalogservice.exception.AttributeDuplicateException;
import com.example.productcatalogservice.exception.AttributeValueDuplicateException;
import com.example.productcatalogservice.repositoty.AttributeRepository;
import com.example.productcatalogservice.util.AttributeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttributeService {
    private final AttributeRepository attributeRepository;

    @Transactional
    public Attribute createAttribute(CreateAttributeDto createDto) {
        try {
            if (createDto.value() == null) {
                return attributeRepository.save(AttributeMapper.fromCreateDto(createDto));
            } else {
                Attribute attribute = AttributeMapper.fromCreateDto(createDto);
                attribute.addValue(createDto.value());
                return attributeRepository.save(attribute);
            }
        } catch (DataIntegrityViolationException exception) {
            throw new AttributeDuplicateException("Attribute by name: %s already exist".formatted(createDto.name()));
        }
    }

    @Transactional
    public Attribute addValue(AddAttributeValueDto addDto) throws AttributeValueDuplicateException {
        Attribute attribute = attributeRepository.findByPublicId(addDto.publicId())
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found"));

        attribute.addValue(addDto.value());

        return attributeRepository.save(attribute);
    }

    public Attribute getAttributeById(Long id) {
        return attributeRepository.findById(id)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found"));
    }

    public Attribute getAttributeByPublicId(UUID publicId) {
        return attributeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found"));
    }

    public Attribute getAttributeByName(String name) {
        return attributeRepository.findByName(name)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found"));
    }

    public Attribute getAttributeBySlug(String slug) {
        return attributeRepository.findBySlug(slug)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found"));
    }
}





