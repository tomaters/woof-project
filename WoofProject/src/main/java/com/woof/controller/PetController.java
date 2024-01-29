package com.woof.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.woof.domain.PageRequest;
import com.woof.domain.Pagination;
import com.woof.domain.Pet;
import com.woof.service.PetService;

import lombok.extern.java.Log;

@Log
@Controller
@RequestMapping("/pet")
public class PetController {

	@Autowired
	private PetService service;

	// image files storage path (gitignored)
	@Value("${upload.path}")
	private String uploadPath;

	// get specific pet information
	@GetMapping("/getPet")
	public String getPet(Pet pet, Model model) throws Exception {
		Pet pet_ = service.getPet(pet);
		model.addAttribute("pet", pet_);
		return "pet/pet";
	}

	// get specific pet information
	@PostMapping("/getPet")
	public void getPetList(Pet pet) throws Exception {
		log.info("/getPet POST");
		service.getPet(pet);
	}

	// get list of pets
	@GetMapping("/getPetList")
	public String getPetList(Pet pet, Model model, PageRequest pageRequest, Pagination pagination, @AuthenticationPrincipal UserDetails userDetails) throws Exception {
		if (null == pageRequest.getKeyword()) {
			pageRequest.setKeyword("");
		}
		pageRequest.setSizePerPage(9);
		pagination.setPageRequest(pageRequest);
		pagination.setTotalCount(service.countPetList(pageRequest));
		log.info("pagination : " + pagination.toString());
		model.addAttribute("pagination", pagination);
		List<Pet> petList = service.getPetList(pageRequest);
		model.addAttribute("petList", petList);
		return "pet/petList";
	}

	@GetMapping("/petList")
	public void getPetList(Model model, PageRequest pageRequest) throws Exception {
		log.info("/petList GET");
		List<Pet> petList = service.getPetList(pageRequest);
		model.addAttribute("petList", petList);
	}

	// ADMIN: insert pet (view)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/insertPet")
	public String insertPetForm(Pet pet) throws Exception {
		return "pet/insertPet";
	}

	// ADMIN: insert pet (view)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/pet/insertPet")
	public void insertPet(Model model) throws Exception {
		log.info("/pet/insertPet GET");
		model.addAttribute(new Pet());
	}

	// ADMIN: insert pet (business logic)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/insertPet")
	public String insertPet(Pet pet) throws Exception {
		log.info("/insertPet POST");
		List<MultipartFile> pictures = pet.getPictures();
		for (int i = 0; i < pictures.size(); i++) {
			MultipartFile file = pictures.get(i);
			String savedName = uploadFile(file.getOriginalFilename(), file.getBytes());
			if (i == 0) {
				pet.setPetMainPic(savedName);
			} else if (i == 1) {
				pet.setPetSubPic(savedName);
			}
		}
		service.insertPet(pet);

		return "redirect:/pet/getPetList";
	}

	// ADMIN: modify pet (view)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/modifyPet")
	public String modifyPet(Pet pet, Model model) throws Exception {
		log.info("/modifyPet GET");
		Pet petModify = this.service.getPet(pet);
		model.addAttribute(petModify);
		return "pet/modifyPet";
	}

	// ADMIN: modify pet (business logic)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/modifyPet")
	public String modify(Pet pet, Model model) throws Exception {
		log.info("/modifyPet POST: " + pet.toString());
		List<MultipartFile> pictures = pet.getPictures();
		for (int i = 0; i < pictures.size(); i++) {
			MultipartFile file = pictures.get(i);
			if (file != null && file.getSize() > 0) {
				String savedName = uploadFile(file.getOriginalFilename(), file.getBytes());
				if (i == 0) {
					pet.setPetMainPic(savedName);
				} else if (i == 1) {
					pet.setPetSubPic(savedName);
				}
			}
		}
		this.service.modifyPet(pet);
		model.addAttribute("Modified");
		return "redirect:/pet/getPetList";
	}

	// ADMIN: delete pet
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/deletePet")
	public String deletePet(Pet pet, Model model) throws Exception {
		this.service.deletePet(pet);
		return "redirect:/pet/getPetList";
	}

	// search by pet type
	@RequestMapping("/searchPetType")
	public void searchPetType(Pet pet) throws Exception {
		service.searchPetType(pet);
	}

	// rest of methods are for displaying pictures
	@ResponseBody
	@GetMapping("/getPetMainPic")
	public ResponseEntity<byte[]> getPetMainPic(Integer petNo) throws Exception {
		InputStream in = null;
		ResponseEntity<byte[]> entity = null;
		String fileName = service.getPetMainPic(petNo);
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

	@ResponseBody
	@GetMapping("/getPetSubPic")
	public ResponseEntity<byte[]> getPetSubPic(Integer petNo) throws Exception {
		InputStream in = null;
		ResponseEntity<byte[]> entity = null;
		String fileName = service.getPetSubPic(petNo);

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
	
	private String uploadFile(String originalName, byte[] fileData) throws Exception {
		UUID uid = UUID.randomUUID();
		String savedName = uid.toString() + "_" + originalName;
		File target = new File(uploadPath, savedName);
		FileCopyUtils.copy(fileData, target);
		return savedName;
	}
}
