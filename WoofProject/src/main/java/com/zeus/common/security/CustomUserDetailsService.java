package com.zeus.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.woof.common.domain.CustomAccount;
import com.woof.domain.Account;
import com.woof.mapper.AccountMapper;

import lombok.extern.java.Log;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private AccountMapper accountMapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Account account = accountMapper.readByUsername(username);
		// check if retrieved account is null. if not null, create custom account; else return null
		return account == null ? null : new CustomAccount(account);
	}
}