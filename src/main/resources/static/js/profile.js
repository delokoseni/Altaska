import { showLoader, hideLoader } from './loader.js';

document.addEventListener("DOMContentLoaded", () => {
    const changeEmailBtn = document.querySelector('a[href="/profile/change-email"]');
    const profileContainer = document.querySelector(".profile-container");

    // Создаем форму смены email
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

    const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content'); // Получаем CSRF-токен

    // Обработчик клика на кнопку "Сменить email"
    changeEmailBtn.addEventListener("click", (e) => {
        e.preventDefault();
        profileContainer.style.display = "none";
        changeEmailFormWrapper.style.display = "flex";
    });

    // Обработчик клика на кнопку "Отмена"
    document.getElementById("cancelChangeEmail").addEventListener("click", () => {
        changeEmailFormWrapper.style.display = "none";
        profileContainer.style.display = "flex";
    });

    // Обработчик отправки формы для смены email
    document.getElementById("changeEmailForm").addEventListener("submit", function (e) {
        e.preventDefault();

        const newEmail = document.querySelector('input[name="newEmail"]').value; // Получаем значение нового email
        const saveButton = this.querySelector('button[type="submit"]');
        saveButton.disabled = true;
        showLoader();
        // Отправляем запрос с новым email
        fetch("/profile/change-email", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',  // Указываем что отправляем JSON
                'X-CSRF-TOKEN': csrfToken           // Добавляем CSRF-токен
            },
            body: JSON.stringify({ newEmail })    // Отправляем новый email в теле запроса
        })
        .then(response => {
            hideLoader();
            saveButton.disabled = false;
            if (response.ok) {
                alert("На оба адреса электронной почты были отправлены ссылки для подтверждения.");
                location.reload(); // Перезагружаем страницу, чтобы отобразить новый email
            } else {
                return response.text().then(text => {
                    throw new Error(text || "Ошибка при обновлении email");
                });
            }
        })
        .catch(error => {
            saveButton.disabled = false;
            alert(error.message); // Показываем ошибку, если запрос не удался
        });
    });
});
