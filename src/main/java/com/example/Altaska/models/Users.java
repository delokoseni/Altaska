package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users"
)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @ManyToOne
    @JoinColumn(name = "id_user_type", referencedColumnName = "id")
    private UserType userType;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    @Column(name = "old_email_change_token")
    private String oldEmailChangeToken;

    @Column(name = "old_email_change_token_expires_at")
    private LocalDateTime oldEmailChangeTokenExpiresAt;

    @Column(name = "new_email")
    private String newEmail;

    @Column(name = "new_email_change_token")
    private String newEmailChangeToken;

    @Column(name = "new_email_change_token_expires_at")
    private LocalDateTime newEmailChangeTokenExpiresAt;

    @Column(name = "email_change_status")
    private Boolean emailChangeStatus;

    @Column(name = "new_password")
    private String newPassword;

    public Long getId() { return id; }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean getConfirmed() { return confirmed; }

    public UserType getUserType() {
        return userType;
    }

    public String getConfirmationToken() { return confirmationToken; }

    public String getNewPassword() {
        return newPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

    public void setConfirmationToken(String confirmationToken) { this.confirmationToken = confirmationToken; }

    public String getPasswordResetToken() { return passwordResetToken; }

    public void setPasswordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; }

    public LocalDateTime getPasswordResetTokenExpiresAt() { return passwordResetTokenExpiresAt; }

    public void setPasswordResetTokenExpiresAt(LocalDateTime passwordResetTokenExpiresAt) { this.passwordResetTokenExpiresAt = passwordResetTokenExpiresAt; }

    public String getOldEmailChangeToken() { return oldEmailChangeToken; }

    public void setOldEmailChangeToken(String oldEmailChangeToken) { this.oldEmailChangeToken = oldEmailChangeToken; }

    public LocalDateTime getOldEmailChangeTokenExpiresAt() { return oldEmailChangeTokenExpiresAt; }

    public void setOldEmailChangeTokenExpiresAt(LocalDateTime oldEmailChangeTokenExpiresAt) { this.oldEmailChangeTokenExpiresAt = oldEmailChangeTokenExpiresAt; }

    public String getNewEmail() { return newEmail; }

    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }

    public String getNewEmailChangeToken() { return newEmailChangeToken; }

    public void setNewEmailChangeToken(String newEmailChangeToken) { this.newEmailChangeToken = newEmailChangeToken; }

    public LocalDateTime getNewEmailChangeTokenExpiresAt() { return newEmailChangeTokenExpiresAt; }

    public void setNewEmailChangeTokenExpiresAt(LocalDateTime newEmailChangeTokenExpiresAt) { this.newEmailChangeTokenExpiresAt = newEmailChangeTokenExpiresAt; }

    public Boolean getEmailChangeStatus() { return emailChangeStatus; }

    public void setEmailChangeStatus(Boolean emailChangeStatus) { this.emailChangeStatus = emailChangeStatus; }

}
