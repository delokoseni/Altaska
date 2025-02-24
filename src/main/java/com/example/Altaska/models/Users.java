package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.LocalDate;

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
    private LocalDate passwordResetTokenExpiresAt;      //В БД тип данных Timestamp with time zone

    @Column(name = "old_email_change_token")
    private String oldEmailChangeToken;

    @Column(name = "old_email_change_token_expires_at")
    private LocalDate oldEmailChangeTokenExpiresAt;     //В БД тип данных Timestamp with time zone

    @Column(name = "new_email")
    private String newEmail;

    @Column(name = "new_email_change_token")
    private String newEmailChangeToken;

    @Column(name = "new_email_change_token_expires_at")
    private LocalDate newEmailChangeTokenExpiresAt;     //В БД тип данных Timestamp with time zone

    @Column(name = "email_change_status")
    private boolean emailChangeStatus;

    public Long GetId() {
        return id;
    }

    public String GetEmail() {
        return email;
    }

    public String GetPassword() {
        return password;
    }

    public UserType GetUserType() {
        return userType;
    }
}
