package com.woof.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.MergedAnnotations.Search;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.woof.domain.Notice;
import com.woof.domain.PageRequest;
import com.woof.domain.Pagination;
import com.woof.service.NoticeService;

import lombok.extern.java.Log;

@Log
@Controller
@RequestMapping("/notice")
public class NoticeController {

	@Autowired
	private NoticeService noticeService;

	// retrieve kakaomap appkey in application.properties (gitignored)
	@Value("${kakaomap.appkey}")
	private String kakaoMapAppkey;

	// get details of an announcement
	@GetMapping("/getNotice")
	public String getNotice(Notice notice, Model model) throws Exception {
		log.info("getNotice");
		noticeService.addNoticeViewCount(notice);
		model.addAttribute(noticeService.getNotice(notice));
		return "about/notice";
	}

	// get list of announcements
	@GetMapping("/getNoticeList")
	public String getNoticeList(Model model, PageRequest pageRequest,Pagination pagination) throws Exception {
		log.info("getNoticeList");
		
		if (pageRequest.getCondition() == null) {
			pageRequest.setCondition("TITLE");
		}
		if (pageRequest.getKeyword() == null) {
			pageRequest.setKeyword("");
		}

		// null check for search information
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
		pagination.setTotalCount(noticeService.countNoticeList(pageRequest));
		model.addAttribute("pagination", pagination);
		
		List<Notice> noticeList = noticeService.getNoticeList(pageRequest);
		model.addAttribute("noticeList", noticeList);
		
		return "about/noticeList";
	}

	// write an announcement (view)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/insertNoticeForm")
	public String insertNoticeForm(Notice notice) throws Exception {
		return "admin/notices/insertNotice";
	}
	
	// write an announcement (business logic)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/insertNotice")
	public String insertNotice(Notice notice) throws Exception {
		noticeService.insertNotice(notice);
		return "redirect:/notice/getNoticeList";
	}
	
	//modify announcement (view)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/modifyNoticeForm")
	public String modifyNoticeForm(Notice notice, Model model) throws Exception {
		model.addAttribute(noticeService.getNotice(notice));
		return "admin/notices/modifyNotice";
	}
	
	//modify announcement (business logic)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/modifyNotice")
	public String modifyNotice(Notice notice) throws Exception {
		log.info("modifyNotice");
		noticeService.modifyNotice(notice);
		return "redirect:/notice/getNoticeList";
	}
	
	// delete announcement
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/deleteNotice")
	public String deleteNotice(Notice notice) throws Exception {
		noticeService.deleteNotice(notice);
		return "redirect:/notice/getNoticeList";
	}
	
	// navigation for about page
	@GetMapping("/getAbout")
	public String getAbout(Model model, Search search) throws Exception {
		log.info("getAbout");
		return "about/about";
	}
	
	// retrieve kakao appkey information to display in location page
	@GetMapping("/getLocation")
	public String getLocation(Model model, Search search) throws Exception {
		log.info("getAbout");
		model.addAttribute("kakaoMapAppkey", kakaoMapAppkey);
		return "about/location";
	}	
}
