package cn.sdu.fileupdownservice.controller;

import cn.sdu.fileupdownservice.annotation.GlobalInterceptor;
import cn.sdu.fileupdownservice.annotation.VerifyParam;
import cn.sdu.fileupdownservice.entity.dto.SessionWebUserDto;
import cn.sdu.fileupdownservice.entity.dto.UploadResultDto;
import cn.sdu.fileupdownservice.entity.dto.UserSpaceDto;
import cn.sdu.fileupdownservice.entity.po.Users;
import cn.sdu.fileupdownservice.entity.vo.ResponseVO;
import cn.sdu.fileupdownservice.service.impl.UserInfoServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 文件信息 Controller
 */
@RestController("fileInfoController")
@RequestMapping("/fileup")
public class FileInfoController extends CommonFileController {


    private final UserInfoServiceImpl userInfoService;

    public FileInfoController(UserInfoServiceImpl userInfoService) {
        super();
        this.userInfoService = userInfoService;
    }
    @GetMapping("/user/space/{userId}")
    public UserSpaceDto getUserSpace1(@PathVariable String userId) {
        return fileInfoService.getUserSpace(userId);
    }
    @RequestMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO uploadFile(String userId,
                                 String fileId,
                                 MultipartFile file,
                                 @VerifyParam(required = true) String fileName,
                                 @VerifyParam(required = true) String filePid,
                                 @VerifyParam(required = true) String fileMd5,
                                 @VerifyParam(required = true) Integer chunkIndex,
                                 @VerifyParam(required = true) Integer chunks) {

        SessionWebUserDto webUserDto = new SessionWebUserDto();
        Users users = userInfoService.selectUser(userId);
        webUserDto.setUserId(userId);
        webUserDto.setNickName(users.getNickName());
        UploadResultDto resultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
        return getSuccessResponseVO(resultDto);
    }



    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getFolderInfo(HttpSession session, @VerifyParam(required = true) String path) {
        return super.getFolderInfo(path, getUserInfoFromSession(session).getUserId());
    }




    @RequestMapping("/createDownloadUrl/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO createDownloadUrl(String userId, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        return super.createDownloadUrl(fileId, userId);
    }

    @RequestMapping("/download/{code}")
    @GlobalInterceptor( checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable("code") @VerifyParam(required = true) String code) throws Exception {
        super.download(request, response, code);
    }
}