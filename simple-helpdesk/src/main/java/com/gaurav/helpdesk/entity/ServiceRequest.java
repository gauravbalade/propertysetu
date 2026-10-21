package com.gaurav.helpdesk.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
@Entity
public class ServiceRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotBlank(message="Please enter a title") @Size(max=100) private String title;
 @NotBlank(message="Please describe the request") @Column(length=2000) private String description;
 @NotBlank(message="Please enter your name") private String requesterName;
 @NotBlank(message="Please choose a category") private String category;
 private String status="OPEN";
 private LocalDateTime createdAt=LocalDateTime.now();
 public ServiceRequest() {}
 public Long getId(){return id;} public void setId(Long id){this.id=id;}
 public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
 public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
 public String getRequesterName(){return requesterName;} public void setRequesterName(String requesterName){this.requesterName=requesterName;}
 public String getCategory(){return category;} public void setCategory(String category){this.category=category;}
 public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
 public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}