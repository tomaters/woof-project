package com.woof.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.woof.domain.Account;
import com.woof.domain.OrderHistory;
import com.woof.domain.PageRequest;
import com.woof.domain.Pagination;
import com.woof.domain.Service;
import com.woof.service.OrderHistoryService;
import com.woof.service.ServiceService;

import lombok.extern.java.Log;

@Log
@Controller
@RequestMapping("/service")
public class ServiceController {

	@Autowired
	private ServiceService serviceService;
	
	// needed when inserting an inquiry; for admin to view specific order history
	@Autowired
	private OrderHistoryService orderHistoryService;
	
	// MEMBER: write an inquiry (view)
	@PreAuthorize("hasRole('ROLE_MEMBER')")
	@GetMapping("/insertServiceForm")
	public String insertServiceForm(Account account, Principal principal, Model model) throws Exception {
		log.info("insertServiceForm");
		account.setUsername(principal.getName());
		model.addAttribute(account);
		
		// for dropdown selection of user's orderHistory numbers to made into a button for admin
		List<OrderHistory> orderHistoryList = orderHistoryService.getOrderHistoryList(principal.getName());
		model.addAttribute("orderHistoryList", orderHistoryList);
		return "service/insertService";
	}

	// MEMBER: write an inquiry (business logic) 
	@PreAuthorize("hasRole('ROLE_MEMBER')")
	@PostMapping("/insertService")
	public String insertService(Service service) throws Exception {
		log.info("insertService");
		serviceService.insertService(service);
		return "redirect:/service/getServiceList";
	}

	// ADMIN: respond to an inquiry (view)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/responseServiceForm")
	public String responseServiceForm(Service service, Model model) throws Exception {
		log.info("responseServiceForm");
		model.addAttribute("service",service);
		return "service/responseService";
	}

	// ADMIN: respond to an inquiry (business logic)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	// need to fix this typo
	@PostMapping("/respnoseService")
	public String respnoseService(Service service) throws Exception {
		serviceService.responseService(service);
		return "redirect:/service/getServiceList";
	}

	// MEMBER/ADMIN: delete inquiry - members can only delete their own inquiry; cannot delete if admin made a response
	@PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_ADMIN')")
	@PostMapping("/deleteService")
	public String deleteService(@AuthenticationPrincipal UserDetails userDetails, Service service, Principal principal,RedirectAttributes rttr) throws Exception {
	    String authList = userDetails.getAuthorities().toString();
	    
	    if(authList.equals("[ROLE_ADMIN]")) {
	    	serviceService.deleteService(service);
	    	return "redirect:/service/getServiceList";
	    }
	    
	    String msg = null;
	    if(!principal.getName().equals(service.getUsername())){
	    	msg = "Only the writer can delete this inquiry";
	    } else if(service.getResponse()!=""){
	    	msg = "Cannot delete an inquiry with a response";
    	} else {
    		serviceService.deleteService(service);	    		
    	}
	    
	    rttr.addFlashAttribute("msg", msg);
		return "redirect:/service/getServiceList";
	}

	// get list of inquiries
	@GetMapping("/getServiceList")
	public String getServiceList(Model model, PageRequest pageRequest, Pagination pagination, Principal principal, Account account) throws Exception {
		log.info("getServiceList");

		if (pageRequest.getCondition() == null) {
			pageRequest.setCondition("TITLE");
		}
		if (pageRequest.getKeyword() == null) {
			pageRequest.setKeyword("");
		}

		// null check for search function
		switch (pageRequest.getCondition()) {
			case "TITLE": {
				pageRequest.setKeywordTitle(pageRequest.getKeyword());
				pageRequest.setKeywordDesc("");
				break;
			}
			case "CONTENT": {
				pageRequest.setKeywordDesc(pageRequest.getKeyword());
				pageRequest.setKeywordTitle("");
				break;
			}
		}
		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(serviceService.countServiceList(pageRequest));
		model.addAttribute("pagination", pagination);
		List<Service> serviceList = serviceService.getServiceList(pageRequest);
		model.addAttribute("serviceList", serviceList);
		
		if (null != principal) {
			account.setUsername(principal.getName());
			model.addAttribute(account);
		}
		return "service/serviceList";
	}

	// navigation for FAQ page
	@GetMapping("/getFAQList")
	public String getFAQList(Service service) {
		return "service/FAQ";
	}

}
