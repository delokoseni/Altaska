<!DOCTYPE html>
<html lang="ru">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="csrf-token" content="${_csrf.token}">
        <link rel="stylesheet" href="/css/style.css">
        <link rel="stylesheet" href="/css/profile.css">
        <script type="module" src="/js/profile.js"></script>
        <title>Профиль</title>
    </head>
    <body>
        <header class="back-button-container">
            <a href="/mainauthorized" class="back-button">Назад</a>
        </header>
        <main class="profile-container">
            <h1 class="profile-title">Профиль</h1>
            <p class="profile-email"></p>
            <div class="profile-buttons">
                <a href="/profile/change-email" class="profile-button">Сменить email</a>
                <a href="/profile/change-password" class="profile-button">Сменить пароль</a>
                <form action="/logout" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit" class="profile-button logout-button">Выйти из аккаунта</button>
                </form>
            </div>
        </main>
        <script>
            const csrfParam = '${_csrf.parameterName}';
            const csrfToken = '${_csrf.token}';
        </script>
    </body>
</html>