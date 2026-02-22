package com.jiwu.api.user.service;

public interface BillsMQService {


    /**
     * 自动更新用户钱包信息
     *
     * @param userId  用户id
     */
    void autoUpdateWallet(String userId);

}
