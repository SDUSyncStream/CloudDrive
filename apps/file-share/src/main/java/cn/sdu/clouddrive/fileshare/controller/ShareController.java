package cn.sdu.clouddrive.fileshare.controller;


import cn.sdu.clouddrive.fileshare.annotation.GlobalInterceptor;
import cn.sdu.clouddrive.fileshare.annotation.VerifyParam;
import cn.sdu.clouddrive.fileshare.pojo.dto.SessionWebUserDto;
import cn.sdu.clouddrive.fileshare.pojo.po.FileShare;
import cn.sdu.clouddrive.fileshare.pojo.query.FileShareQuery;
import cn.sdu.clouddrive.fileshare.pojo.vo.PaginationResultVO;
import cn.sdu.clouddrive.fileshare.pojo.vo.ResponseVO;
import cn.sdu.clouddrive.fileshare.service.FileShareService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController("shareController")
@RequestMapping("/share")
public class ShareController extends ABaseController {
    @Resource
    private FileShareService fileShareService;


    @RequestMapping("/loadShareList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadShareList(HttpSession session, FileShareQuery query) {
        query.setOrderBy("share_time desc");
        
        // 优先使用前端传递的userId，如果没有则从session获取
        if (query.getUserId() == null || query.getUserId().isEmpty()) {
            SessionWebUserDto userDto = getUserInfoFromSession(session);
            if (userDto != null) {
                query.setUserId(userDto.getUserId());
            }
        }
        
        query.setQueryFileName(true);
        PaginationResultVO resultVO = this.fileShareService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/shareFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO shareFile(HttpSession session,
                                @VerifyParam(required = true) String fileId,
                                @VerifyParam(required = true) Integer validType,
                                String code,
                                String userId) {
        // 优先使用前端传递的userId，如果没有则从session获取
        if (userId == null || userId.isEmpty()) {
            SessionWebUserDto userDto = getUserInfoFromSession(session);
            if (userDto != null) {
                userId = userDto.getUserId();
            }
        }
        
        FileShare share = new FileShare();
        share.setFileId(fileId);
        share.setValidType(validType);
        share.setCode(code);
        share.setUserId(userId);
        fileShareService.saveShare(share);
        return getSuccessResponseVO(share);
    }

    @RequestMapping("/cancelShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO cancelShare(HttpSession session, @VerifyParam(required = true)
        @RequestParam(value = "shareIds", required = true) String shareIds,
        @RequestParam(value = "userId", required = false) String userId) {
        // 优先使用前端传递的userId，如果没有则从session获取
        if (userId == null || userId.isEmpty()) {
            SessionWebUserDto userDto = getUserInfoFromSession(session);
            if (userDto != null) {
                userId = userDto.getUserId();
            }
        }
        
        fileShareService.deleteFileShareBatch(shareIds.split(","), userId);
        return getSuccessResponseVO(null);
    }
}
