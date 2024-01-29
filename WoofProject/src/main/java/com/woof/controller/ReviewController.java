package com.woof.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.woof.domain.Account;
import com.woof.domain.PageRequest;
import com.woof.domain.Pagination;
import com.woof.domain.Pet;
import com.woof.domain.Reply;
import com.woof.domain.Review;
import com.woof.service.ReviewService;

import lombok.extern.java.Log;

@Log
@Controller
@RequestMapping("/review")
public class ReviewController {

	// for pagination; set number of reviews in a page
	private final int SIZE_PER_PAGE = 8;
	
	@Autowired
	private ReviewService service;

	@Value("${upload.path}")
	private String uploadPath;

	// get specific review information (view)
	@GetMapping("/getReview")
	public String getReview(Review review, Model model, Principal principal, Account account) throws Exception {
		if (null != principal) {
			account.setUsername(principal.getName());
			model.addAttribute(account);
		}
		Review review_ = service.getReview(review);
		model.addAttribute("review", review_);
		return "pet/petReview";
	}
	// get specific review information (business logic)

	@PostMapping("/getReview")
	public void getReview(Review review, Model model) throws Exception {
		service.getReview(review);
	}
	
	// get list of reviews
	@GetMapping("/getReviewList")
	public String getReviewList(Model model,PageRequest pageRequest,Pagination pagination) throws Exception{
		log.info("getReviewList");
		
		pageRequest.setSizePerPage(SIZE_PER_PAGE);
		if(pageRequest.getCondition() == null) {
			pageRequest.setCondition("TITLE");
		}
		if(pageRequest.getKeyword() == null) {
			pageRequest.setKeyword("");
		}
		
		switch (pageRequest.getCondition()) {
		case "TITLE":{
			pageRequest.setKeywordTitle(pageRequest.getKeyword());
			pageRequest.setKeywordDesc("");
			break;
		}
		case "CONTENT":{
			pageRequest.setKeywordDesc(pageRequest.getKeyword());
			pageRequest.setKeywordTitle("");
			break;
		}
		}
		pageRequest.setSizePerPage(SIZE_PER_PAGE);
		log.info(pageRequest.toString());
		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(service.countReviewList(pageRequest));
		model.addAttribute("pagination", pagination);
		List<Review> reviewList = service.getReviewList(pageRequest);
		model.addAttribute("reviewList", reviewList);
		
		return "pet/petReviewList";
	}
	 
	// MEMBER: write a review (view)
	@PreAuthorize("hasAnyRole('ROLE_MEMBER')")
	@GetMapping("/insertReviewForm")
	public String ReviewForm(Review review, Model model, Principal principal) throws Exception {
		review.setUsername(principal.getName());
		return "pet/insertPetReview";
	}

	@PreAuthorize("hasAnyRole('ROLE_MEMBER')")
	@GetMapping("/pet/insertReview")
	public void insertReview(Model model) throws Exception {
		log.info("/pet/insertPetReview GET");
		model.addAttribute(new Review());
	}

	// MEMBER: write a review (business logic)
	@PreAuthorize("hasAnyRole('ROLE_MEMBER')")
	@PostMapping("/insertReview")
	public String insertReview(Review review) throws Exception {
		log.info("/insertReview POST");
		log.info("/insertReview POST : "+review.toString());
		if(null==review.getItemName()) {
			review.setItemName("");
		}
		List<MultipartFile> pictures = review.getPictures();
		for (int i = 0; i < pictures.size(); i++) {
			MultipartFile file = pictures.get(i);
			String savedName = uploadFile(file.getOriginalFilename(), file.getBytes());
			if (i == 0) {
				review.setReviewPic(savedName);
			}
		}
		service.insertReview(review);
		if(review.getItemNo()>0) {
			 return "redirect:/orderHistory/getOrderHistoryList";
		}
		
		return "redirect:/review/getReviewList";
	}


	// modify a review (only for members or admin; view)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
	@GetMapping("/modifyReview")
	public String modifyReivew(Review reivew, Model model) throws Exception {
		log.info("/modifyPetReview GET");
		Review reviewModify = this.service.getReview(reivew);
		model.addAttribute(reviewModify);
		return "pet/modifyPetReview";
	}
	
