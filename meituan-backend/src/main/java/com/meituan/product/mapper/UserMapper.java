package com.meituan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meituan.product.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
