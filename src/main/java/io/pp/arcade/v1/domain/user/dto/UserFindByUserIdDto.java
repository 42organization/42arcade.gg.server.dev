package io.pp.arcade.v1.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class UserFindByUserIdDto {
    Integer userId;

    @Override
    public String toString() {
        return "UserFindByUserIdDto{" +
                "userId=" + userId +
                '}';
    }
}