package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.repositories.ProjectMembersRepository;
import com.example.Altaska.services.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class InviteController {

    private final ProjectMembersRepository projectMembersRepository;

    private final ActivityLogService activityLogService;

    @GetMapping("/confirm-invite")
    public String confirmInvite(@RequestParam String token, Model model) {
        Optional<ProjectMembers> memberOpt = projectMembersRepository.findByConfirmationToken(token);

        if (memberOpt.isEmpty()) {
            model.addAttribute("statusTitle", "Ошибка");
            model.addAttribute("statusMessage", "Недействительный или просроченный токен.");
            return "confirmInvite";
        }

        ProjectMembers member = memberOpt.get();
        member.setConfirmed(true);
        member.setConfirmationToken(null);
        projectMembersRepository.save(member);
        activityLogService.logActivity(
                member.getIdUser(),
                member.getIdProject(),
                "update",
                "project_member",
                member.getIdUser().getId(),
                List.of(new Change(
                        "confirmed",
                        false,
                        true
                )),
                "Пользователь присоединился к проекту"
        );
        model.addAttribute("statusTitle", "Приглашение подтверждено");
        model.addAttribute("statusMessage", "Вы успешно присоединились к проекту \"" + member.getIdProject().getName() + "\"!");
        model.addAttribute("goToLogin", true);

        return "confirmInvite";
    }
}

