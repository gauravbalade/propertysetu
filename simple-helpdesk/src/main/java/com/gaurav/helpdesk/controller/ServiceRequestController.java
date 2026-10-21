package com.gaurav.helpdesk.controller;
import com.gaurav.helpdesk.entity.ServiceRequest;
import com.gaurav.helpdesk.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
@Controller
public class ServiceRequestController {
 private final ServiceRequestService service;
 public ServiceRequestController(ServiceRequestService service){this.service=service;}
 @GetMapping("/") public String home(Model model){model.addAttribute("requests",service.findAll());model.addAttribute("openCount",service.countByStatus("OPEN"));model.addAttribute("progressCount",service.countByStatus("IN_PROGRESS"));model.addAttribute("closedCount",service.countByStatus("CLOSED"));return "requests";}
 @GetMapping("/requests/new") public String create(Model model){model.addAttribute("request",new ServiceRequest());model.addAttribute("pageTitle","Create a request");return "request-form";}
 @GetMapping("/requests/edit/{id}") public String edit(@PathVariable Long id,Model model){model.addAttribute("request",service.findById(id));model.addAttribute("pageTitle","Edit request");return "request-form";}
 @PostMapping("/requests/save") public String save(@Valid @ModelAttribute("request") ServiceRequest request,BindingResult result,Model model){if(result.hasErrors()){model.addAttribute("pageTitle",request.getId()==null?"Create a request":"Edit request");return "request-form";}if(request.getCreatedAt()==null)request.setCreatedAt(LocalDateTime.now());if(request.getStatus()==null||request.getStatus().isBlank())request.setStatus("OPEN");service.save(request);return "redirect:/";}
 @PostMapping("/requests/delete/{id}") public String delete(@PathVariable Long id){service.delete(id);return "redirect:/";}
 @PostMapping("/requests/status/{id}") public String status(@PathVariable Long id,@RequestParam String status){if(!List.of("OPEN","IN_PROGRESS","CLOSED").contains(status))throw new IllegalArgumentException("Invalid status");ServiceRequest request=service.findById(id);request.setStatus(status);service.save(request);return "redirect:/";}
}