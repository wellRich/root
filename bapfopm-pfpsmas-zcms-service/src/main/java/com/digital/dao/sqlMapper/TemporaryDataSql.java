package com.digital.dao.sqlMapper;

import com.digital.util.Common;
import com.digital.util.StringUtil;
import org.apache.ibatis.jdbc.SQL;

/**
 * @Description: TODO 逻辑校验的sql   方法名与相应的mapper中方法名相同
 * @Author: zhanghpj
 * @Version 1.0, 16:31 2018/3/12
 * @See
 * @Since
 * @Deprecated
 */
public class TemporaryDataSql {


    public String queryChangeData(Integer fileNum,String zoningCode,String changeType){
        return new SQL(){
            {
                SELECT("DZBXH","YSXZQH_DM","YSXZQH_MC","BGLX_DM","MBXZQH_DM","MBXZQH_MC","BZ","LRR_DM","BH","GROUPMC","LRJG_DM","PXH");
                FROM("XZQH_JZBGDZB_TEMP");
                WHERE("ZIPXH="+fileNum);
                if (Common.ADD.equals(changeType)){
                    AND();
                    WHERE("BGLX_DM="+ Common.ADD);
                    if (!StringUtil.isEmpty(zoningCode)){
                        AND();
                        WHERE("MBXZQH_DM like '"+ Common.getSuperiorZoningCode(zoningCode)+"'%");
                    }
                }
            }
        }.toString();
    }

    public String updateChangeData(Integer tableNum,String errStatus,String errMsg){
        return new SQL(){
            {
                UPDATE("xzqh_jzbgdzb_temp");

                WHERE("DZBXH="+tableNum);

            }
        }.toString();
    }
}
