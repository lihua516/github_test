package com.integrity.api.controller.v2;

import com.integrity.integritycli.GatewayCli;
import com.integrity.util.file.AlmResult;
import com.integrity.util.file.FileIo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@CrossOrigin
@RestController
@RequestMapping("/v2/gateway")
@Api(value = "Gateway操作类", tags = {"Gateway操作类"})
@Log4j2
public class GatewayController {

    @Autowired
    private GatewayCli gatewayCli;

    @RequestMapping(value = "/exportWord/{id}",method = RequestMethod.GET)
    @ApiOperation(value = "导出word")
    public void exportWord(
        HttpServletResponse response,
        @ApiParam(name = "id", value = "id") @PathVariable("id") String id
    ) {
        try {
            File file = gatewayCli.exportWord("毫末智行-Word导出模板", id);
            FileIo.responseExcelIO(response, file, null, file.getName(), true);
        } catch (Exception e) {
            log.error(">>> exportWord 导出Word" + e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/importWord",method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "导入word")
    public AlmResult importWord(
            HttpServletResponse response,
            @ApiParam(name = "file", value = "文件", type = "MultipartFile") @RequestPart(required = false) MultipartFile file,
            @RequestParam String  project,
            @RequestParam(required = false) String  title,
            @RequestParam String  config
    ) {
        try {
            File files = gatewayCli.fileDecrypt(file);
            //String filePath = FileIo.approvalFile(file,"C:\\temp");
            boolean flag = true;
            int i=0;
            while (flag) {
                if (files.exists() && files.length() >= 15000) {
                    flag = false;
                }
                if(i>8){
                    log.error("File export failure-------------------");
                    flag = false;
                }
                Thread.currentThread().sleep(2000);

            }
            log.info("The file has been exported.---->{},The file size---->{}",files.getAbsolutePath(),files.length());
           return  AlmResult.success(gatewayCli.importWord(config, project,title,files.getAbsolutePath()),"执行完毕,请稍后刷新页面查看导入结果");
        } catch (Exception e) {
            log.error(">>> exportWord 导入Word" + e.getMessage(), e);
            return  AlmResult.fail("执行失败");
        }
    }


}
