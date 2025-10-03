package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.AddAttributeValueDto;
import com.example.productcatalogservice.dto.create.CreateAttributeDto;
import com.example.productcatalogservice.entity.Attribute;
import com.example.productcatalogservice.entity.AttributeValue;
import com.example.productcatalogservice.exception.AttributeDuplicateException;
import com.example.productcatalogservice.exception.AttributeNotFoundException;
import com.example.productcatalogservice.exception.AttributeValueDuplicateException;
import com.example.productcatalogservice.repositoty.AttributeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttributeServiceTest {
    @Mock
    private AttributeRepository attributeRepository;

    @InjectMocks
    private AttributeService attributeService;

    @Test
    void createAttribute_ShouldReturnSuccessCreatedAttribute_WhenValueIsProvidedAndDuplicateDoesNotExist() {
        var name = "color";
        var value = "red";
        Attribute attribute = Attribute.builder()
                .id(1L)
                .name(name)
                .build();

        attribute.addValue(value);

        var createDto = new CreateAttributeDto(name, value, null);

        when(attributeRepository.save(any(Attribute.class))).thenReturn(attribute);

        Attribute result = attributeService.createAttribute(createDto);

        assertEquals(attribute, result);
    }

    @Test
    void createAttribute_ShouldReturnSuccessCreatedAttribute_WhenDuplicateDoesNotExist() {
        var name = "color";
        Attribute attribute = Attribute.builder()
                .id(1L)
                .build();

        var createDto = new CreateAttributeDto(name, null, null);

        when(attributeRepository.save(any(Attribute.class))).thenReturn(attribute);

        Attribute result = attributeService.createAttribute(createDto);

        assertEquals(attribute, result);
    }

    @Test
    void createAttribute_ShouldReturnSuccessAttributeDuplicateException_WhenDuplicateExist() {
        var name = "color";
        var createDto = new CreateAttributeDto(name, null, null);

        when(attributeRepository.save(any(Attribute.class))).thenThrow(new DataIntegrityViolationException("violation of restrictions"));

        assertThrows(AttributeDuplicateException.class,
                () -> attributeService.createAttribute(createDto));
    }

    @Test
    void addValue_ShouldReturnSuccessUpdateAttribute_WhenNotDuplicateValue() {
        var value = "red";
        var publicId = UUID.randomUUID();
        var attribute = Attribute.builder()
                .id(1L)
                .name("color")
                .publicId(publicId)
                .build();

        var createDto = new AddAttributeValueDto(value, publicId);

        when(attributeRepository.findByPublicId(publicId)).thenReturn(Optional.of(attribute));
        when(attributeRepository.save(any(Attribute.class))).thenReturn(attribute);

        Attribute result = attributeService.addValue(createDto);

        assertEquals(attribute, result);
    }

    @Test
    void addValue_ShouldReturnAttributeValueDuplicateException_WhenDuplicateValue() {
        var value = "red";
        var publicId = UUID.randomUUID();
        var attribute = Attribute.builder()
                .id(1L)
                .name("color")
                .publicId(publicId)
                .build();

        attribute.getValues().add(AttributeValue.builder()
                        .id(1L)
                        .publicId(publicId)
                        .value(value)
                        .slug(value)
                .build());

        var createDto = new AddAttributeValueDto(value, publicId);

        when(attributeRepository.findByPublicId(publicId)).thenReturn(Optional.of(attribute));

        assertThrows(AttributeValueDuplicateException.class,
                () -> attributeService.addValue(createDto));

        verify(attributeRepository, never()).save(any(Attribute.class));
    }

    @Test
    void getAttributeById_ShouldReturnAttributeById_WhenAttributeExist() {
        var id = 1L;
        var attributee = Attribute.builder()
                .id(id)
                .name("color")
                .build();

        when(attributeRepository.findById(anyLong())).thenReturn(Optional.of(attributee));

        Attribute result = attributeService.getAttributeById(id);

        assertEquals(attributee, result);
    }

    @Test
    void getAttributeById_ShouldReturnAttributeNotFoundException_WhenAttributeDoesNotExist() {
        var id = 1L;

        when(attributeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AttributeNotFoundException.class,
                () -> attributeService.getAttributeById(id));
    }

    @Test
    void getAttributeByPublicId_ShouldReturnAttributeByPublicId_WhenAttributeDoesNotExist() {
        var publicId = UUID.randomUUID();
        var attributee = Attribute.builder()
                .id(1L)
                .publicId(publicId)
                .name("color")
                .build();

        when(attributeRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(attributee));

        Attribute result = attributeService.getAttributeByPublicId(publicId);

        assertEquals(attributee, result);
    }

    @Test
    void getAttributeByName_ShouldReturnAttributeByName_WhenAttributeExist() {
        var name = "color";
        var attributee = Attribute.builder()
                .id(1L)
                .name(name)
                .build();

        when(attributeRepository.findByName(anyString())).thenReturn(Optional.of(attributee));

        Attribute result = attributeService.getAttributeByName(name);

        assertEquals(attributee, result);
    }

    @Test
    void getAttributeByName_ShouldReturnAttributeNotFoundException_WhenAttributeDoesNotExist() {
        var name = "color";

        when(attributeRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(AttributeNotFoundException.class,
                () -> attributeService.getAttributeByName(name));
    }

    @Test
    void getAttributeBySlug_ShouldReturnAttributeBySlug_WhenAttributeExist() {
        var slug = "color";
        var attributee = Attribute.builder()
                .id(1L)
                .slug(slug)
                .build();

        when(attributeRepository.findBySlug(anyString())).thenReturn(Optional.of(attributee));

        Attribute result = attributeService.getAttributeBySlug(slug);

        assertEquals(attributee, result);
    }

    @Test
    void getAttributeBySlug_ShouldReturnAttributeNotFoundException_WhenAttributeDoesNotExist() {
        var slug = "color";

        when(attributeRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(AttributeNotFoundException.class,
                () -> attributeService.getAttributeBySlug(slug));
    }
}









