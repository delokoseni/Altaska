import { showLoader, hideLoader } from './loader.js';

document.addEventListener("DOMContentLoaded", () => {
    const changeEmailBtn = document.querySelector('a[href="/profile/change-email"]');
    const changePasswordBtn = document.querySelector('a[href="/profile/change-password"]');
    const profileContainer = document.querySelector(".profile-container");

    const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');

    const changeEmailFormWrapper = document.createElement("div");
    changeEmailFormWrapper.classList.add("change-email-form");
    changeEmailFormWrapper.style.display = "none";
    changeEmailFormWrapper.style.flexDirection = "column";
    changeEmailFormWrapper.style.alignItems = "center";
    changeEmailFormWrapper.innerHTML = `
        <h2 class="profile-title">Сменить email</h2>
        <form id="changeEmailForm" class="profile-buttons">
            <input type="email" name="newEmail" placeholder="Новый email" required class="profile-input">
            <button type="submit" class="profile-button">Сохранить</button>
            <button type="button" class="profile-button" id="cancelChangeEmail">Отмена</button>
        </form>
    `;
    profileContainer.parentNode.insertBefore(changeEmailFormWrapper, profileContainer.nextSibling);

    changeEmailBtn.addEventListener("click", (e) => {
        e.preventDefault();
        profileContainer.style.display = "none";
        changeEmailFormWrapper.style.display = "flex";
    });

    document.getElementById("cancelChangeEmail").addEventListener("click", () => {
        changeEmailFormWrapper.style.display = "none";
        profileContainer.style.display = "flex";
    });

    document.getElementById("changeEmailForm").addEventListener("submit", function (e) {
        e.preventDefault();

        const newEmail = document.querySelector('input[name="newEmail"]').value;
        const saveButton = this.querySelector('button[type="submit"]');
        saveButton.disabled = true;
        showLoader();

        fetch("/profile/change-email", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ newEmail })
        })
        .then(response => {
            hideLoader();
            saveButton.disabled = false;
            if (response.ok) {
                alert("На оба адреса электронной почты были отправлены ссылки для подтверждения.");
                location.reload();
            } else {
                return response.text().then(text => {
                    throw new Error(text || "Ошибка при обновлении email");
                });
            }
        })
        .catch(error => {
            saveButton.disabled = false;
            hideLoader();
            alert(error.message);
        });
    });

    const changePasswordFormWrapper = document.createElement("div");
    changePasswordFormWrapper.classList.add("change-password-form");
    changePasswordFormWrapper.style.display = "none";
    changePasswordFormWrapper.style.flexDirection = "column";
    changePasswordFormWrapper.style.alignItems = "center";
    changePasswordFormWrapper.innerHTML = `
        <h2 class="profile-title">Сменить пароль</h2>
        <form id="changePasswordForm" class="profile-buttons">
            <div class="password-container">
                <input type="password" id="passwordOld" name="oldPassword" placeholder="Старый пароль" required class="profile-input">
                <span class="toggle-password" title="Показать/Скрыть пароль">
                    <img id="eye-icon-passwordOld" src="/icons/EyeSlashed.svg" class="eye-icon" alt="Глаз">
                </span>
            </div>
            <div class="password-container">
                <input type="password" id="passwordNew" name="newPassword" placeholder="Новый пароль" required class="profile-input">
                <span class="toggle-password" title="Показать/Скрыть пароль">
                    <img id="eye-icon-passwordNew" src="/icons/EyeSlashed.svg" class="eye-icon" alt="Глаз">
                </span>
            </div>
            <div class="password-container">
                <input type="password" id="passwordNewRepeat" name="repeatNewPassword" placeholder="Повторите новый пароль" required class="profile-input">
                <span class="toggle-password" title="Показать/Скрыть пароль">
                    <img id="eye-icon-passwordNewRepeat" src="/icons/EyeSlashed.svg" class="eye-icon" alt="Глаз">
                </span>
            </div>
            <button type="submit" class="profile-button">Сохранить</button>
            <button type="button" class="profile-button" id="cancelChangePassword">Отмена</button>
        </form>
    `;
    profileContainer.parentNode.insertBefore(changePasswordFormWrapper, changeEmailFormWrapper.nextSibling);

    document.querySelectorAll('.toggle-password').forEach(toggle => {
        toggle.addEventListener('click', () => {
            const input = toggle.previousElementSibling;
            const icon = toggle.querySelector('img');

            if (input.type === 'password') {
                input.type = 'text';
                icon.src = '/icons/EyeOpen.svg';
            } else {
                input.type = 'password';
                icon.src = '/icons/EyeSlashed.svg';
            }
        });
    });

    changePasswordBtn.addEventListener("click", (e) => {
        e.preventDefault();
        profileContainer.style.display = "none";
        changePasswordFormWrapper.style.display = "flex";
    });

    document.getElementById("cancelChangePassword").addEventListener("click", () => {
        changePasswordFormWrapper.style.display = "none";
        profileContainer.style.display = "flex";
    });

    document.getElementById("changePasswordForm").addEventListener("submit", function (e) {
        e.preventDefault();

        const form = this;
        const oldPassword = form.querySelector('input[name="oldPassword"]').value;
        const newPassword = form.querySelector('input[name="newPassword"]').value;
        const repeatNewPassword = form.querySelector('input[name="repeatNewPassword"]').value;
        const saveButton = form.querySelector('button[type="submit"]');

        if (newPassword !== repeatNewPassword) {
            alert("Новые пароли не совпадают");
            return;
        }

        saveButton.disabled = true;
        showLoader();

        fetch("/profile/change-password", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                oldPassword,
                newPassword,
                repeatPassword: repeatNewPassword,
            })
        })
        .then(response => {
            hideLoader();
            saveButton.disabled = false;
            if (response.ok) {
                alert("Вам на почту было отправлено письмо со ссылкой для завершения смены пароля.");
                location.reload();
            } else {
                return response.text().then(text => {
                    throw new Error(text || "Ошибка при смене пароля");
                });
            }
        })
        .catch(error => {
            hideLoader();
            saveButton.disabled = false;
            alert(error.message);
        });
    });
});
