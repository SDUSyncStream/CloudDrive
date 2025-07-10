package cn.sdu.clouddrive.filemanageservice.controller;

import cn.sdu.clouddrive.filemanageservice.po.FileInfo;
import cn.sdu.clouddrive.filemanageservice.service.FileInfoService;
import cn.sdu.clouddrive.filemanageservice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileInfoController {
    @Autowired
    private FileInfoService fileInfoService;

    @GetMapping("/get/{pid}")
    public Result<List<FileInfo>> getFilesByUserIdAndPid(@RequestParam String userId, @PathVariable String pid, @RequestParam String delFlag) {
        int file_delFlag = Integer.parseInt(delFlag);
        List<FileInfo> fileInfos = fileInfoService.selectByPidAndUserId(pid, userId, file_delFlag);
        return Result.success(fileInfos);
    }

    @GetMapping("/copy/{fileId}")
    public Result<String> CopyFile(@PathVariable String fileId, @RequestParam String userId, @RequestParam String targetId) {
        String newFileId = UUID.randomUUID().toString();
        fileInfoService.CopyFile(fileId, userId, targetId, newFileId);
        fileInfoService.CreateTime(fileId, userId);
        fileInfoService.UpdateTime(newFileId, userId);
        return Result.success("粘贴成功");
    }

    @GetMapping("/recycle/{fileId}")
    public Result<String> RecycleFile(@PathVariable String fileId, @RequestParam String userId, @RequestParam String newDelFlag) {
        int file_newDelFlag = Integer.parseInt(newDelFlag);
        String file_pid = "1";
        String message = "";
        if (file_newDelFlag == 1) {
            file_pid = "1";
            message = "回收成功";
        } else {
            file_pid = "0";
            message = "还原成功";
        }
        fileInfoService.RecycleFile(fileId, userId, file_newDelFlag, file_pid);
        return Result.success(message);
    }

    @GetMapping("/rename/{fileId}")
    public Result<String> RenameFile(@PathVariable String fileId, @RequestParam String userId, @RequestParam String newName) {

        String message = "重命名成功";
        fileInfoService.RenameFile(fileId, userId, newName);
        fileInfoService.UpdateTime(fileId, userId);
        return Result.success(message);
    }

    @GetMapping("/newfolder/{pid}")
    public Result<String> NewFolder(@PathVariable String pid, @RequestParam String userId, @RequestParam String newName) {
        String newFileId = UUID.randomUUID().toString();
        String message = "新建文件夹成功";
        fileInfoService.NewFolder(newFileId, userId, pid, newName);
        fileInfoService.CreateTime(newFileId, userId);
        fileInfoService.UpdateTime(newFileId, userId);
        return Result.success(message);
    }

    @GetMapping("/recycle/delete/all")
    public Result<String> clearRecycle(@RequestParam String userId) {
        fileInfoService.clearRecycle(userId);
        String message = "清空成功";
        return Result.success(message);
    }
    @GetMapping("/recycle/delete/{fileId}")
    public Result<String> deleteFile(@PathVariable String fileId, @RequestParam String userId) {
        fileInfoService.deleteFile(fileId, userId);
        String message = "删除成功";
        return Result.success(message);
    }
}
