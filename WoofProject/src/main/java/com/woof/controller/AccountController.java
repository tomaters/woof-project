package com.woof.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.woof.domain.Account;
import com.woof.domain.PageRequest;
import com.woof.domain.Pagination;
import com.woof.service.AccountService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;

@Log
@Controller
@RequestMapping("/account")
public class AccountController {

	@Autowired
	private AccountService service;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// create account (view)
	@GetMapping("/createAccount")
	public String createAccountForm(Account account, Model model) throws Exception {
		return "account/login/createAccount";
	}
	
	// check login details
	@PutMapping("/accountCheck")
	public ResponseEntity<String> accountCheck(@RequestBody Account account, Model model) throws Exception {
		if(service.checkusername(account)) {
			return ResponseEntity.ok("Username already exists");
		} else {
			return ResponseEntity.ok("Username available");
		}
	}

	// create account (business logic)
	@PostMapping("/createAccount")
	public String createAccount(@Validated Account account, Model model, BindingResult result, RedirectAttributes rttr) throws Exception {
		// encode password
		String inputPassword = account.getPassword();
		account.setPassword(passwordEncoder.encode(inputPassword));
		// concatenate addresses 1-4 (in DB) into address (not in DB)
		String address = account.getAddress1() + " " + account.getAddress2() + " " + account.getAddress3() + " " + account.getAddress4();
		account.setAddress(address);
		service.registerAccount(account);
		rttr.addFlashAttribute("username", account.getUsername());
		return "redirect:/account/login";
	}

	// login
	@GetMapping("/login")
	public String loginForm(String error, String logout, Model model) {
		return "account/login/loginForm";
	}

	// login failure
	@GetMapping("/loginFail")
	public String loginFail(String error, String logout, Model model) {
		model.addAttribute("error", "xo");
		return "account/login/loginForm";
	}

	@RequestMapping("/logout")
	public String logoutForm() {
		return "account/login/loginForm";
	}

	// my account (view)
	@GetMapping("/myAccount")
	public String myAccountForm(Account account, Model model, Principal principal) throws Exception {
		account.setUsername(principal.getName());
		model.addAttribute(service.getAccount(account));
		return "account/myAccount/myAccountForm";
	}

	// my account (business logic)
	@PostMapping("/myAccount")
	public String myAccountFormpost(Account account, Model model) throws Exception {
		model.addAttribute(service.getAccount(account));
		return "account/myAccount/myAccountForm";
	}

	// edit account info (view)
	@GetMapping("/modifyAccountForm")
	public String modifyAccountForm(Account account, Model model, Principal principal) throws Exception {
		account.setUsername(principal.getName());
		model.addAttribute(service.getAccount(account));
		return "account/myAccount/modifyAccount";
	}

	// edit account info (business logic)
	@PostMapping("/modifyAccount")
	public String modifyAccount(@Validated Account account, Model model) throws Exception {
		log.info("modifyAccount : POST");
		// encode password
		String inputPassword = account.getPassword();
		account.setPassword(passwordEncoder.encode(inputPassword));
		// set address
		String address = account.getAddress1() + " " + account.getAddress2() + " " + account.getAddress3() + " " + account.getAddress4();
		account.setAddress(address);
		service.modifyAccount(account);
		model.addAttribute(service.getAccount(account));
		return "account/myAccount/myAccountForm";
	}

	// 'delete' account (view)
	@GetMapping("/deleteAccountForm")
	public String deleteAccountForm(Account account, Model model, Principal principal) throws Exception {
		account.setUsername(principal.getName());
		model.addAttribute(service.getAccount(account));
		return "account/myAccount/deleteAccount";
	}

	// 'delete' account (business logic)
	@PostMapping("/deleteAccount")
	public String deleteAccount(@ModelAttribute("account") Account account, BindingResult result, Model model,
			RedirectAttributes rttr, Principal principal, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// return page if there are errors
		if (result.hasErrors()) {
			return "account/myAccount/deleteAccount";
		}
		// if info is correct...
		if (principal != null && principal.getName().equals(account.getUsername())) {
			// use deleteAccount.jsp to delete
			service.deleteAccount(account);

			// log out and invalidate session
			SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
			logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

			// send message
			model.addAttribute("msg2", "SUCCESS2");

			// return to home page
			return "/";

		} else {
			// if unauthorized delete 
			model.addAttribute("msg3", "SUCCESS3");
			return "/";
		}

	}

	// set up admin (view)
	@GetMapping("/setup")
	public String setupAdminForm(Account account, Model model,PageRequest pageRequest) throws Exception {

		if(null==pageRequest.getKeyword()) {
			pageRequest.setKeyword("");
		}
		if (service.countAll(pageRequest) == 0) {
			return "account/login/setup";
		}
		model.addAttribute("msg", "SUCCESS");
		return "homewoof";
	}

	// set up admin (business logic)
	@PostMapping("/setup")
	public String setupAdmin(Account account, RedirectAttributes rttr, Model model,PageRequest pageRequest) throws Exception {
		if(null==pageRequest.getKeyword()) {
			pageRequest.setKeyword("");
		}
		String address = account.getAddress1() + " " + account.getAddress2() + " " + account.getAddress3() + " "
				+ account.getAddress4();
		account.setAddress(address);
		// check user table; if empty, create admin account
		if (service.countAll(pageRequest) == 0) {
			String inputPassword = account.getPassword();
			account.setPassword(passwordEncoder.encode(inputPassword));
			service.setupAdmin(account);
			rttr.addFlashAttribute("username", account.getUsername());
			return "/";
		}
		// return failure if not empty
		model.addAttribute("msg", "SUCCESS");
		return "/";
	}

	// manage users from admin (accountList)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/accountList")
	public String accountListForm(Account account, Model model, PageRequest pageRequest,Pagination pagination) throws Exception {
		if(null==pageRequest.getKeyword()) {
			pageRequest.setKeyword("");
		}
		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(service.countAll(pageRequest));
		model.addAttribute("pagination", pagination);
		model.addAttribute(pageRequest);
		model.addAttribute("list", service.getAccountList(pageRequest));
		return "account/admin/accountList";
	}

	// deactivate user from admin
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/accountStatusSwitch")
	public String accountStatusSwitch(Account account, Model model, Principal principal, PageRequest pageRequest,RedirectAttributes redirectAttributes) throws Exception {
	    service.restoreAccount(account);
		if(null==pageRequest.getKeyword()) {
			pageRequest.setKeyword("");
		}
		redirectAttributes.addAttribute("keyword",pageRequest.getKeyword());
	    return "redirect:/account/accountList";
	}
}
