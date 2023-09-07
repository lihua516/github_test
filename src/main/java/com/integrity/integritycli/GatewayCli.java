package com.integrity.integritycli;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.integrity.util.file.FileIo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Log4j2
public class GatewayCli {

    @Value("${integrity.hostname}")
    private String integrity_hostname;

    @Value("${integrity.port}")
    private Integer integrity_port;

    @Value("${integrity.user}")
    private String user;

    @Value("${integrity.password}")
    private String password;

    @Value("${gwm.env}")
    private String env;
    @Value("${gwm.appKey}")
    private String appKey;
    @Value("${gwm.appSecret}")
    private String appSecret;

    // TODO: gateway 需不需要替换成redis里面的username、password
    /**
     * 导出Word
     *
     * @return
     * @throws
     */
    public File exportWord( String config, String docid) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = docid + "-" + sdf.format(new Date())+System.currentTimeMillis() + ".docx";
        String tempExcelPath = FileIo.getSystemRoot();
        File tempPath = new File(tempExcelPath);
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
        String tempfile = tempExcelPath +  fileName;
        String comand = "Gateway export --hostname=" + integrity_hostname +
                " --port=" + integrity_port +
                " --user=" + user +
                " --password=" + password +
                " --config=\"" + config+ "\""+
                " --file=" + tempfile+ " --silent " + docid;
        log.info("gateway 导出命令************--->{}",comand);
        Process p = Runtime.getRuntime().exec(comand);
        /*String line = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader brError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = br.readLine()) != null || (line = brError.readLine()) != null) {
            //输出exe输出的信息以及错误信息
            log.info(line);
        }
        br.close();
        brError.close();*/
        boolean flag = true;
        File file = null;
        int i=0;
        while (flag) {
            file = new File(tempfile);
            if(file.exists() && file.length()>=15000){
                flag = false;
            }
            if(i>8){
                log.error("File export failure-------------------");
                flag = false;
            }
            Thread.currentThread().sleep(5000);
            i++;
            log.info("File is being exported, please wait-------------------");
        }
        log.info("The file has been exported.---->{},The file size---->{}",file.getAbsolutePath(),file.length());
        byte[] fileData = EncryptUtiles.fileEncryptByte(file, env, appKey, appSecret);
        log.info("Encrypted file size-------->{}",fileData.length);
        return  getFile(fileData, file);
       //return new File(tempfile);
    }


    public String importWord(String config, String project, String title, String filePath) {
        //TODO 先传文件

        Map<String, String> fields = new HashMap<>();
        fields.put("Title", title);
        fields.put("Project", project);
        fields.put("Category", "Requirement");
        String strFields = fields.entrySet().stream()
                .map(item -> "\"" + item.getKey() + "\"=\"" + item.getValue() + "\"")
                .collect(Collectors.joining(";"));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Gateway import --user=");
        stringBuilder.append(user);
        stringBuilder.append(" --password=");
        stringBuilder.append(password);
        stringBuilder.append(" --hostname=");
        stringBuilder.append(integrity_hostname);
        stringBuilder.append(" --port=");
        stringBuilder.append(integrity_port);
        stringBuilder.append(" --config=\"");
        stringBuilder.append(config);
        stringBuilder.append("\" --file=\"");
        stringBuilder.append(filePath);
        stringBuilder.append("\"");
        stringBuilder.append(" --Fields=");
        stringBuilder.append(strFields);
        stringBuilder.append(" --silent");
        String str = stringBuilder.toString();
        //TODO 连接远程执行命令
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        log.info("******************************---->"+str);
        return str;
    }


    public File fileEncrypt(MultipartFile file) {
        File files = FileIo.approvalFile(file);
        byte[] fileData = EncryptUtiles.fileEncryptByte(files, env, appKey, appSecret);
        return  getFile(fileData, files);
    }

    private File getFile(byte[] fileData,File files){
        File fileEncrypt = null;
        OutputStream out = null;
        InputStream is = null;
        try {
            String format = DateUtil.format(new Date(), "yyyyMMddHHmmss");
            String fileName =  format+files.getName();
            String tempExcelPath = FileIo.getSystemRoot();
            fileEncrypt = new File(tempExcelPath+fileName);
            out = new FileOutputStream(fileEncrypt);
            is = new ByteArrayInputStream(fileData);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = is.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
        } catch (Exception e) {
            log.error("文件读取异常--------->"+e.getMessage());
        }finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileEncrypt;
    }

    public File fileDecrypt(MultipartFile file) {
        File files = FileIo.approvalFile(file);
        byte[] fileData = EncryptUtiles.fileDecryptByte(files, env, appKey, appSecret);
        if(null!=fileData && fileData.length>0){
            return  getFile(fileData, files);
        }
        return files;
    }



}
