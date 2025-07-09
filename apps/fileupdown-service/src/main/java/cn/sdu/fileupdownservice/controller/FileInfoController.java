package cn.sdu.fileupdownservice.controller;

import cn.sdu.fileupdownservice.annotation.GlobalInterceptor;
import cn.sdu.fileupdownservice.annotation.VerifyParam;
import cn.sdu.fileupdownservice.entity.dto.SessionWebUserDto;
import cn.sdu.fileupdownservice.entity.dto.UploadResultDto;
import cn.sdu.fileupdownservice.entity.po.UserInfo;
import cn.sdu.fileupdownservice.entity.query.UserInfoQuery;
import cn.sdu.fileupdownservice.entity.vo.ResponseVO;
import cn.sdu.fileupdownservice.service.impl.UserInfoServiceImpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 文件信息 Controller
 */
@RestController("fileInfoController")
@RequestMapping("/file")
public class FileInfoController extends CommonFileController {


    private final UserInfoServiceImpl userInfoService;

    public FileInfoController(UserInfoServiceImpl userInfoService) {
        super();
        this.userInfoService = userInfoService;
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
        UserInfoQuery query = new UserInfoQuery();
        query.setUserId(userId);
        List<UserInfo> userInfo = userInfoService.findListByParam(query);
        webUserDto.setUserId(userId);
        webUserDto.setNickName(userInfo.get(0).getNickName());
        webUserDto.setAdmin(false);
        webUserDto.setAvatar(userInfo.get(0).getQqAvatar());
        UploadResultDto resultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
        return getSuccessResponseVO(resultDto);
    }


    @RequestMapping("/getImage/{imageFolder}/{imageName}")
    public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName) {
        super.getImage(response, imageFolder, imageName);
    }

    @RequestMapping("/ts/getVideoInfo/{fileId}")
    public void getVideoInfo(HttpServletResponse response, HttpSession session, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDto.getUserId());
    }

    @RequestMapping("/getFile/{fileId}")
    public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDto.getUserId());
    }


    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getFolderInfo(HttpSession session, @VerifyParam(required = true) String path) {
        return super.getFolderInfo(path, getUserInfoFromSession(session).getUserId());
    }




    @RequestMapping("/createDownloadUrl/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO createDownloadUrl(HttpSession session, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        return super.createDownloadUrl(fileId, getUserInfoFromSession(session).getUserId());
    }

    @RequestMapping("/download/{code}")
    @GlobalInterceptor( checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable("code") @VerifyParam(required = true) String code) throws Exception {
        super.download(request, response, code);
    }
}