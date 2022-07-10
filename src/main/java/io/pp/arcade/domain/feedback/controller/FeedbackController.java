package io.pp.arcade.domain.feedback.controller;

import io.pp.arcade.domain.feedback.dto.FeedbackRequestDto;
import io.pp.arcade.global.type.FeedbackType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

public interface FeedbackController {
    void feedbackSave(@RequestBody FeedbackRequestDto saveReqDto,
                      HttpServletRequest request);
}
