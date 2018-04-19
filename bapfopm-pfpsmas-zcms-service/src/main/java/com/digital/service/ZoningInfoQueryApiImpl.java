package com.digital.service;

import com.digital.api.ZoningInfoQueryApi;
import com.digital.dao.FormalTableMapper;
import com.digital.dao.ZCCDetailMapper;
import com.digital.entity.FormalTableInfo;
import com.digital.entity.ZCCDetail;
import com.digital.util.Common;
import com.digital.util.StringUtil;
import com.digital.util.search.QueryFilter;
import com.digital.util.search.QueryReq;
import com.digital.util.search.QueryResp;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.ImmutableMap;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author guoyka
 * @version 1.0, 2018/4/13
 */
@Service
@Transactional
public class ZoningInfoQueryApiImpl implements ZoningInfoQueryApi{

    protected static final org.slf4j.Logger log = LoggerFactory.getLogger(ZoningInfoQueryApiImpl.class);

    @Autowired
    FormalTableMapper formalTableMapper;

    @Autowired
    ZCCDetailMapper zccDetailMapper;

    @Override
    public Map<String, List<FormalTableInfo>> findSubordinateZoning(String zoningCode) {

        //获取级次代码
        String assigningCode = Common.getAssigningCode(zoningCode);

        List<FormalTableInfo> formalTableInfos = formalTableMapper.findSubordinateZoning(zoningCode);

        return ImmutableMap.of(assigningCode, formalTableInfos);
    }

    /**
     * 获取祖上、自身、子级区划数据
     * @param zoningCode 登录用户所属的区划代码
     * @return  [级次代码：所有的子级区划]
     */
    @Override
    public Map<String, List<FormalTableInfo>> findAncestorsAndSubsByZoningCode(String zoningCode) {
        Integer assigningCode = Integer.valueOf(Common.getAssigningCode(zoningCode));

        //去0后的区划代码
        String code = Common.removeZeroOfTailing(zoningCode);

        //祖系区划代码
        List<String> ancestralCodes = Common.getPaternalCodes(zoningCode);
        String sql;

        //组以上级别的的区划
        String processedCodes = StringUtil.insertSingleQuotationMarks(ancestralCodes);
        QueryReq req = new QueryReq();
        if(assigningCode < Common.GROUP_LEVEL){
            req.addFilter("zoningCode", code + "%", QueryFilter.OPR_LIKE);
            req.addFilter("assigningCode", assigningCode + 1 );
            req.addFilter("zoningCode", processedCodes, QueryFilter.OPR_IN, QueryFilter.LOGIC_OR);
        }else {
            req.addFilter("zoningCode", processedCodes, QueryFilter.OPR_IN);
        }
        Map<String, List<FormalTableInfo>> result = new HashMap<>();
        for(FormalTableInfo info: formalTableMapper.seek(req)){
            if(result.containsKey(info.getAccessCode())){
                result.get(info.getAssigningCode()).add(info);
            }else {
                List cell = new ArrayList();
                cell.add(info);
                result.put(info.getAssigningCode(), cell);
            }
        }
        return result;
    }


    /**
     * 变更对照查询
     * @param params 查询条件 [字段/属性：值]
     * @return
     */
    @Override
    public QueryResp<ZCCDetail> search(Map<String, Object> params, Integer pageSize, Integer pageIndex, Integer total) {
        QueryResp<ZCCDetail> resp = new QueryResp<>(pageIndex, pageSize);
        QueryReq queryReq = new QueryReq();
        List<QueryFilter> filters = new ArrayList<>();
        params.forEach((k, v) -> {
            if(k.equals("changeDateStart")){
                filters.add(new QueryFilter("creatorDate", v, QueryFilter.OPR_MORE_THAN));
            }else if(k.equals("changeDateEnd")){
                filters.add(new QueryFilter("creatorDate", v, QueryFilter.OPR_LESS_THAN));
            }else if(k.equals("changeType")){
                filters.add(new QueryFilter("changeType", v));
            }else {
                filters.add(new QueryFilter(k, v + "%", QueryFilter.OPR_LIKE));
            }
        });
        queryReq.search = filters;
        resp.query(() -> zccDetailMapper.seek(queryReq));
        if(total == 0){
            QueryReq req = new QueryReq("count(*)");
            req.search = filters;
            resp.count(() -> zccDetailMapper.countBy(filters));
        }else {
            resp.setTotalRecord(total);
        }
        return resp;
    }


    public Page pageHelperTest(String groupSeq, int pageNum, int pageSize, int total) {
        Page<ZCCDetail> page = PageHelper.startPage(pageNum, pageSize);
        //List<ZCCDetail> zccDetails = PageHelper.startPage(pageNum, pageSize);
        List<QueryFilter> filters = new ArrayList<QueryFilter>() {{
            add(new QueryFilter("groupSeq", groupSeq));
        }};
        if (total == 0) {
            page.setTotal(zccDetailMapper.countByGroups(groupSeq));
        } else {
            page.setTotal(total);
        }
        page.doSelectPage(() -> zccDetailMapper.seek(new QueryReq() {{
            search = filters;
        }}));

        page.forEach(e -> System.out.println("e----------> " + e.getCurrentZoningCode()));
        return page;
    }


}