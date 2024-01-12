package com.woof.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.woof.domain.Account;
import com.woof.domain.AccountAuth;
import com.woof.mapper.AccountMapper;
import com.woof.service.AccountService;

import lombok.extern.java.Log;

@Log
@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	private AccountMapper mapper;
	
	//내정보
	@Override
	public Account getAccount(Account username) throws Exception {
		
		return mapper.getAccount(username);
	}

	//Account들의 모든정보 리스트
	@Override
	public List<Account> getAccountList() throws Exception {
		// TODO Auto-generated method stub
		return mapper.getAccountList();
	}

	//계정 등록 처리
	@Transactional
	@Override
	public void registerAccount(Account account) throws Exception {
		mapper.registerAccount(account);
		// 회원 권한 생성
		AccountAuth accountAuth = new AccountAuth();
		accountAuth.setUsername(account.getUsername());
		accountAuth.setAuth("ROLE_MEMBER");
		log.info(accountAuth.toString());
		mapper.registerAccountAuth(accountAuth);

		
	}

	//내정보 수정
	@Override
	public void modifyAccount(Account account) throws Exception {
		
		mapper.modifyAccount(account);
	}

	//계정 탈퇴
	@Override
	public void deleteAccount(Account username) throws Exception {
		mapper.deleteAccount(username);
		
	}

	//admin이 개인계정을 찾기
	@Override
	public Account searcheAccount(Account searchKeyword) throws Exception {
		return mapper.searcheAccount(searchKeyword);
	}
	
	// 최초 관리자를 생성한다.
	@Transactional
	@Override
	public void setupAdmin(Account account) throws Exception {
		mapper.registerAccount(account);
		AccountAuth accountAuth = new AccountAuth();
		accountAuth.setUsername(account.getUsername()); 
		accountAuth.setAuth("ROLE_ADMIN");
		mapper.registerAccountAuth(accountAuth);
	}

	@Override
	public int countAll() throws Exception {
		return mapper.countAll();
	}

}
