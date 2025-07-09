package cn.sdu.clouddrive.admin.Controller;

import cn.sdu.clouddrive.admin.Service.FileInfoService;
import cn.sdu.clouddrive.admin.pojo.FileInfo;
import cn.sdu.clouddrive.admin.util.ServerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-api/file") // 将文件相关的API统一放在 /admin-api/file 路径下
public class FileInfoController {

    @Autowired
    private FileInfoService fileInfoService; // 注入 FileInfoService

    /**
     * 获取所有文件信息
     * GET /admin-api/file/getAllFileInfos
     * @return 包含所有文件信息列表的 ServerResult
     */
    @GetMapping("/getAllFileInfos")
    public ServerResult<List<FileInfo>> getAllFileInfos() {
        List<FileInfo> fileInfos = fileInfoService.getAllFileInfos();
        return ServerResult.ok(fileInfos, "获取所有文件信息成功");
    }

    /**
     * 根据用户ID获取该用户的所有文件信息
     * GET /admin-api/file/getFileInfosByUserId/{userId}
     * @param userId 用户ID
     * @return 包含该用户文件信息列表的 ServerResult
     */
    @GetMapping("/getFileInfosByUserId/{userId}")
    public ServerResult<List<FileInfo>> getFileInfosByUserId(@PathVariable String userId) {
        if (userId == null || userId.isEmpty()) {
            return ServerResult.fail("用户ID不能为空");
        }
        List<FileInfo> fileInfos = fileInfoService.getFileInfosByUserId(userId);
        return ServerResult.ok(fileInfos, "根据用户ID获取文件信息成功");
    }

    /**
     * 根据文件ID和用户ID获取单个文件信息
     * GET /admin-api/file/getFileInfo/{fileId}/{userId}
     * 由于fileId和userId是联合主键，两者都必须提供
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 包含单个文件信息的 ServerResult，如果不存在则数据为null
     */
    @GetMapping("/getFileInfo/{fileId}/{userId}")
    public ServerResult<FileInfo> getFileInfoByIdAndUserId(@PathVariable String fileId, @PathVariable String userId) {
        if (fileId == null || fileId.isEmpty() || userId == null || userId.isEmpty()) {
            return ServerResult.fail("文件ID和用户ID不能为空");
        }
        FileInfo fileInfo = fileInfoService.getFileInfoByIdAndUserId(fileId, userId);
        if (fileInfo != null) {
            return ServerResult.ok(fileInfo, "获取文件信息成功");
        } else {
            return ServerResult.fail("未找到对应的文件信息");
        }
    }

    /**
     * 添加新的文件信息
     * POST /admin-api/file/addFileInfo
     * @param fileInfo 要添加的文件信息对象，JSON格式
     * @return 操作结果的 ServerResult
     */
    @PostMapping("/addFileInfo")
    public ServerResult<String> addFileInfo(@RequestBody FileInfo fileInfo) {
        // 可以在此处进行更详细的参数校验
        if (fileInfo == null || fileInfo.getFileId() == null || fileInfo.getUserId() == null || fileInfo.getFileMd5() == null) {
            return ServerResult.fail("文件信息不完整，缺少必要字段");
        }
        try {
            int result = fileInfoService.addFileInfo(fileInfo);
            if (result > 0) {
                return ServerResult.ok("添加文件信息成功");
            } else {
                return ServerResult.fail("添加文件信息失败");
            }
        } catch (Exception e) {
            // 捕获可能的数据库操作异常
            return ServerResult.fail("添加文件信息时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新文件信息
     * PUT /admin-api/file/updateFileInfo
     * 由于是联合主键，请求体中的 fileInfo 必须包含 fileId 和 userId 来确定更新目标
     * @param fileInfo 包含更新字段和主键信息的文件信息对象，JSON格式
     * @return 操作结果的 ServerResult
     */
    @PutMapping("/updateFileInfo")
    public ServerResult<String> updateFileInfo(@RequestBody FileInfo fileInfo) {
        if (fileInfo == null || fileInfo.getFileId() == null || fileInfo.getUserId() == null) {
            return ServerResult.fail("更新文件信息时文件ID和用户ID不能为空");
        }
        try {
            int result = fileInfoService.updateFileInfo(fileInfo);
            if (result > 0) {
                return ServerResult.ok("更新文件信息成功");
            } else {
                // 可能是没有找到对应的记录进行更新
                return ServerResult.fail("更新文件信息失败，可能未找到对应记录");
            }
        } catch (Exception e) {
            return ServerResult.fail("更新文件信息时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据文件ID和用户ID删除文件信息
     * DELETE /admin-api/file/deleteFileInfo/{fileId}/{userId}
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 操作结果的 ServerResult
     */
    @DeleteMapping("/deleteFileInfo/{fileId}/{userId}")
    public ServerResult<String> deleteFileInfo(@PathVariable String fileId, @PathVariable String userId) {
        if (fileId == null || fileId.isEmpty() || userId == null || userId.isEmpty()) {
            return ServerResult.fail("文件ID和用户ID不能为空");
        }
        try {
            int result = fileInfoService.deleteFileInfo(fileId, userId);
            if (result > 0) {
                return ServerResult.ok("删除文件信息成功");
            } else {
                return ServerResult.fail("删除文件信息失败，可能未找到对应记录");
            }
        } catch (Exception e) {
            return ServerResult.fail("删除文件信息时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取处于正常状态的文件总数（del_flag=2）
     * GET /admin-api/file/getTotalNormalFiles
     * @return 包含文件总数的 ServerResult
     */
    @GetMapping("/getTotalNormalFiles")
    public ServerResult<Integer> getTotalNormalFiles() {
        Integer total = fileInfoService.getTotalNormalFiles();
        return ServerResult.ok(total, "获取正常状态文件总数成功");
    }
}