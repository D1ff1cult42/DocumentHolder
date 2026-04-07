package com.d1ff.authservice.service.auth.interfaces;

public interface ActivateAccountService {
    void sendFirstDeletionWarning();
    void sendSecondDeletionWarning();
    void AccountDeletion();
}
