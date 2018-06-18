package com.numbyc.mywatcher.controller;

import com.numbyc.mywatcher.beans.common.CheckFileResult;
import com.numbyc.mywatcher.beans.upload.MergeFile;
import com.numbyc.mywatcher.beans.upload.ResultBean;
import com.numbyc.mywatcher.beans.upload.UploadFile;
import com.numbyc.mywatcher.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * 上传文件接口
 *
 * @author Numbyc
 * @version 1.0
 */

@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final String WITH_CUT_POINT = "0";

    private static final String NO_CUT_POINT = "1";

    @Value("${upload.temppath}")
    String uploadTempPath;

    @Value("${upload.finalpath}")
    String uploadFinalPath;

    @PostMapping("/")
    public ResultBean uploadFile(UploadFile file) throws IllegalStateException, IOException {
        ResultBean resultBean = new ResultBean();
        resultBean.setCode("success");
        if (file.getStatus().equals(WITH_CUT_POINT)) {
            File[] existFiles = new File(uploadTempPath + "/" + file.getMd5value()).listFiles();
            int [] blocks = new int[existFiles.length];
            Arrays.fill(blocks, 0);
            for (File tempFile : existFiles) {
                blocks[Integer.valueOf(tempFile.getName())] = 1;
            }
            if (blocks[Integer.valueOf(file.getChunk())] == 1) {
                return resultBean;
            }
        } else {
            File newDir = new File(uploadTempPath + "/" + file.getMd5value());
            newDir.mkdir();
        }
        String path;
        if (null == file.getChunk()) {
            path = uploadTempPath + file.getMd5value() + "/0";
        } else {
            path = uploadTempPath + file.getMd5value() + "/" + file.getChunk();
        }
        file.getFile().transferTo(new File(path));
        return resultBean;

    }

    @PostMapping("/checkpoint")
    public ResultBean checkPoint(@RequestParam String md5) throws IllegalStateException {
        ResultBean resultBean = new ResultBean();
        CheckFileResult result = new CheckFileResult();
        String tempPath = uploadTempPath + md5;

        File file = new File(tempPath);
        if (null == file) {
            result.setCode(NO_CUT_POINT);
            resultBean.setResult(result);
            return resultBean;
        }

        File[] existFiles = file.listFiles();
        if ( null == existFiles || existFiles.length == 0) {
            result.setCode(NO_CUT_POINT);
            resultBean.setResult(result);
            return resultBean;
        }

        String [] blocks = new String[existFiles.length];
        for (int index = 0; index < existFiles.length; index++) {
            blocks[index] = existFiles[index].getName();
        }
        result.setCode(WITH_CUT_POINT);
        result.setBlocks(blocks);
        resultBean.setResult(result);
        return resultBean;
    }

    @PostMapping("/merge")
    public ResultBean mergeFiles(MergeFile mergeFile) throws IllegalStateException {
        ResultBean resultBean = new ResultBean();
        resultBean.setCode("fail");
        String mergedFilePath = uploadFinalPath + mergeFile.getFileName();
        boolean result = FileUtils.saveFileTogether(uploadTempPath + mergeFile.getMd5(), mergedFilePath);
        if (!result) {
            return resultBean;
        }
        String finalMd5 = FileUtils.getFileMd5(mergedFilePath);

        if (finalMd5.equals(mergeFile.getMd5())) {
            File dir = new File(uploadTempPath + mergeFile.getMd5());
            dir.delete();
            resultBean.setCode("success");
        } else {
            resultBean.setCode("fail");
        }
        return resultBean;
    }
}
