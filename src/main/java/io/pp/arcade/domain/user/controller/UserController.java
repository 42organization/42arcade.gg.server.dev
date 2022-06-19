package io.pp.arcade.domain.user.controller;

import io.pp.arcade.domain.user.dto.*;
import io.pp.arcade.domain.user.dto.UserDetailResponseDto;
import io.pp.arcade.domain.user.dto.UserHistoricResponseDto;
import io.pp.arcade.domain.user.dto.UserResponseDto;
import io.pp.arcade.domain.user.dto.UserSearchResultResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

public interface UserController {
    UserResponseDto userFind(HttpServletRequest request);
    UserDetailResponseDto userFindDetail(@PathVariable String targetUserId, HttpServletRequest request);
    UserHistoricResponseDto userFindHistorics(@PathVariable String userId, @PageableDefault(size = 10) Pageable pageable);
    void userModifyProfile(@PathVariable String intraId, HttpServletRequest request);
    UserSearchResultResponseDto userSearchResult(@RequestParam(value="q") String inquiringString); // 유효성 검사 해야함
    UserLiveInfoResponseDto userLiveInfo(HttpServletRequest request);
}
