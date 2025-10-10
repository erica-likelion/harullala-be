package likelion.harullala.service;

import likelion.harullala.dto.MyInfoResponse;

public interface UserService {
    MyInfoResponse getMyInfo(Long userId);
}
