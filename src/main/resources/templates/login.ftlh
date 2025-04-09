<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/css/mainstyle.css">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="/css/registrationlogin.css">
    <script src="/js/themeToggle.js" defer></script>
    <script src="/js/passwordToggle.js"></script>
    <title>Вход</title>
</head>
<body>
<header>
    <div class="site-title">altaska</div>
    <div>
        <a href="/" class="login-button">На главную</a>
        <a href="/registration" class="register-button">Зарегистрироваться</a>
    </div>
</header>
<main>
    <div class="container">
        <h1>Вход</h1>
        <#if error??>
            <div class="error-message" style="color: red;">${error}</div>
        </#if>
    <form action="/login" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>Email</label>
        <input type="text" name="username" placeholder="Email" required>

        <label>Пароль</label>
        <div class="password-container">
            <input type="password" id="password" name="password" placeholder="Пароль" required>
            <span class="toggle-password" onclick="togglePasswordVisibility('password')" title="Показать/Скрыть пароль">
                    <img id="eye-icon-password" src="/icons/EyeSlashed.svg" class="eye-icon" alt="Глаз">
                </span>
        </div>

        <button type="submit">Войти</button>
    </form>
    </div>
</main>
<footer>
    <p>
        Разработчик: Бельш А.Е.<br>
        Алтайский государственный технический университет<br>
        Барнаул, 2025
    </p>
</footer>
</body>
</html>