	// modify a review (only for members or admin; business logic)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
	@PostMapping("/modifyReview")
	public String modify(Review review,Model model)throws Exception{
		log.info("/modifyReview POST");
		List<MultipartFile> pictures = review.getPictures();
		for(int i = 0; i < pictures.size(); i++) {
			MultipartFile file = pictures.get(i);
			if(file != null && file.getSize() > 0) {
				String savedName = uploadFile(file.getOriginalFilename(), file.getBytes());
				if(i == 0) {
					review.setReviewPic(savedName);
				}
			}
		}
		this.service.modifyReview(review);
		model.addAttribute("Modified");
		return "redirect:/review/getReviewList";
	}
	
	// delete a review (only for members or admin)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
	@GetMapping(value = "/deleteReview")
	public String deleteReview(Review review,Model model) throws Exception{
		this.service.deleteReview(review);
		return "redirect:/review/getReviewList";
	}

	// get item review list
	@PutMapping("/getItemReviewList")
	public ResponseEntity<List> getItemReviewList(@RequestBody PageRequest pageRequest, Pagination pagination)
			throws Exception {
		log.info("getItemReviewList");

		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(service.countItemReviewList(pageRequest));

		List<Review> reviewList = service.getItemReviewList(pageRequest);
		ResponseEntity<List> entity = null;
		if (reviewList.size() != 0) {
			entity = new ResponseEntity<List>(reviewList, HttpStatus.OK);
			log.info("reviewList : " + reviewList.toString());
		}
		return entity;
	}
	
	@PutMapping("/getItemReviewPagination")
	public ResponseEntity<List> getItemReviewPagination(@RequestBody PageRequest pageRequest, Pagination pagination)
			throws Exception {
		log.info("getItemReviewPagination");
		log.info("pageRequest1 : " + pageRequest.toString());

		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(service.countItemReviewList(pageRequest));
		log.info("pagination3 : " + pagination.toString());
		log.info("pageRequest3 : " + pageRequest.toString());
		List<Pagination> pageList = new ArrayList<Pagination>();
		pageList.add(pagination);

		ResponseEntity<List> entity = new ResponseEntity<List>(pageList, HttpStatus.OK);
		return entity;
	}
	
	// delete review (only for admin or members; AJAX)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
	@PutMapping("/deleteReviewAjax")
	public ResponseEntity<List> deleteReviewAjax(@RequestBody Review review) throws Exception {
		service.deleteReview(review);
		ResponseEntity<List> entity = null;
		return entity;
	}
	

	// MEMBER - insert an item review (view)
	@PostMapping("/insertItemReviewForm")
	public String insertItemReviewForm(Model model, Principal principal, Account account,Review review) throws Exception {
		log.info("insertItemReviewForm");
		log.info("review : " +review.toString());
		if (null != principal) {
			account.setUsername(principal.getName());
			log.info(principal.getName());
			model.addAttribute(account);
			model.addAttribute(review);
		}
		return "item/insertItemReview";
	}
	
	// MEMBER - insert an item review (business logic)
	@PostMapping("/insertItemReview")
	public String insertItemReview() {
		log.info("insertItemReview");
		return "redirect:/reply/getItemReviewList";
	}
	
	// remaining methods for uploading and retrieving images
	private String uploadFile(String originalName, byte[] fileData) throws Exception {
		log.info("UploadFile()");
		UUID uid = UUID.randomUUID();
		String savedName = uid.toString() + "_" + originalName;
		File target = new File(uploadPath, savedName);
		FileCopyUtils.copy(fileData, target);
		return savedName;
	}
	
	@ResponseBody
	@GetMapping("/getReviewPic")
	public ResponseEntity<byte[]> getReviewPic(Integer reviewNo) throws Exception {
		InputStream in = null;
		ResponseEntity<byte[]> entity = null;
		String fileName = service.getReviewPic(reviewNo);
		try {
			String formatName = fileName.substring(fileName.lastIndexOf(".") + 1);
			MediaType mType = getMediaType(formatName);
			HttpHeaders headers = new HttpHeaders();
			in = new FileInputStream(uploadPath + File.separator + fileName);
			if (mType != null) {
				headers.setContentType(mType);
			}
			entity = new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			entity = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
		} finally {
			in.close();
		}
		return entity;
	}
	
	private MediaType getMediaType(String formatName) {
		if (formatName != null) {
			if (formatName.equals("JPG")) {
				return MediaType.IMAGE_JPEG;
			}
			if (formatName.equals("GIF")) {
				return MediaType.IMAGE_GIF;
			}
			if (formatName.equals("PNG")) {
				return MediaType.IMAGE_PNG;
			}
		}
		return null;
	}
}

