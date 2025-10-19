package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateAttributeDto;
import com.example.productcatalogservice.dto.create.CreateAttributeValueDto;
import com.example.productcatalogservice.entity.Attribute;
import com.example.productcatalogservice.entity.AttributeValue;
import com.example.productcatalogservice.exception.AttributeDuplicateException;
import com.example.productcatalogservice.exception.AttributeNotFoundException;
import com.example.productcatalogservice.exception.AttributeValueDuplicateException;
import com.example.productcatalogservice.repositoty.jpa.AttributeRepository;
import com.example.productcatalogservice.repositoty.jpa.AttributeValueRepository;
import com.example.productcatalogservice.util.mapper.AttributeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttributeService {
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;

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
    public Attribute addValue(CreateAttributeValueDto addDto) throws AttributeValueDuplicateException {
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

    public List<AttributeValue> getAttributeValuesByPublicIds(List<UUID> publicIds) {
        return attributeValueRepository.findAllByPublicIdIn(publicIds);
    }

    public List<AttributeValue> getAttributeValuesByIds(List<Long> ids) {
        return attributeValueRepository.findAllByIdIn(ids);
    }
}





