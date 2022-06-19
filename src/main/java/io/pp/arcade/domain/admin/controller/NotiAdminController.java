package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.all.NotiAllDto;
import io.pp.arcade.domain.admin.dto.create.NotiCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.NotiUpdateRequestDto;
import io.pp.arcade.domain.noti.dto.NotiDeleteDto;
import io.pp.arcade.domain.noti.dto.NotiDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface NotiAdminController {
    void notiCreate(@RequestBody NotiCreateRequestDto notiCreateRequestDto, HttpServletRequest request);
    void notiUpdate(@RequestBody NotiUpdateRequestDto notiUpdateRequestDto, HttpServletRequest request);
    void notiDelete(@PathVariable Integer notiId, HttpServletRequest request);
    List<NotiDto> notiAll(Pageable pageable, HttpServletRequest request);
}
