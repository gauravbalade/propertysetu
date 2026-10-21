package com.gaurav.helpdesk.service;
import com.gaurav.helpdesk.entity.ServiceRequest;
import com.gaurav.helpdesk.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
@Service
public class ServiceRequestService {
 private final ServiceRequestRepository repository;
 public ServiceRequestService(ServiceRequestRepository repository){this.repository=repository;}
 public List<ServiceRequest> findAll(){return repository.findAll().stream().sorted(Comparator.comparing(ServiceRequest::getCreatedAt,Comparator.nullsLast(Comparator.reverseOrder()))).toList();}
 public ServiceRequest findById(Long id){return repository.findById(id).orElseThrow(()->new IllegalArgumentException("Request not found: "+id));}
 public ServiceRequest save(ServiceRequest request){if(request.getStatus()==null||request.getStatus().isBlank())request.setStatus("OPEN");return repository.save(request);}
 public void delete(Long id){repository.deleteById(id);}
 public long countByStatus(String status){return repository.findByStatus(status).size();}
}