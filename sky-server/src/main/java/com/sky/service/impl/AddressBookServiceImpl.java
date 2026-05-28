package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;

    public AddressBookServiceImpl(AddressBookMapper addressBookMapper) {
        this.addressBookMapper = addressBookMapper;
    }

    @Override
    public void save(AddressBook addressBook) {
        //1. 设置当前用户id
        addressBook.setUserId(BaseContext.getCurrentId());

        //2. 新增地址默认不是默认地址
        addressBook.setIsDefault(0);

        //3. 插入数据库
        addressBookMapper.insert(addressBook);
    }

    @Override
    public List<AddressBook> list() {
        AddressBook addressBook = AddressBook.builder()
                .userId(BaseContext.getCurrentId())
                .build();

        return addressBookMapper.list(addressBook);
    }

    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        //1. 将当前用户的所有地址设置为非默认
        AddressBook param = AddressBook.builder()
                .userId(BaseContext.getCurrentId())
                .isDefault(0)
                .build();
        addressBookMapper.updateIsDefaultByUserId(param);

        //2. 将当前地址设置为默认
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    @Override
    public AddressBook getDefault() {
        AddressBook addressBook = AddressBook.builder()
                .userId(BaseContext.getCurrentId())
                .isDefault(1)
                .build();

        List<AddressBook> list = addressBookMapper.list(addressBook);

        if (list != null && list.size() == 1) {
            return list.get(0);
        }

        return null;
    }

    @Override
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}