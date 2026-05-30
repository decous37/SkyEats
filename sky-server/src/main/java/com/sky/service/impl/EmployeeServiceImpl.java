package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.*;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import com.sky.constant.PasswordConstant;
import com.sky.context.BaseContext;
import org.springframework.beans.BeanUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;


import java.time.LocalDateTime;


@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 新增员工
     *
     * @param employeeDTO
     *
     *
     */
    public void save(EmployeeDTO employeeDTO) {
        //1.创建Employee实体对象
        Employee employee = new Employee();


        //2.复制EmployeeDTO中的属性
        BeanUtils.copyProperties(employeeDTO, employee);

        //3.设置默认密码
        employee.setPassword(PasswordConstant.DEFAULT_PASSWORD);

        //4.设置默认状态
        employee.setStatus(StatusConstant.ENABLE);

        //5.设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        employee.setCreateTime(now);
        employee.setUpdateTime(now);

        //6.设置创建人和修改人
        long currentId = BaseContext.getCurrentId();
        employee.setCreateUser(currentId);
        employee.setUpdateUser(currentId);
        //7.调用Mapper插入数据库
        employeeMapper.insert(employee);
    }

    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        //1. 获取当前登录员工id
        Long empId = BaseContext.getCurrentId();

        //2. 根据员工id查询员工
        Employee employee = employeeMapper.getById(empId);

        //3. 判断员工是否存在
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //4. 校验旧密码是否正确
        String oldPassword = passwordEditDTO.getOldPassword();
        if (!oldPassword.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //5. 设置新密码
        Employee updateEmployee = Employee.builder()
                .id(empId)
                .password(passwordEditDTO.getNewPassword())
                .build();

        //6. 更新数据库
        employeeMapper.update(updateEmployee);
    }

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对：前端传来的是明文密码，数据库中保存的是 MD5 摘要
//        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //1.开始分页
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        //2.查询数据
        Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);
        //3.封装返回结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 启用或禁用员工账号
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void  update(EmployeeDTO employeeDTO) {
        //1. 创建 Employee 实体对象
        Employee employee = new Employee();
        //2. 把 EmployeeDTO 复制到 Employee
        BeanUtils.copyProperties(employeeDTO, employee);
        //3. 设置 updateTime
        employee.setUpdateTime(LocalDateTime.now());
        //4. 设置 updateUser
        employee.setUpdateUser(BaseContext.getCurrentId());
        //5. 调用 employeeMapper.update(employee)
        employeeMapper.update(employee);


    }

}
