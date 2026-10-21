package com.gaurav.helpdesk.repository;
import com.gaurav.helpdesk.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest,Long> {
 List<ServiceRequest> findByStatus(String status);
}