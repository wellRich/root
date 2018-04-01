package com.digital.dao;

import com.digital.dao.sqlMapper.ZCCGroupSql;
import org.apache.ibatis.annotations.*;
import com.digital.entity.ZCCGroup;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 区划变更对照组映射
 * @author guoyka
 * @version 2018/3/16
 * @see ZCCGroup
 */
@Mapper
public interface ZCCGroupMapper {
    @DeleteProvider(type = ZCCGroupSql.class, method = "delete")
    int delete(Object object);

    @UpdateProvider(type = ZCCGroupSql.class, method = "update")
    int save(ZCCGroup group);

    @InsertProvider(type = ZCCGroupSql.class, method = "insert")
    @SelectKey(before = false, resultType = Integer.class, keyColumn = "GROUPXH", keyProperty = "seq", statement = "SELECT LAST_INSERT_ID()")
    @Options(useGeneratedKeys = true)
    int insert(Object group);

    //根据申请单查找变更对照组
    @Select("SELECT GROUPXH, GROUPMC, SQDXH FROM XZQH_BGGROUP WHERE SQDXH=#{requestSeq}")
    @Results({
            @Result(id = true, property = "seq", column = "GROUPXH"),
            @Result(property = "name", column = "GROUPMC"),
            @Result(property = "requestSeq", column = "SQDXH")
    })
    List<ZCCGroup> findByRequestSeq(@Param("requestSeq") Integer requestSeq);

    @SelectProvider(type = ZCCGroupSql.class, method = "get")
    @Results(id="get", value = {
            @Result(id = true, property = "seq", column = "GROUPXH"),
            @Result(property = "name", column = "GROUPMC"),
            @Result(property = "serialNumber", column = "BH"),
            @Result(property = "creatorCode", column = "LRR_DM"),
            @Result(property = "createDate", column = "LRSJ"),
            @Result(property = "creatorDeptCode", column = "LRJG_DM"),
            @Result(property = "requestSeq", column = "SQDXH"),

    })
    ZCCGroup get(Object key);


    /**
     *  根据区划代码查找该区划相关的变更组中最大的编号
     * @param zoingCode 区划代码
     * @return
     */
    @SelectProvider(type = ZCCGroupSql.class, method = "getMaxSerialNumber")
    @ResultType(value = BigInteger.class)
    BigInteger getMaxSerialNumber(String zoingCode);


    /**
     * 获取最大的排序号
     * @param tableName 表名
     * @return 最大值
     */
    @ResultType(value = Long.class)
    @SelectProvider(type = ZCCGroupSql.class, method = "getMaxOrderNum")
    //@Select("SELECT TABLE_MAX  FROM XZQH_MAX WHERE TABLE_NAME=#{tableName}")
    Long getMaxOrderNum(@Param(value = "tableName") String tableName);

    /**
     * 更新最大值
     * @param tableName 表名
     * @return 被修改的数据条数
     */
    //@Update("UPDATE XZQH_MAX SET TABLE_MAX=TABLE_MAX+1 WHERE TABLE_NAME=#{tableName}")
    @UpdateProvider(type = ZCCGroupSql.class, method = "updateMaxOrderNum")
    int updateMaxOrderNum(@Param(value = "tableName") String tableName);

    /**
     * 初始化最大值
     * @param params
     * @return
     */
    @Update("INSERT INTO XZQH_MAX(TABLE_NAME, TABLE_NOTE, TABLE_MAX) VALUES(#{tableName}, #{tableNote}, #{tableMax})")
    int insertMaxOrderNum(Map<String, Object> params);
}
