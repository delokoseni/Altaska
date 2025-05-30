package com.example.Altaska.repositories;

import com.example.Altaska.models.Notifications;
import com.example.Altaska.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {

    List<Notifications> findByIdUserIdOrderByCreatedAtDesc(Long userId);

    List<Notifications> findByIdUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByIdUserIdAndIsReadFalse(Long userId);

    List<Notifications> findAllByIdUserOrderByCreatedAtDesc(Users idUser);

    List<Notifications> findAllByIdUser(Users user);

}
