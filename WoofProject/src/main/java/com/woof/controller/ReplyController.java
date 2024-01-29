package com.woof.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.woof.domain.Account;
import com.woof.domain.PageRequest;
import com.woof.domain.Pagination;
import com.woof.domain.Reply;
import com.woof.domain.Review;
import com.woof.service.ReplyService;

import lombok.extern.java.Log;

@Log
@Controller
@RequestMapping("/reply")
public class ReplyController {
	
	@Autowired
	private ReplyService replyService;

	// get list of replies
	@GetMapping("/getReplyList")
	public String getReplyList(Model model, PageRequest pageRequest, Pagination pagination, Principal principal,
			Account account) throws Exception {
		log.info("getServiceList");
		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(replyService.countReplyList(pageRequest));
		model.addAttribute("pagination", pagination);
		
		List<Reply> replyList = replyService.getReplyList(pageRequest);
		model.addAttribute("replyList", replyList);
		if (null != principal) {
			account.setUsername(principal.getName());
			model.addAttribute(account);
		}
		return "reply/replyList";
	}

	// create a reply (view)
	@GetMapping("/insertReplyForm")
	public String insertReplyForm(Model model, Principal principal, Account account, Review review) throws Exception {
		log.info("insertReplyForm");
		if (null != principal) {
			account.setUsername(principal.getName());
			model.addAttribute(account);
		}
		return "reply/insertReply";
	}

	// create a reply (business logic)
	@PutMapping("/insertReply")
	public ResponseEntity<List> insertReply(@RequestBody Reply reply, PageRequest pageRequest) throws Exception {
		log.info("insertReply");
		if (reply.getReply() != null && !reply.getReply().equals("")) {
			replyService.insertReply(reply);
		}
		List<Reply> replyList = replyService.getReplyList(pageRequest);
		ResponseEntity<List> entity = null;
		if (replyList.size() != 0) {
			entity = new ResponseEntity<List>(replyList, HttpStatus.OK);
		}
		return entity;
	}

	@PutMapping("/getReplyList")
	public ResponseEntity<List> getReplyList(@RequestBody PageRequest pageRequest, Pagination pagination)
			throws Exception {
		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(replyService.countReplyList(pageRequest));

		List<Reply> replyList = replyService.getReplyList(pageRequest);
		ResponseEntity<List> entity = null;
		if (replyList.size() != 0) {
			entity = new ResponseEntity<List>(replyList, HttpStatus.OK);
			log.info("replyList : " + replyList.toString());
		}
		return entity;
	}

	@PutMapping("/getReplyPage")
	public ResponseEntity<List> getReplyPage(@RequestBody PageRequest pageRequest, Pagination pagination)
			throws Exception {
		log.info("getReplyPage");
		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(replyService.countReplyList(pageRequest));
		List<Pagination> pageList = new ArrayList<Pagination>();
		pageList.add(pagination);

		ResponseEntity<List> entity = new ResponseEntity<List>(pageList, HttpStatus.OK);
		return entity;
	}

	@PutMapping("/deleteReply")
	public ResponseEntity<List> deleteReply(@RequestBody Reply reply) throws Exception {
		replyService.deleteReply(reply);
		ResponseEntity<List> entity = null;
		return entity;
	}
}
