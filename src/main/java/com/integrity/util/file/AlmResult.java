package com.integrity.util.file;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mks.api.response.APIException;
import com.mks.api.response.WorkItemIterator;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.integrity.util.file.StrKit.notBlank;

/**
 * 返回信息类
 * @author liuxiaoguang
 * @date 2021/4/26 11:25
 */

@Data
@Log4j2
public class AlmResult<T> {

    private int code;

    private boolean result;

    private String msg;

    private T data;

    public static final boolean  fail  = false;
    public static final boolean  success  = true;
    public static final String CREAT_SUCCESS  = "创建成功！";
    public static final String MODIFY_SUCCESS = "修改成功！";
    public static final String DELETE_SUCCESS = "删除成功！";
    public static final String UPLOAD_SUCCESS = "上传成功！";
    public static final String DOWNLOAD_SUCCESS = "下载成功！";
    public static final String OPERATE_SUCCESS = "操作成功！";
    public static final String COPY_SUCCESS = "复制成功！";
    public static final String PARM_ERROR = "参数异常！";

    public static final String DELETE_DOCUMENT_HASCONTENTS_ERROR = "MKS704283";

    public static <T> AlmResult<T> success(T data) {
        AlmResult<T> almResult = new AlmResult<>();
        almResult.setData(data);
        almResult.setResult(success);
        almResult.setCode(HttpStatus.OK.value());
        return almResult;
    }

    public static <T> AlmResult<T> success(T data, String msg) {
        AlmResult<T> almResult = new AlmResult<>();
        almResult.setData(data);
        almResult.setResult(success);
        almResult.setMsg(msg);
        almResult.setCode(HttpStatus.OK.value());
        return almResult;
    }

    public static <T> AlmResult<T> success(String msg) {
        AlmResult<T> almResult = new AlmResult<>();
        almResult.setCode(HttpStatus.OK.value());
        almResult.setResult(success);
        almResult.setMsg(msg);
        return almResult;
    }

    public static <T> AlmResult<T> fail(String msg) {
        AlmResult<T> almResult = new AlmResult<>();
        almResult.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        almResult.setResult(fail);
        almResult.setMsg(msg);
        return almResult;
    }

    public static <T> AlmResult<T> fail(Exception e) {
        AlmResult<T> almResult = new AlmResult<>();
        almResult.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        almResult.setResult(fail);
        String message = e.getMessage();
        if(null == message || "".equals(message)){
            message = "未知错误";
        }else if(message.contains(DELETE_DOCUMENT_HASCONTENTS_ERROR)){
            message = "请先删除文档条目";
        }
        almResult.setMsg(message);
        return almResult;
    }


    /**
     * @description:  返回列表参数封装
     * @param: [list, title]
     * @return: com.alibaba.fastjson.JSONObject
     * @author liuxiaoguang
     * @date: 2021/7/19 10:07
     */
    public static JSONObject dateProcess(List<Map<String, String>> list, JSONObject json){

        JSONObject jsonObject = new JSONObject();
        //表格字段详情
        JSONArray columns = new JSONArray();
        //列表字段
        JSONArray dataTable = new JSONArray();

        for (Map<String, String> map : list) {
            dataTable.add(map);
        }
        if(json == null ){
            return jsonObject;
        }

        JSONArray languages = json.getJSONArray("language");
        for (int i =0 ;i<languages.size();i++){
            JSONObject column = new JSONObject();
            JSONObject languageJson = languages.getJSONObject(i);
            column.put("title",languageJson.get("zh"));
            column.put("dataIndex",languageJson.get("key"));
            column.put("key", languageJson.get("en"));
            columns.add(column);
        }
        jsonObject.put("columns",columns);
        jsonObject.put("dataTable",dataTable);
        return jsonObject;
    }


    //临时，动态列表全部改完去掉
    public static JSONObject dateProcess(List<Map<String, String>> list, String[] title){

        JSONObject jsonObject = new JSONObject();
        //表格字段详情
        JSONArray columns = new JSONArray();
        //列表字段
        JSONArray dataTable = new JSONArray();

        for (Map<String, String> map : list) {
            dataTable.add(map);
        }
        for (int i =0 ;i<title.length;i++){
            JSONObject column = new JSONObject();
            column.put("title",title[i]);
            column.put("dataIndex",title[i]);
            column.put("key", title[i]);
            columns.add(column);
        }
        jsonObject.put("columns",columns);
        jsonObject.put("dataTable",dataTable);
        return jsonObject;
    }


