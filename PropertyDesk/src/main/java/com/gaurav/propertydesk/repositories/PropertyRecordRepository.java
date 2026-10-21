package com.gaurav.propertydesk.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.gaurav.propertydesk.entities.PropertyRecord;

@Repository
public interface PropertyRecordRepository extends JpaRepository<PropertyRecord, Long> {
    List<PropertyRecord> findByOwnerId(Long ownerId);
    List<PropertyRecord> findByStatusIgnoreCase(String status);
}