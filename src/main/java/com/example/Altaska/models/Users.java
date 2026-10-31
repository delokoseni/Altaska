package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
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

}
