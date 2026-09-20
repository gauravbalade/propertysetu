package com.gaurav.property.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyRequest {

    @NotNull
    private Long ownerId;

    @NotBlank
    @Size(max = 50)
    private String propertyNumber;

    @NotBlank
    @Size(max = 50)
    private String propertyType;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal area;

    @Size(max = 1000)
    private String description;
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getPropertyNumber() { return propertyNumber; }
    public void setPropertyNumber(String propertyNumber) { this.propertyNumber = propertyNumber; }
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}