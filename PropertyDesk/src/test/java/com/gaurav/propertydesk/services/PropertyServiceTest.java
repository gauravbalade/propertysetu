package com.gaurav.propertydesk.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.gaurav.propertydesk.entities.PropertyRecord;
import com.gaurav.propertydesk.repositories.OwnerRepository;
import com.gaurav.propertydesk.repositories.PropertyLocationRepository;
import com.gaurav.propertydesk.repositories.PropertyRecordRepository;
import com.gaurav.propertydesk.repositories.VisitRequestRepository;
import com.gaurav.propertydesk.response_wrapper.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {
    @Mock
    private PropertyRecordRepository propertyRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private PropertyLocationRepository locationRepository;

    @Mock
    private VisitRequestRepository visitRepository;

    @InjectMocks
    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(propertyService, "response", new Response());
    }

    @Test
    void updatePropertyStatusRejectsUnknownStatus() {
        PropertyRecord property = new PropertyRecord();
        when(propertyRepository.findById(7L)).thenReturn(Optional.of(property));

        var result = propertyService.updatePropertyStatus(7L, "NOT_A_STATUS");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        verify(propertyRepository, never()).save(property);
    }

    @Test
    void deletePropertyRejectsPropertyWithLinkedLocation() {
        when(propertyRepository.existsById(7L)).thenReturn(true);
        when(locationRepository.existsByPropertyId(7L)).thenReturn(true);

        var result = propertyService.deleteProperty(7L);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        verify(propertyRepository, never()).deleteById(7L);
    }
}
