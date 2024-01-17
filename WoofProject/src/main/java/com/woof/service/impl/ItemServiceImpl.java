package com.woof.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.woof.domain.Item;
import com.woof.mapper.ItemMapper;
import com.woof.service.ItemService;

@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	private ItemMapper mapper;

	@Override
	public Item getItem(int itemNo) throws Exception {
		return mapper.getItem(itemNo);
	}

	@Override
	public List<Item> getItemList() throws Exception {
		return mapper.getItemList();
	}

	@Override
	public void insertItem(Item item) throws Exception {
		mapper.insertItem(item);
	}

	@Override
	public void modifyItem(Item item) throws Exception {
		mapper.modifyItem(item);
	}

	@Override
	public void toggleItemStatus(int itemNo) throws Exception {
		mapper.toggleItemStatus(itemNo);
	}

	@Override
	public String getItemMainPic(Integer itemId) throws Exception {
		return mapper.getItemMainPic(itemId);
	}

	@Override
	public String getItemSubPic(Integer itemId) throws Exception {
		return mapper.getItemSubPic(itemId);
	}
	
	@Override
	public List<Item> searchItemType(Item item) throws Exception {
		return mapper.searchItemType(item);
	}
	
	@Override
	public List<Item> searchItemKeyword(Item item) throws Exception {
		return mapper.searchItemKeyword(item);
	}

}
