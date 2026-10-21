package com.gaurav.propertydesk.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.gaurav.propertydesk.entities.VisitRequest;

@Repository
public interface VisitRequestRepository extends JpaRepository<VisitRequest, Long> {
    List<VisitRequest> findByPropertyId(Long propertyId);
    boolean existsByPropertyId(Long propertyId);
}