    /**
     * description:  获取报错信息
     * param: [e-报错返回信息, className-报错的方法]
     * return: java.lang.String
     * author liuxiaoguang
     * date: 2021/7/23 8:26
     */
    public static String getEroMsg(APIException e,String className){
        String errorMsg = "";
        if(notBlank(e.getMessage())){
            log.info(className,e.getMessage());
            errorMsg =  e.getMessage();
        }else {
            WorkItemIterator iterator = e.getResponse().getWorkItems();
            while (iterator.hasNext()) {
                try {
                    iterator.next();
                } catch (APIException apiException) {
                    log.error(className ,apiException.getMessage());
                    errorMsg =   apiException.getMessage();
                }
            }
        }
        //用户不存在
        if(StringUtils.isBlank(errorMsg)){
            return errorMsg;
        }
        if(errorMsg.indexOf("MKS6411834: User")>-1){
            List<String> erroeList = StrKit.getStringQuotationMark(errorMsg);
            throw new AlmResultException(506,erroeList.get(1)+":用户在ALM系统中没有，请联系管理员创建！",errorMsg);
        }
        //测试任务为New时不允许修改测试结果
        if(errorMsg.indexOf("Type Test Session does not allow modification of test results in state New") > -1){
            throw new AlmResultException(506,"测试任务为New时不允许修改测试结果！",errorMsg);
        }
        //如果不能作废需要在integrity中配置流程 New-Invalid
        if(errorMsg.indexOf("MKS124252: Field \"State\": Value \"Invalid\" is not a valid state") > -1){
            throw new AlmResultException(506,"此节点不允许作废,如需作废请联系管理员！",errorMsg);
        }
        //项目中没有该迭代计划
        if(errorMsg.indexOf("Iteration plan in project")!=-1){
            //原msg：   Iteration plan in project </三一重起/cxa> : <默认迭代计划111> does not exist！
            String projectPath = errorMsg.substring(errorMsg.indexOf("<")+1, errorMsg.indexOf(">"));
            String second = errorMsg.split(":")[1];
            String reqName = second.substring(second.indexOf("<")+1, second.indexOf(">"));
            throw new AlmResultException(506,projectPath+" 中不存在该迭代计划："+reqName,errorMsg);
        }
        //项目不存在
         if(errorMsg.indexOf("MKS124814: Cannot show view information: Error parsing filter \"field[Project]")!=-1){
            //原msg：MKS124814: Cannot show view information: Error parsing filter "field[Project] = 12312312123" : The value "12312312123" is not valid
            String msg = errorMsg.split("=")[1];
            throw new AlmResultException(506,"项目："+msg.substring(0,msg.indexOf("\""))+" 不存在！",errorMsg);
        }
         //标签不存在
        if(errorMsg.indexOf("Tag not found in project path")!=-1){
            //原msg： Tag not found in project path </三一重起/cxa> : <asdasd>
            String projectPath = errorMsg.substring(errorMsg.indexOf("<")+1, errorMsg.indexOf(">"));
            String second = errorMsg.split(":")[1];
            String tagName = second.substring(second.indexOf("<")+1, second.indexOf(">"));
            throw new AlmResultException(506,"项目："+projectPath+" 中不存在该标签： "+tagName,errorMsg);
        }
        //项目路径已被使用，请修改项目路径
        if(errorMsg.indexOf("There can only be one item backing a project") > -1){
            throw new AlmResultException(511,"此项目路径已被使用，请修改项目路径！",errorMsg);
        }
        return errorMsg;
    }


    /**
     * description: 获取请求头数
     * author liuxiaoguang
     * date 2021/10/11 16:33
     * version 1.0
     */
    public static String getHeadersInfo(HttpServletRequest request,String str) {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key.toLowerCase(), value);
        }
        return map.get(str.toLowerCase());
    }
}
